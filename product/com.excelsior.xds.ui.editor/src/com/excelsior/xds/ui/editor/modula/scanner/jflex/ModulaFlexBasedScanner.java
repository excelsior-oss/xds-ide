package com.excelsior.xds.ui.editor.modula.scanner.jflex;

import com.excelsior.xds.ui.commons.syntaxcolor.TokenManager;
import com.excelsior.xds.ui.editor.commons.scanner.jflex.FlexAdapter;


public class ModulaFlexBasedScanner extends FlexAdapter {

    public ModulaFlexBasedScanner(TokenManager tokenManager) {
        super(new _ModulaFlexScanner(), tokenManager);
    }
    
//    @Override
//    public IToken nextToken() {
//        IToken token = super.nextToken();
//        if (token instanceof TextAttributeToken) {
//            System.out.println(">>> token="+((TextAttributeToken)token).getName());
//        } else if (token.isWhitespace()) {
//            System.out.println(">>> token=Token.WHITESPACE");
//        } else if (token.isEOF()) {
//            System.out.println(">>> token=Token.EOF");
//        } else {
//            System.out.println(">>> token="+token.getClass().getName());
//        }
//        System.out.println("    offset="+getTokenOffset()+",  length="+getTokenLength());
//        return token;
//    }

}
