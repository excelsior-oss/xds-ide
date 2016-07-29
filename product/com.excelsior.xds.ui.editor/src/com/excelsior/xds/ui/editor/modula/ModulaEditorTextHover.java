package com.excelsior.xds.ui.editor.modula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.search.modula.utils.ModulaSearchUtils;
import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.parser.commons.ast.IAstFrameChild;
import com.excelsior.xds.parser.commons.ast.IAstFrameNode;
import com.excelsior.xds.parser.commons.ast.IElementType;
import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenType;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.ast.tokens.PragmaTokenTypes;
import com.excelsior.xds.parser.modula.scanner.jflex._XdsFlexScanner;
import com.excelsior.xds.parser.modula.symbol.IConstantSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleAliasSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureDefinitionSymbol;
import com.excelsior.xds.parser.modula.symbol.IVariableSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IForwardTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;
import com.excelsior.xds.ui.commons.swt.resources.ResourceRegistry;
import com.excelsior.xds.ui.commons.utils.XStyledString;
import com.excelsior.xds.ui.editor.commons.HoverInfoWithText;
import com.excelsior.xds.ui.editor.commons.HoverInformationControl;
import com.excelsior.xds.ui.editor.commons.IHoverInfo;
import com.excelsior.xds.ui.editor.commons.text.EditorTextUtils;
import com.excelsior.xds.ui.editor.internal.modula.hover.ModulaEditorHoverRegistry;
import com.excelsior.xds.ui.editor.modula.utils.ModulaEditorSymbolUtils;
import com.excelsior.xds.ui.viewers.ModulaSymbolDescriptions;
import com.excelsior.xds.ui.viewers.ModulaSymbolImages;

/**
 * Standard implementation of class for providing hover information 
 * for Modula-2/Oberon-2 elements.
 */
