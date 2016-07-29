package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.commons.ast.AstNode;
import com.excelsior.xds.parser.commons.ast.AstVisitor;
import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;

/**
 * A node in the Modula-2/Oberon-2 AST tree. 
 */
public abstract class ModulaAstNode extends AstNode
{
    protected ModulaAstNode(PstCompositeNode parent, ElementType elementType) {
        super(parent, elementType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(AstVisitor visitor) {
        if (visitor instanceof ModulaAstVisitor) {
            doAccept((ModulaAstVisitor)visitor);
        }
    }
    
    protected void doAccept(ModulaAstVisitor visitor) {
    }
    
}
