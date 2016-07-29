package com.excelsior.xds.ui.editor.modula.hyperlink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.parser.commons.ast.IAstFrameNode;
import com.excelsior.xds.parser.commons.ast.IElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureDeclarationSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureDefinitionSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IForwardTypeSymbol;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;
import com.excelsior.xds.ui.editor.modula.ModulaEditor;
import com.excelsior.xds.ui.editor.modula.utils.ModulaAstUtils;
import com.excelsior.xds.ui.editor.modula.utils.ModulaEditorSymbolUtils;

/**
 * Modula-2/Oberon-2 symbol declaration hyperlink detector.
 * 
 * NOTE: this method used by eclipse to show hyperlinks AND from OpenDeclarationsAction to process F3 key
 */
public class ModulaDeclarationHyperlinkDetector extends AbstractHyperlinkDetector
{
    private static final HashSet<IElementType> hsFramedNodesToSkip;
    static { // framed elements to don't jump on
        hsFramedNodesToSkip = new HashSet<IElementType>();
        hsFramedNodesToSkip.add(ModulaTokenTypes.BY_KEYWORD);
        hsFramedNodesToSkip.add(ModulaTokenTypes.DO_KEYWORD);
        hsFramedNodesToSkip.add(ModulaTokenTypes.OF_KEYWORD);
        hsFramedNodesToSkip.add(ModulaTokenTypes.RETURN_KEYWORD);
        hsFramedNodesToSkip.add(ModulaTokenTypes.THEN_KEYWORD);
        hsFramedNodesToSkip.add(ModulaTokenTypes.TO_KEYWORD);
        hsFramedNodesToSkip.add(ModulaTokenTypes.SEP);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IHyperlink[] detectHyperlinks( ITextViewer textViewer
                                        , IRegion region
                                        , boolean canShowMultipleHyperlinks )
    {
        ITextEditor editor = (ITextEditor)getAdapter(ITextEditor.class);
        if ((region == null) || !(editor instanceof ModulaEditor)) {
            return null;
        }

        int offset = region.getOffset();
        
        ModulaAst ast = ModulaAstUtils.getAstIfUpToDate((ModulaEditor)editor);
        if (ast == null) {
            return null; // No up to date ast :(
        }
        IProject project = CoreEditorUtils.getIProjectFrom(editor.getEditorInput());
        PstLeafNode pstLeafNode = ModulaEditorSymbolUtils.getPstLeafNode(ast, offset);
        IModulaSymbol symbol = ModulaEditorSymbolUtils.getModulaSymbol(textViewer.getDocument(), pstLeafNode);

        if (symbol == null && offset > 0) {
            // try to find symbol on the left of cursor position:
            pstLeafNode = ModulaEditorSymbolUtils.getPstLeafNode(editor, offset-1);
            if (pstLeafNode == null) {
                return null; // oops. bug in parser?
            }
            symbol = ModulaEditorSymbolUtils.getModulaSymbol(textViewer.getDocument(), pstLeafNode);
        }
        
        PstNode nodeToGo = null;
        PstNode nodeToJumpFrom = null;
        
        if (symbol != null) {
            if (symbol instanceof IForwardTypeSymbol) {
                symbol = ((IForwardTypeSymbol) symbol).getActualTypeSymbol();
            }
            else if (symbol instanceof IProcedureDefinitionSymbol) {
                IModulaSymbol declSymbol = ((IProcedureDefinitionSymbol)symbol).getDeclarationSymbol();
                if (declSymbol != null) {
                    symbol = declSymbol;
                }
            } else if (symbol instanceof IProcedureDeclarationSymbol) {
                boolean gotoDef = false;

                if (ModulaTokenTypes.IDENTIFIER == pstLeafNode.getToken()) {
                    // Determine if it is hit in "PROCEDURE ProcId()" line (not in "END ProcId;") - in this case 
                    // link should point to procedure definition in .def file if any
                    PstNode pnProcId = pstLeafNode.getParent();
                    if (pnProcId != null) {
                        PstNode pnProc = pnProcId.getParent();
                        boolean isProcedureDeclarationNode = (pnProc != null) && (
                            ModulaElementTypes.PROCEDURE_DECLARATION.equals(pnProc.getElementType()) ||
                            ModulaElementTypes.OBERON_METHOD_DECLARATION.equals(pnProc.getElementType())
                        );
                        if (isProcedureDeclarationNode) {
                            List<PstNode> children = ((PstCompositeNode)pnProc).getChildren();
                            for (PstNode child : children) {
                                if (child.getElementType().equals(ModulaElementTypes.PROCEDURE_IDENTIFIER)) {
                                    gotoDef = (child == pnProcId);
                                    break;
                                }
                            }
                        }
                    }
                }
                
                if (gotoDef) {
                    IModulaSymbol defSymbol = ((IProcedureDeclarationSymbol)symbol).getDefinitionSymbol();
                    if (defSymbol != null) {
                        symbol = defSymbol;
                    }
                }
            }
            
        } else { // if (symbol != null) .. else
            
            // Jump on construction frame?
            PstLeafNode startNode[] = new PstLeafNode[1];
            IAstFrameNode fr = ModulaAstUtils.findConstructionNear(ast, offset, startNode);
            if (fr != null) {
                List<PstNode> nodes = new ArrayList<PstNode>(fr.getFrameNodes());
                Collections.sort(nodes, new Comparator<PstNode>() {
                    @Override
                    public int compare(PstNode o1, PstNode o2) {
                        return o1.getOffset() - o2.getOffset();
                    }
                });
                
                int startIdx = nodes.indexOf(startNode[0]);
                if (startIdx >= 0) {
                    for (int i = 1; i < nodes.size(); ++i) {
                        PstNode n = nodes.get((startIdx + i) % nodes.size());
                        if (!hsFramedNodesToSkip.contains(n.getElementType())) {
                            nodeToGo = n;
                            nodeToJumpFrom = startNode[0];
                            break;
                        }
                    }
                }
            }
        }
        
        if ((symbol != null) && (symbol.getPosition() != null)) {
            IRegion tokenRegion = new Region(pstLeafNode.getOffset(), pstLeafNode.getLength());
            
            if (nodeToGo != null) {
                IFile ifile = (IFile)editor.getEditorInput().getAdapter(IFile.class);
                if (ifile != null) {
                    return new IHyperlink[] { 
                            new ModulaDeclarationHyperlink(nodeToGo, tokenRegion, ifile)  
                        };
                }
            } else {
                IFile efile = ModulaSymbolUtils.findFirstFileForSymbol(project, symbol);
                if (efile != null) {
                    return new IHyperlink[] { 
                        new ModulaDeclarationHyperlink(symbol, tokenRegion, efile)  
                    };
                }

                IFileStore file = ModulaSymbolUtils.getSourceFileStore(symbol);
                if (file != null) {
                    return new IHyperlink[] { 
                        new ModulaDeclarationHyperlink(symbol, tokenRegion, file)  
                    };
                }
            }
        } 
        else if (nodeToJumpFrom != null) {
            IRegion tokenRegion = new Region(nodeToJumpFrom.getOffset(), nodeToJumpFrom.getLength());
            IFile ifile = (IFile)editor.getEditorInput().getAdapter(IFile.class);
            if (ifile != null) {
                return new IHyperlink[] { 
                        new ModulaDeclarationHyperlink(nodeToGo, tokenRegion, ifile)  
                    };
            }
        }

        return null;
    }
    
}
