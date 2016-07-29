package com.excelsior.xds.parser.commons.ast;

/**
 * The standard scanner token types common to all languages.
 */
public interface TokenTypes {

    /**
     * Token type for a character which is not valid in the position where it was encountered,
     * according to the language grammar.
     */
    TokenType BAD_CHARACTER = new TokenType("BAD_CHARACTER", "bad character");    //$NON-NLS-1$
    
    /**
     * Token type for a sequence of whitespace characters.
     */
    TokenType WHITE_SPACE = new TokenType ("WHITE_SPACE", "whitespace character");    //$NON-NLS-1$ 

    /**
     * Standard token: End Of File.
     */
    TokenType EOF = new TokenType ("EOF", "end of file");    //$NON-NLS-1$
    
}
