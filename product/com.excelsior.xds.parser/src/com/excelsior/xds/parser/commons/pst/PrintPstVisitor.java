package com.excelsior.xds.parser.commons.pst;

import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;

public class PrintPstVisitor extends PstVisitor implements ModulaTokenTypes {
    
    private int nestingLevel = 0;
//    private CharSequence input;
    
    @Override
    public boolean visit(PstLeafNode node) {
        printIndent();
        printNode(node);
        return super.visit(node);
    }

    @Override
    public boolean visit(PstCompositeNode node) {
        printIndent();
        printNode(node);
        nestingLevel++;
        return super.visit(node);
    }

    @Override
    public void endVisit(PstCompositeNode node) {
        nestingLevel--;
        super.visit(node);
    }

    private void printIndent() {
        for (int i = 0; i < nestingLevel; i++) {
            System.out.print("    ");
        }
    }
    
    private void printNode(PstNode node) {
        System.out.println(node + "  \toffs=" + node.getOffset() + "  len=" + node.getLength());
    }

}
