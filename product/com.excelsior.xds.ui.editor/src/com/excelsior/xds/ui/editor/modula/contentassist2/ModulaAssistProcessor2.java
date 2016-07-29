package com.excelsior.xds.ui.editor.modula.contentassist2;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.utils.JavaUtils;
import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.commons.symbol.ISymbol;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;
import com.excelsior.xds.parser.modula.symbol.IModuleAliasSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithType;
import com.excelsior.xds.parser.modula.symbol.type.IPointerTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;
import com.excelsior.xds.ui.editor.commons.contentassist.EmptyCompletionProposal;
import com.excelsior.xds.ui.editor.commons.contentassist.SourceCodeAssistProcessor;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.modula.IModulaPartitions;
import com.excelsior.xds.ui.editor.modula.ModulaContentAssistant;
import com.excelsior.xds.ui.editor.modula.facade.ActiveEditorFacade;
import com.excelsior.xds.ui.editor.modula.utils.ModulaEditorSymbolUtils;
import com.excelsior.xds.ui.images.ImageUtils;

/**
 * Content assist processor to suggest Modula-2 source code completions.
 */
public class ModulaAssistProcessor2 extends SourceCodeAssistProcessor<CompletionContext> 
{
	private static final Image LOADING_IMAGE = ImageUtils.getImage(ImageUtils.OBJ_LOADING_16x16);
    static private final char[] PROPOSAL_ACTIVATION_CHARS = new char[] { '.' }; 
    
    public ModulaAssistProcessor2(ModulaContentAssistant contentAssistant) {
		super(null, new ActiveCodeContentAssistProcessor(contentAssistant),
				new ModulaTemplateCompletionProcessor(contentAssistant));
    }
    
    /**
     * {@inheritDoc}
     */
    public char[] getCompletionProposalAutoActivationCharacters() {
        return PROPOSAL_ACTIVATION_CHARS;
    }

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		CompletionContext context = computeContext(viewer, offset);
		List<ICompletionProposal> completionProposals = new ArrayList<ICompletionProposal>();
		doComputeCompletionProposals(context, completionProposals);
		
