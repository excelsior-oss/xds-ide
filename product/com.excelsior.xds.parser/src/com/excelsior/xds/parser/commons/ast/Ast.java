package com.excelsior.xds.parser.commons.ast;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.utils.time.ModificationStamp;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.commons.pst.PstVisitor;

/**
 * A root element of the AST
 */
public class Ast extends PstCompositeNode {

    /** Buffer containing the text of this AST */
    private CharSequence chars;
    
    /** The source code file for which this AST is created */
    private IFileStore sourceFile;
    
    /** Modification stamp of AST creation */
    private final ModificationStamp modificationStamp = new ModificationStamp();
    
    
    public Ast(CharSequence chars, IFileStore sourceFile) {
        super(ElementTypes.AST_ROOT);
        this.chars = chars;
        this.sourceFile = sourceFile;
    }
    
    /** 
     * Returns creation modification stamp of this AST.
     *   
     * @return creation modification stamp of this AST.
     */
    public ModificationStamp getModificationStamp() {
        return modificationStamp;
    }

    /**
     * Returns char sequence which is corresponding to the source file of this AST.
     * File read operation may be invoked.
     *  
     * @return char sequence which is corresponding to the source file of this AST
     * @throws CoreException 
     */
    public CharSequence getChars() {
        if (chars == null && sourceFile != null) {
            try {
				chars = ResourceUtils.toString(sourceFile);
			} catch (CoreException e) {
				LogHelper.logError(e);
			}
        }
        return chars;
    }

    public IFileStore getSourceFile() {
        return sourceFile;
    }
    
    public AstNode getAstNode() {
        return (AstNode) getChildren().get(0);
    }

    /**
     * Accepts the given visitor on a visit of the AST root node.
     *
     * @param visitor the visitor object
     * @exception IllegalArgumentException if the visitor is null
     */
    public void accept(AstVisitor visitor) {
        AstNode node = getAstNode();
        if (node != null) {
            node.doAccept(visitor);
        }
    }

    /**
     * Minimizes the storage an <tt>AST</tt> instance (including its child items).
     */
    public void trim() {
        AstNode node = getAstNode();
        if (node != null) {
            node.accept(new PstVisitor() {
                public boolean visit(PstLeafNode node) {
                    return false;
                }

                public void endVisit(PstCompositeNode node) {
                    node.trim();
                }
            });
        }
    }
    
}
