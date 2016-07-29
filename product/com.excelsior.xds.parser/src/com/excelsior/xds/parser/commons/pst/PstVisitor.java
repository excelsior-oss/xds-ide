package com.excelsior.xds.parser.commons.pst;

public abstract class PstVisitor {
    
    public boolean visit(PstLeafNode node) {
        return true;
    }

    public boolean visit(PstCompositeNode node) {
        return true;
    }

    public boolean preVisit(PstNode pstNode) {
        return true;
    }

    public void postVisit(PstNode pstNode) {
    }

    public void endVisit(PstCompositeNode node) {
    }
    
}