		if (completionProposals.isEmpty() /*&& !isAstUpToDate()*/) {
			IModuleSymbol moduleSymbol = context.getModuleSymbol();
			if (moduleSymbol == null) {
				EmptyCompletionProposal emptyCompletionProposal = new EmptyCompletionProposal(Messages.XdsOutlinePage_Loading, 
						LOADING_IMAGE, viewer.getSelectedRange().x);
				return new ICompletionProposal[]{emptyCompletionProposal, emptyCompletionProposal};
			}
			else {
				return null;
			}
		}
		return completionProposals.toArray(new ICompletionProposal[0]);
	}
	
	private static CompletionContext computeContext(ITextViewer viewer,
			int offset) {
		CompletionContext context = new CompletionContext(viewer, offset);
		
		IFile editedFile = CoreEditorUtils.getActiveEditorInputAsIFile();
		
		context.setEditedFile(editedFile);
		IModuleSymbol moduleSymbol = ModulaEditorSymbolUtils.getModuleSymbol(editedFile);
		context.setModuleSymbol(moduleSymbol);
		try{
			IDocument doc = viewer.getDocument();
			
			String contentType = TextUtilities.getContentType(doc, IModulaPartitions.M2_PARTITIONING, offset, true);
			context.setContentType(contentType);
			
			IRegion lineRegion = guessStatement(doc, offset);
			String line = doc.get(lineRegion.getOffset(), lineRegion.getLength());
			context.setCurrentStatement(line);
			context.setStatementRegion(lineRegion);
			
			ModulaAst ast = ActiveEditorFacade.getAst();
			PstLeafNode leafNode = ModulaEditorSymbolUtils.getPstLeafNode(ast, offset);
			context.setLeafNode(leafNode);
			
			List<Token> tokens = ContentAssistUtils.tokenize(line);
			context.setTokens(tokens);
            int tokenIdx = containingTokenIdx(doc, tokens, lineRegion, offset);
            if (tokenIdx > -1 && tokens.get(tokenIdx).tokenType == ModulaTokenTypes.DOT) {
            	context.setDotBeforeCursor(true);
            }
            context.setDottedExpression(isDottedExpression(tokens, tokenIdx));
            
            int identifierBeforeCursorOffset = 0;
            if (context.isDotBeforeCursor()) {
            	tokenIdx--;
            	if (tokenIdx > -1 && tokens.size() > 0) {
            		Token t = tokens.get(tokenIdx);
            		String text = ContentAssistUtils.region(line, t);
            		if (isIdentifier(text)) {
            			identifierBeforeCursorOffset = lineRegion.getOffset() + t.offset;
            		}
            	}
            }
            else {
            	if (tokenIdx > -1 && tokens.size() > 0) {
            		Token t = tokens.get(tokenIdx);
            		String text = ContentAssistUtils.region(line, t);
            		if (isIdentifier(text)) {
            			context.setBeforeCursorWordPart(text);
            			identifierBeforeCursorOffset = lineRegion.getOffset() + t.offset;
            		}
            	}
            }
            
            {
            	PstLeafNode ln = context.getLeafNode(); 
            	context.setRegionType(determineRegionType(doc, tokens, ast, ln, context.isDotBeforeCursor() ? identifierBeforeCursorOffset : offset));
            	IModulaSymbol symbol = ModulaEditorSymbolUtils.getModulaSymbol(doc, ln);
    			if (symbol == null) {
    				if (context.isDottedExpression() && !context.isDotBeforeCursor()) {
    					tokenIdx--;
    				}
    				symbol = SymbolResolver.resolve(context, doc, line, ModulaSymbolUtils.getParentScope(ln), tokens, tokenIdx);
    			}
    			else {
    				symbol = SymbolResolver.getTargetSymbol(symbol);
    			}
            	context.setReferencedSymbol(symbol);
            }
            
            {
            	Point selectedRange = viewer.getSelectedRange();
            	int replacementOffset = context.getBeforeCursorWordPart() == null ? offset : identifierBeforeCursorOffset;
            	int replacementLength =  selectedRange.x + selectedRange.y - replacementOffset; 
            	context.setReplacementOffset(replacementOffset);
            	context.setReplacementLength(replacementLength);
            	context.setCurrentLineTail(currentLineTail(doc, selectedRange));
            }
		}
		catch(BadLocationException e) {
			// ignore it
		}
		
        return context;
	}
	
	private static IRegion guessStatement(IDocument doc, int offset) {
		IRegion region;
		try {
			region = doc.getLineInformationOfOffset(offset);
			if (doc.getChar(offset) == ';') {
				--offset;
			}
			
			int toOffset = offset;
			
			while (offset > -1 && doc.getChar(offset) != ';') {
				--offset;
			}
			offset++;
			
			while (toOffset < doc.getLength() && doc.getChar(toOffset) != ';') {
				++toOffset;
			}
			
			region = new Region(offset, toOffset - offset);
		} catch (BadLocationException e) {
			region = new Region(offset, 1);
		}
		
		return region;
	}
	
	private static RegionType determineRegionType(IDocument doc, List<Token> tokens, ModulaAst ast, PstNode currentNode, int offset) throws BadLocationException {
		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			if (token.tokenType == ModulaTokenTypes.IMPORT_KEYWORD || 
					token.tokenType == ModulaTokenTypes.FROM_KEYWORD) {
				return RegionType.IMPORT_STATEMENT;
			}
			else if (!ContentAssistUtils.isWhitespaceCommentOrPragma(token) && token.tokenType != ModulaTokenTypes.END_KEYWORD) {
				break;
			}
		}
		int lineNo = doc.getLineOfOffset(offset);
		return determineRegionType(pstAtLine(ast, doc, lineNo - 1));
	}
	
	private static PstNode pstAtLine(ModulaAst ast, IDocument doc, int lineNo) throws BadLocationException {
		if (lineNo < 0 || lineNo > doc.getNumberOfLines()) {
			return null;
		}
		
		int len = doc.getLineLength(lineNo);
		return ModulaEditorSymbolUtils.getPstLeafNode(ast, doc.getLineOffset(lineNo) + (len / 2));
	}
	
	private static String currentLineTail(IDocument doc, Point selectedRange) {
		String lineTail = StringUtils.EMPTY;
		try {
            int lastPos = selectedRange.x + selectedRange.y;
            int lastLnum = doc.getLineOfOffset(lastPos);
            IRegion lastReg = doc.getLineInformation(lastLnum);
            String lastLine = doc.get(lastReg.getOffset(), lastReg.getLength());
            lineTail = lastLine.substring(lastPos - lastReg.getOffset());
        } catch (BadLocationException e) {
        }
		return lineTail.trim();
	}
	
	private static RegionType determineRegionType(PstNode n) {
		RegionType type = RegionType.UNKNOWN;
		if (n == null) {
			return RegionType.UNKNOWN;
		}
		
		while(n != null) {
			if (n.getElementType() == ModulaElementTypes.LOCAL_MODULE ||
				n.getElementType() == ModulaElementTypes.DEFINITION_MODULE || 
				n.getElementType() == ModulaElementTypes.PROGRAM_MODULE) {
				type = RegionType.MODULE;
				break;
			}
			else if (n.getElementType() == ModulaElementTypes.VARIABLE_DECLARATION_BLOCK || n.getElementType() == ModulaElementTypes.PROCEDURE_DECLARATION) {
				type = RegionType.DECLARATIONS;
				break;
			}
			else if (n.getElementType() == ModulaElementTypes.PROCEDURE_BODY) {
				type = RegionType.PROCEDURE_BODY;
				break;
			}
			else if (n.getElementType() == ModulaElementTypes.MODULE_BODY) {
				type = RegionType.PROCEDURE_BODY;
				break;
			}
			else if (n.getElementType() == ModulaElementTypes.FORMAL_PARAMETER_BLOCK) {
				type = RegionType.PROCEDURE_PARAMETERS;
			}
			else if (n.getElementType() == ModulaElementTypes.SIMPLE_IMPORT || n.getElementType() == ModulaElementTypes.UNQUALIFIED_IMPORT) {
				type = RegionType.IMPORT_STATEMENT;
				break;
			}
			n = n.getParent();
		}
		
		return type;
	}
	
	private static boolean isDottedExpression(List<Token> tokens, int tokenIdx) {
		int i = tokenIdx;
		
		boolean isDottedExpression = false;
		
		while(i > -1) {
			Token t = tokens.get(i);
			if (t.tokenType != ModulaTokenTypes.DOT && t.tokenType != ModulaTokenTypes.IDENTIFIER) {
				break;
			}
			if (t.tokenType == ModulaTokenTypes.DOT) {
				isDottedExpression = true;
				break;
			}
			--i;
		}
		
		return isDottedExpression;
	}
	
	private static int containingTokenIdx(IDocument doc,  List<Token> tokens, IRegion lineRegion, int offset) throws BadLocationException {
		int offsetInLine = offset - lineRegion.getOffset();
		
		int i = tokens.size() - 1;
		for (; i > -1; --i) {
			Token t = tokens.get(i);
			if (t.contains(offsetInLine - 1)) {
				break;
			}
		}
		
		return i;
	}
	
	static IModulaSymbolScope getScope(ISymbol symbol){
		if (symbol instanceof IModuleAliasSymbol) {
			symbol = ((IModuleAliasSymbol)symbol).getReference();
        }
		if (symbol instanceof IModulaSymbolScope) {
			return (IModulaSymbolScope)symbol;
		}
		
		symbol = getTypeSymbol(symbol);
		
		if (symbol instanceof IModulaSymbolScope) {
			return (IModulaSymbolScope)symbol;
		}
		
		return null;
	}

	static ITypeSymbol getTypeSymbol(ISymbol symbol) {
		ITypeSymbol typeSymbol;
		if (symbol instanceof ITypeSymbol) {
			typeSymbol = (ITypeSymbol)symbol;
		}
		else {
			ISymbolWithType withTypeSymbol = JavaUtils.as(ISymbolWithType.class, symbol);
			typeSymbol = withTypeSymbol != null? withTypeSymbol.getTypeSymbol() : null;
		}
		
		ITypeSymbol result;
		
		if (typeSymbol instanceof IPointerTypeSymbol) {
			IPointerTypeSymbol pointerSymbol = (IPointerTypeSymbol)typeSymbol;
			result = pointerSymbol.getBoundTypeSymbol();
		}
		else {
			result = typeSymbol;
		}
		return result;
	}
	
	private static boolean isIdentifier(String text) {
        boolean first = true;
        for (int i = 0; i < text.length(); i++) {
        	char ch = Character.toUpperCase(text.charAt(i));
            if (first) {
                if (!(ch == '_' || (ch >= 'A' && ch <= 'Z'))) {
                    return false;
                }
                first = false;
            } else {
                if (!(ch == '_' || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9'))) {
                    return false;
                }
            }
		}
        return true;
    }
}