public class ModulaEditorTextHover implements ITextHover
                                            , ITextHoverExtension
                                            , ITextHoverExtension2 
{
    private static final int MAX_LINES_FROM_SOURCE_IN_HOVER = 60;
    
    private final ITextEditor editor;
    private final ITextHover defaultHover;
    private final Color fDecorationColor;

	private final ResourceRegistry resourceRegistry;

	private final IProject project; // project of the IFile being processed in this editor
	
	private static List<ITextHover> hoverContributors = new ArrayList<ITextHover>();
	
	private ITextHover lastReturnedHover;

    public ModulaEditorTextHover(ITextEditor editor, ITextHover defaultHover, ResourceRegistry resourceRegistry) {
        this.editor = editor;
        if (editor != null) {
        	this.project = CoreEditorUtils.getIProjectFrom(editor.getEditorInput());
        }
        else{
        	this.project = null;
        }
        this.defaultHover = defaultHover;
        this.fDecorationColor = JFaceResources.getColorRegistry().get(JFacePreferences.DECORATIONS_COLOR);
        this.resourceRegistry = resourceRegistry;
        
		ModulaEditorHoverRegistry.get().contributions().stream()
				.forEach(ModulaEditorTextHover::addContributer);
    }
    
    /**
	   * Register a ITextHover tooltip contributor
	   * 
	   * @param hoverContributor
	   */
	private static void addContributer(ITextHover hoverContributor) {
		hoverContributors.add(hoverContributor);
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		// Check through the contributed hover providers.
		for (ITextHover hoverContributer : hoverContributors) {
			@SuppressWarnings("deprecation")
			String hoverText = hoverContributer.getHoverInfo(textViewer,
					hoverRegion);

			if (hoverText != null) {
				lastReturnedHover = hoverContributer;

				return hoverText;
			}
		}
        
        return String.valueOf(getHoverInfo2(textViewer, hoverRegion));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		// Overridden from ITextHoverExtension2. We try and return the richest
		// help available; this
		// means trying to call getHoverInfo2() on any contributors, and falling
		// back on getHoverInfo().
		lastReturnedHover = null;

		// Check through the contributed hover providers.
		for (ITextHover hoverContributer : hoverContributors) {
			if (hoverContributer instanceof ITextHoverExtension2) {
				Object hoverInfo = ((ITextHoverExtension2) hoverContributer)
						.getHoverInfo2(textViewer, hoverRegion);

				if (hoverInfo != null) {
					lastReturnedHover = hoverContributer;
					return hoverInfo;
				}
			} else {
				@SuppressWarnings("deprecation")
				String hoverText = hoverContributer.getHoverInfo(textViewer,
						hoverRegion);

				if (hoverText != null) {
					lastReturnedHover = hoverContributer;
					return hoverText;
				}
			}
		}
        
        return getModulaDocHover(textViewer, hoverRegion);
    }

	private Object getModulaDocHover(ITextViewer textViewer, IRegion hoverRegion) {
		PstLeafNode pstLeafNode = ModulaEditorSymbolUtils.getPstLeafNode(
            editor, hoverRegion.getOffset()
        ); 
        
        if (defaultHover != null && pstLeafNode == null) {
            hoverRegion = defaultHover.getHoverRegion(textViewer, hoverRegion.getOffset());
        }
            
        IHoverInfo hoverInfo = null;
          
        @SuppressWarnings("deprecation")
        String defStr = defaultHover.getHoverInfo(textViewer, hoverRegion);
        if (defStr != null) {
            XStyledString xss = new XStyledString(resourceRegistry);
            xss.append(defStr);
            hoverInfo = new HoverInfoWithText(xss, false);
        }
        
        if (pstLeafNode != null) {
            if (hoverInfo == null) {
                IModulaSymbol symbol = ModulaEditorSymbolUtils.getModulaSymbolExt(textViewer.getDocument(), pstLeafNode);
                if (symbol != null) {
                    IModuleSymbol editedModule = ModulaSymbolUtils.getHostModule(pstLeafNode);
                    hoverInfo = getSymbolHoverInfo(project, symbol, editedModule, textViewer.getDocument());
                }
            }
            
            if (hoverInfo == null) {
                hoverInfo = getPragmaHoverInfo(pstLeafNode, textViewer.getDocument());
            }

        }

        return hoverInfo;
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
    	return EditorTextUtils.findWord(textViewer.getDocument(), offset);
    }

    /**
     * Returns the information which should be presented when a hover popup is shown
     * for the specified symbol.
     * 
     * @param p - project of the IFile being processed in the editor for which to compute this hovering information. Can be null for the non-workspace elements.
     * @param symbol the symbol which is used to determine the hover display information.
     * @param module the currently edited module
     * @param document the document this hover info applies to
     * 
     * @return the hover popup display information, or <code>null</code> if none available.
     */
    protected IHoverInfo getSymbolHoverInfo(IProject project, IModulaSymbol symbol, IModuleSymbol module 
                                           , IDocument document ) 
    {
        if (symbol instanceof IForwardTypeSymbol) {
            IModulaSymbol actualTypeSymbol = ((IForwardTypeSymbol)symbol).getActualTypeSymbol();
            if (actualTypeSymbol != null) {
                symbol = actualTypeSymbol;
            }
        }

        IHoverInfo hoverInfo = getSymbolSourceTextHover(project, symbol, module, document);
        
        if (hoverInfo == null) {
            hoverInfo = getSymbolOutlineHover(symbol);
        }
        
        return hoverInfo;
    }
    
    private IDocument getDocumentWithSymbol(IProject project,IModulaSymbol symbol) {
        IDocument document = null;
        IFile symbolFile = ModulaSymbolUtils.findFileForSymbol(project, symbol);
        if (symbolFile != null && !symbolFile.getName().toLowerCase().endsWith(".sym")) { // //$NON-NLS-1$
            document = ModulaSearchUtils.evalNonFileBufferDocument(symbolFile);
            if (document == null) { 
                // not contained in changed editors => get it from file:
                document = ModulaSearchUtils.getDocumentForIFile(symbolFile, null);
            }
        }
        return document;
    }

    /**
     * Creates hover with source text where the symbol is defined if possible
     * 
     * @param symbol the symbol which is used to determine the hover display information.
     * @param module the currently edited module
     * 
     * @return the hover popup display information, or <code>null</code> if none available.
     */
    protected IHoverInfo getSymbolSourceTextHover(IProject project, IModulaSymbol symbol
                                                    , IModuleSymbol module
                                                    , IDocument document ) 
    {
        if (!ModulaSymbolUtils.isSymbolFromModule(module, symbol)) {
            document = getDocumentWithSymbol(project, symbol);
        }
        
        if (symbol instanceof IProcedureDefinitionSymbol) {
            // Try to show procedure body for procedure declarations (in FORWARDs or .def files)
            IModulaSymbol decl = ((IProcedureDefinitionSymbol)symbol).getDeclarationSymbol();
            if (decl != null) {
                symbol = decl;
                if (!ModulaSymbolUtils.isSymbolFromModule(module, symbol)) {
                    document = getDocumentWithSymbol(project, symbol);
                }
            }
        }
        
        ITextRegion textRegion = symbol.getDeclarationTextRegion();

        int nameOffs = -1; // symbol name inside
        int nameLen = 0;   //   hover text
        {
            ITextRegion nameRegion = symbol.getNameTextRegion();
            if (nameRegion != null && textRegion != null) {
                nameOffs = nameRegion.getOffset() - textRegion.getOffset(); 
                nameLen = nameRegion.getLength();
            }
        }
        
        if ((document != null) && (textRegion != null)) {
            int startLine;
            try {
                startLine = document.getLineOfOffset(textRegion.getOffset());
                int offset = document.getLineOffset(startLine);
                int addLen = (textRegion.getOffset() - offset);
                int length = textRegion.getLength() + addLen;
                nameOffs += addLen;
                
                String dots = "";
                { // trim text if it is longer than MAX_LINES_FROM_SOURCE_IN_HOVER lines:
                    int topLine = startLine + MAX_LINES_FROM_SOURCE_IN_HOVER;
                    if (topLine < document.getNumberOfLines()) {
                        int topOffs = document.getLineOffset(topLine);
                        if (topOffs < offset + length) {
                            length = topOffs - offset;
                            dots = "\n....."; // //$NON-NLS-1$
                        }
                    }
                }
                
                String text = document.get(offset, length);
                

                // Add "VAR ...\r\n" before if need:
                {
                    ModulaTokenType declWord = null;
                    if (symbol instanceof IVariableSymbol) {
                        declWord = ModulaTokenTypes.VAR_KEYWORD;
                    } else if (symbol instanceof ITypeSymbol) {
                        declWord = ModulaTokenTypes.TYPE_KEYWORD;
                    } else if (symbol instanceof IConstantSymbol) {
                        declWord = ModulaTokenTypes.CONST_KEYWORD;
                    }
                    
                    if (declWord != null) {
                        _XdsFlexScanner input = new _XdsFlexScanner(); 
                        input.reset(text);
                        TokenType tok = null;
                        while (true) {
                            tok = input.nextToken();
                            if (ModulaTokenTypes.EOF == tok) {
                                break;
                            } else if (ModulaTokenTypes.WHITE_SPACE == tok
                                    || ModulaTokenTypes.BLOCK_COMMENT == tok
                                    || ModulaTokenTypes.END_OF_LINE_COMMENT == tok)
                            {
                                continue;
                            } else if (declWord == tok) {
                                declWord = null; // 'VAR' found, don't add it
                                break;
                            }
                        }
                    }
                    
                    if (declWord != null) {
                        String s = declWord.getDesignator() + "\r\n"; //$NON-NLS-1$
                        text = s + text;
                        nameOffs += s.length();
                    }
                }

                XStyledString xssHover;
                ModulaSyntaxColorer fc = new ModulaSyntaxColorer(resourceRegistry);
                if (nameOffs >=0 && nameOffs + nameLen <= text.length() && 
                    symbol.getName().equals(text.substring(nameOffs,  nameOffs+nameLen)))
                {
                    // Highlight background under symbol. It is difficult to merge color ranges so do it:
                    xssHover = fc.color(text.substring(0, nameOffs), null); // "VAR "
                    
                    XStyledString xss = fc.color(symbol.getName(), null);   // "varName"
                    List<StyleRange> lsr = xss.getStyleRanges();
                    if (lsr.size() == 1) {
                        StyleRange sr = lsr.get(0);
                        sr.background = new Color(Display.getDefault(), 212, 212, 212);
                    }
                    xssHover.append(xss);
                    
                    xssHover.append(fc.color(text.substring(nameOffs + nameLen), null)); // " : Type;"
                } else {
                    xssHover = fc.color(text, null);
                    nameOffs = -1;
                }
                xssHover.append(dots);

                return new HoverInfoWithText(xssHover, true, nameOffs);
            } catch (Exception e) {
                LogHelper.logError(e);
            }
        }
        return null;
    }
    
    
    /**
     * Create hover with symbol type description (as in outline view) 
     */
    protected IHoverInfo getSymbolOutlineHover(IModulaSymbol symbol) {
        Image icon; 
        String description;

        if (symbol instanceof IModuleAliasSymbol) {
            IModuleSymbol moduleSymbol = ((IModuleAliasSymbol)symbol).getReference();
            if (moduleSymbol == null) {
                return null;
            }
            description = " := " + moduleSymbol.getName();
            icon = ModulaSymbolImages.getImage(moduleSymbol);
        } else {
            description = ModulaSymbolDescriptions.getSymbolDescription(symbol);
            icon = ModulaSymbolImages.getImage(symbol); 
        }

        XStyledString xss = new XStyledString(resourceRegistry);
        if (icon != null) {
            xss.append(" ");
            xss.append(icon);
            xss.append("  ");
        }
        xss.append(symbol.getName(), SWT.NONE);
        xss.append(description, fDecorationColor);
        xss.append(" ");
        return new HoverInfoWithText(xss, false);
    }

    
    /**
     * Check if pstLeafNode is part of pragma structure and create pragma structure hover in this case
     */
    protected IHoverInfo getPragmaHoverInfo(PstLeafNode pstLeafNode, IDocument document) {
        IHoverInfo hoverInfo = null;
        try {
            PstNode pragmaStmt = null;
            for(PstNode n = pstLeafNode.getParent(); n != null; n = n.getParent()) {
                IElementType et = n.getElementType();
                if (ModulaElementTypes.PRAGMA_IF_STATEMENT.equals(et) ||
                    ModulaElementTypes.PRAGMA_ELSIF_STATEMENT.equals(et) ||
                    ModulaElementTypes.PRAGMA_ELSE_STATEMENT.equals(et) ||
                    ModulaElementTypes.PRAGMA_END_STATEMENT.equals(et)) 
                {
                    pragmaStmt = n;
                    break;
                }
            }
            
            IAstFrameNode frameNode = null;
            
            if (pragmaStmt instanceof IAstFrameNode) {
                frameNode = (IAstFrameNode)pragmaStmt;
            } else if (pragmaStmt instanceof IAstFrameChild) {
                frameNode = ((IAstFrameChild)pragmaStmt).getAstFrameNode();
            }
            
            if (frameNode != null) {
                ArrayList<PstNode> alFrame = new ArrayList<PstNode>(frameNode.getFrameNodes());
                Collections.sort(alFrame, new Comparator<PstNode>() {
                    public int compare(PstNode n1, PstNode n2) {
                        return n2.getOffset() - n2.getOffset();
                    }
                });
    
                StringBuilder sb = new StringBuilder();
                int lastEnd = -1;
                for (PstNode n : alFrame) {
                    List<PstNode> children = ((PstCompositeNode)n.getParent()).getChildren();
                    int offs = -1;
                    int end = -1;
                    for (PstNode child : children) {
                        IElementType et = child.getElementType();
                        if (PragmaTokenTypes.PRAGMA_BEGIN.equals(et) && offs<0) {
                            offs = child.getOffset();
                        } else if (PragmaTokenTypes.PRAGMA_END.equals(et) && offs>=0 && end < 0) {
                            end = child.getOffset() + child.getLength();
                            break;
                        }
                    }
                    if (offs >= 0 && end > offs) {
                        if (offs >= lastEnd) { // to don't make duplicates for IF & THEN in <* IF zz THEN *>
                            if (sb.length() > 0) {
                                sb.append("\r\n");
                            }
                            sb.append(document.get(offs, end-offs));
                        }
                        lastEnd = end;
                    } else {
                        throw new Exception(); // bad tree - can't make hover
                    }
                }
                
                if (sb.length() > 0) {
                	try(ResourceRegistry resourceRegistry = new ResourceRegistry()){
                		ModulaSyntaxColorer colorer = new ModulaSyntaxColorer(resourceRegistry);
                		XStyledString xss = colorer.color(sb.toString());
                		hoverInfo = new HoverInfoWithText(xss, true);   
                	}
                }
            }
        } catch (Exception e) {} // hs what may crash on bad tree
        
        return hoverInfo;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IInformationControlCreator getHoverControlCreator() {
    	if (lastReturnedHover instanceof ITextHoverExtension) {
    		return ((ITextHoverExtension) lastReturnedHover).getHoverControlCreator();
    	} else {
    		return new IInformationControlCreator() {
    			public IInformationControl createInformationControl(Shell parent) {
    				return new HoverInformationControl(parent, EditorsUI.getTooltipAffordanceString());
    			}
    		};
    	}
    }
    
}

