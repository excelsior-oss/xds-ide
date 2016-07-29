package com.excelsior.xds.parser.commons.pst;

import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;

public class PrintSourcePstVisitor extends PstVisitor implements ModulaTokenTypes {
    
    private CharSequence input;

    public PrintSourcePstVisitor(CharSequence input) {
        this.input = input;
    }

    @Override
    public boolean visit(PstLeafNode node) {
        CharSequence src = input.subSequence(node.getOffset(), node.getOffset() + node.getLength());
        System.out.print(src);
        return super.visit(node);
    }
}
