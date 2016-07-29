package com.excelsior.xds.ui.editor.modula.utils;

import java.util.HashMap;
import com.excelsior.xds.core.utils.time.ModificationStamp;
import com.excelsior.xds.parser.commons.ast.Ast;
import com.excelsior.xds.parser.commons.ast.IAstFrameChild;
import com.excelsior.xds.parser.commons.ast.IAstFrameNode;
import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.IModulaElementType;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.ui.editor.commons.SourceCodeTextEditor;

public class ModulaAstUtils {
    
    /**
     * 
     * @param editor
     * @param ast may be null
     * @return true if the ast is up to date
     */
    public static boolean isAstUpToDate(SourceCodeTextEditor editor, Ast ast) {
    	if (editor == null) {
    		return false;
    	}
    	ModificationStamp tsEdt = editor.getLastDocumentChangeModificationStamp();
        if (tsEdt != null && ast != null) {
        	ModificationStamp tsAst = ast.getModificationStamp();
            return tsAst.isGreaterThan(tsEdt);
        }
        return false;
    }

    /**
     * For the given {@link SourceCodeTextEditor} returns ModulaAst if it exists 
     * and up to date or null.
     * @param editor
     * @return
     */
    public static ModulaAst getAstIfUpToDate(SourceCodeTextEditor editor) {
        ModulaAst ast = ModulaEditorSymbolUtils.getModulaAst(editor.getEditorInput());
        if (isAstUpToDate(editor, ast)) {
            return ast;
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////
    //
    // Finding language constructions:
    //
    ////////////////////////////////////////////////////////////////


    /**
     * When the leaf node in 'ast' at the given offset is a member of some construction frame
     * returns all this frame.
     * 
     * @param ast
     * @param offset
     * @param startNode (out) if not null, startNode[0] will receive leaf node was found at the offset   
     * @return
     */
    public static IAstFrameNode findConstruction(Ast ast, int offset, PstLeafNode startNode[]) {
        try {
            PstLeafNode pstLeafNode = ModulaEditorSymbolUtils.getPstLeafNode(ast, offset);
            if (pstLeafNode != null) {
                for (PstNode frameNode = pstLeafNode; frameNode != null; frameNode = frameNode.getParent()) {
                    if (frameNode instanceof IAstFrameNode) {
                        if (((IAstFrameNode)frameNode).getFrameNodes().contains(pstLeafNode)) {
                            if (startNode != null) {
                                startNode[0] = pstLeafNode;
                            }
                            return (IAstFrameNode)frameNode;
                        }
                    }
                }
                
                // Some [pragma] nodes are not inside its frames but its frame may be found via IAstFrameChild:
                IModulaElementType t = hmAstFrameChilds.get(pstLeafNode.getElementType());
                if (t != null) {
                    PstNode parent = pstLeafNode.getParent();
                    if (parent instanceof IAstFrameChild && t.equals(parent.getElementType())) {
                        if (startNode != null) {
                            startNode[0] = pstLeafNode;
                        }
                        return ((IAstFrameChild)parent).getAstFrameNode();
                    }
                }
            }
        } catch (Exception e) {} // hz, tree may be incorrect
        return null;
    }

    private static HashMap<TokenType, IModulaElementType> hmAstFrameChilds;
    static {
        hmAstFrameChilds = new HashMap<TokenType, IModulaElementType>();
        hmAstFrameChilds.put(ModulaTokenTypes.THEN_KEYWORD, ModulaElementTypes.PRAGMA_ELSIF_STATEMENT);
        hmAstFrameChilds.put(ModulaTokenTypes.ELSE_KEYWORD, ModulaElementTypes.PRAGMA_ELSE_STATEMENT);
        hmAstFrameChilds.put(ModulaTokenTypes.ELSIF_KEYWORD, ModulaElementTypes.PRAGMA_ELSIF_STATEMENT);
        hmAstFrameChilds.put(ModulaTokenTypes.END_KEYWORD, ModulaElementTypes.PRAGMA_END_STATEMENT);
    }
    
    
    /**
     * Try findConstruction() at the given offset and at offset-1 (cursor may be after word)
     */
    public static IAstFrameNode findConstructionNear(Ast ast, int offset, PstLeafNode startNode[]) {
        IAstFrameNode res = findConstruction(ast, offset, startNode);
        if (res == null && offset > 0) {
            res = findConstruction(ast, offset - 1, startNode); // cursor after word?
        }
        return res;
    }
}
