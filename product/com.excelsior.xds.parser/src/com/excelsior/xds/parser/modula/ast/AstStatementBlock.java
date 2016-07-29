package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.commons.ast.AstNode;
import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;

/**
 * Base class for the block of statements. 
 * It usually includes list of statements as a child element.
 */
public abstract class AstStatementBlock extends ModulaAstNode 
{
    protected AstStatementBlock(PstCompositeNode parent, ElementType elementType) {
        super(parent, elementType);
    }

    public PstCompositeNode getStatementList() {
        return findFirstChild(ModulaElementTypes.STATEMENT_LIST, PstCompositeNode.class);
    }

    protected void visitStatementList(ModulaAstVisitor visitor) {
        visitAstChidren(getStatementList(), AstNode.class, visitor);
    }
    
}
