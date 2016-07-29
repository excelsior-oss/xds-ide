package com.excelsior.xds.parser.commons.ast;

public abstract class AstVisitor {

    /**
     * Visits the given AST node prior to the type-specific visit (before <code>visit</code>).
     * <p>
     * </p>
     *
     * @param node the node to visit
     * @return <code>true</code> if <code>visit(node)</code> should be called,
     * and <code>false</code> otherwise.
     */
    public boolean preVisit(AstNode node) {
        return true;
    }
    
    /**
     * Visits the given AST node following the type-specific visit
     * (after <code>endVisit</code>).
     * <p>
     * The default implementation does nothing.
     * </p>
     *
     * @param node the node to visit
     */
    public void postVisit(AstNode node) {
        // default implementation: do nothing
    }
    
}
