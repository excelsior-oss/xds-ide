package com.excelsior.xds.ui.editor.modula.utils;

import java.util.HashSet;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.ide.utils.ParsedModuleKeyUtils;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.parser.commons.ast.Ast;
import com.excelsior.xds.parser.commons.ast.IElementType;
import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.XdsParserManager;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.ast.modules.AstDefinitionModule;
import com.excelsior.xds.parser.modula.ast.modules.AstLocalModule;
import com.excelsior.xds.parser.modula.ast.modules.AstModuleBody;
import com.excelsior.xds.parser.modula.ast.modules.AstProgramModule;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureBody;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureDeclaration;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureDefinition;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureForwardDeclaration;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.binding.ModulaSymbolCache;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;

public class ModulaEditorSymbolUtils 
{
    private static final HashSet<IElementType> hsKeywordsToGetParent = new HashSet<IElementType>();
    static {
        hsKeywordsToGetParent.add(ModulaTokenTypes.END_KEYWORD);
        hsKeywordsToGetParent.add(ModulaTokenTypes.PROCEDURE_KEYWORD);
        hsKeywordsToGetParent.add(ModulaTokenTypes.IMPLEMENTATION_KEYWORD);
        hsKeywordsToGetParent.add(ModulaTokenTypes.DEFINITION_KEYWORD);
        hsKeywordsToGetParent.add(ModulaTokenTypes.FORWARD_KEYWORD);
        hsKeywordsToGetParent.add(ModulaTokenTypes.MODULE_KEYWORD);
    }

	/**
     * Returns the <code>IModulaSymbol</code> corresponding to the given <code>pstLeafNode</code>.
     *
     * @param pstLeafNode, the ModulaAST leaf node for which the request has been issued.
     * @param document, the document to operate on.
     * @return the <code>IModulaSymbol</code> at the given offset or 
     *         <code>null</code> if none available.
     */
    public static IModulaSymbol getModulaSymbol( IDocument document
                                               , PstLeafNode pstLeafNode ) 
    {
        if (pstLeafNode != null) {
            if (ModulaTokenTypes.IDENTIFIER == pstLeafNode.getToken()) {
                IModulaSymbol symbol = ModulaSymbolUtils.getSymbol(pstLeafNode.getParent());
                if (symbol != null) {
                    try {
                        String regionText = document.get(pstLeafNode.getOffset(), pstLeafNode.getLength());
                        if (symbol.getName().equals(regionText)) {
                            return symbol;
                        }
                    } catch (BadLocationException e) {
                        LogHelper.logError(e);
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Same as getModulaSymbol() but for module and procedure BEGIN/END/etc. keyword returns corresponded symbol too
     */
    public static IModulaSymbol getModulaSymbolExt( IDocument document
                                                  , PstLeafNode pstLeafNode ) 
    {
        if (pstLeafNode == null) {
            return null;
        }
        
        PstNode p = null;
        IElementType et = pstLeafNode.getElementType();
        if (ModulaTokenTypes.BEGIN_KEYWORD.equals(et)) {
            p = pstLeafNode.getParent();
            if (p instanceof AstModuleBody || p instanceof AstProcedureBody) {
                p = p.getParent();
            } else {
                p = null;
            }
        }
        else if (hsKeywordsToGetParent.contains(et)) {
            p = pstLeafNode.getParent();
        }
        
        IModulaSymbol sym = null;
        if (p instanceof AstProgramModule) {
            sym = ((AstProgramModule)p).getSymbol();
        }
        else if (p instanceof AstLocalModule) {
            sym = ((AstLocalModule)p).getSymbol();
        }
        else if (p instanceof AstDefinitionModule) {
            sym = ((AstDefinitionModule)p).getSymbol();
        }
        else if (p instanceof AstProcedureDeclaration) {
            sym = ((AstProcedureDeclaration)p).getSymbol();
        }
        else if (p instanceof AstProcedureDefinition) {
            sym = ((AstProcedureDefinition)p).getSymbol();
        }
        else if (p instanceof AstProcedureForwardDeclaration) {
            sym = ((AstProcedureForwardDeclaration)p).getSymbol();
        }
    
        if (sym == null) {
            sym = getModulaSymbol(document, pstLeafNode); 
        }
        
        return sym;
    }

    /**
     * Returns the <code>PstLeafeNode</code> at the given offset. Works correctly when offset points to the position 
     * immediately following the identifier token
     * @param editor
     * @param offset
     * @return
     */
    public static PstLeafNode getIdentifierPstLeafNode(ITextEditor editor, int offset) {
    	PstLeafNode pstLeafNode = getPstLeafNode(editor, offset);
        if (pstLeafNode != null) {
        	if (pstLeafNode.getElementType() != ModulaTokenTypes.IDENTIFIER) {
        		pstLeafNode = getPstLeafNode(editor, offset - 1);
        	}
        }
        return pstLeafNode;
    }
    
    public static PstLeafNode getIdentifierPstLeafNode(Ast ast, int offset) {
    	PstLeafNode pstLeafNode = getPstLeafNode(ast, offset);
        if (pstLeafNode != null) {
        	if (pstLeafNode.getElementType() != ModulaTokenTypes.IDENTIFIER) {
        		pstLeafNode = getPstLeafNode(ast, offset - 1);
        	}
        }
        return pstLeafNode;
    }

    
    /**
     * Returns the <code>PstLeafeNode</code> at the given offset in the editor.
     *
     * @param editor, the text editor to operate on.
     * @param offset, the offset for which the request has been issued.
     * @return the <code>PstLeafeNode</code> at the given offset or 
     *         <code>null</code> if none available.
     */
    public static PstLeafNode getPstLeafNode(ITextEditor editor, int offset) {
        PstLeafNode pstLeafNode = null;
        if (editor != null) {
            IEditorInput input = editor.getEditorInput();
            ModulaAst ast = getModulaAst(input);
            pstLeafNode = getPstLeafNode(ast, offset);
        }
        return pstLeafNode;
    }

    public static PstLeafNode getPstLeafNode(Ast ast, int offset) {
        PstLeafNode pstLeafNode = null;
        if (ast != null) {
            PstNode pstNode = ast.getPstNodeAt(offset);
            if (pstNode instanceof PstLeafNode) {
                pstLeafNode = (PstLeafNode) pstNode;
            }
        }
        return pstLeafNode;
    }    
    
    /**
     * Returns the <code>PstLeafeNode</code> at the given offset.
     *
     * @param file containing the source code
     * @param offset the offset for which the hover request has been issued
     * @return the <code>PstLeafeNode</code> at the given offset or 
     *         <code>null</code> if none available.
     */
    public static PstLeafNode getPstLeafNode(IFile file, int offset) {
    	PstLeafNode pstLeafNode = null;
    	ModulaAst ast = getModulaAst(file);
        if (ast != null) {
            pstLeafNode = getPstLeafNode(ast, offset);
        }
        return pstLeafNode;
    }

    public static ModulaAst getModulaAst(IFile iFile) {
        return XdsParserManager.getModulaAst(ParsedModuleKeyUtils.create(iFile));
    }
    
    public static IModuleSymbol getModuleSymbol(IFile iFile){
    	return ModulaSymbolCache.instance().getModuleSymbol(ParsedModuleKeyUtils.create(iFile));
    }

    public static ModulaAst getModulaAst(IEditorInput input) {
        if (input == null) {
            return null;
        }
        IProject project = CoreEditorUtils.getIProjectFrom(input);
        IFileStore store = CoreEditorUtils.editorInputToFileStore(input);
        return XdsParserManager.getModulaAst(ParsedModuleKeyUtils.create(project, store));
    }
    
}
