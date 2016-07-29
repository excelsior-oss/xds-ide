package com.excelsior.xds.parser.modula.ast.tokens;

import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.commons.ast.TokenTypes;

/**
 * Modula-2/Oberon-2 scanner token types for compiler pragmas.
 */
public interface PragmaTokenTypes extends TokenTypes {

    TokenType PRAGMA_BEGIN          = new PragmaTokenType("PRAGMA_BEGIN", "<*");            //$NON-NLS-1$
    TokenType PRAGMA_END            = new PragmaTokenType("PRAGMA_END",   "*>");            //$NON-NLS-1$
    TokenType PRAGMA_POP            = new PragmaTokenType("PRAGMA_POP",   "<* POP *>");     //$NON-NLS-1$   '<* POP *>'  | '<*$>*>'
    TokenType PRAGMA_PUSH           = new PragmaTokenType("PRAGMA_PUSH",  "<* PUSH *>");    //$NON-NLS-1$   '<* PUSH *>' | '<*$<*>'

    TokenType PRAGMA_ARGS_BEGIN         = new PragmaTokenType("PRAGMA_ARGS_BEGIN",         "<*$");     //$NON-NLS-1$
    TokenType PRAGMA_ARGS_POP_BEGIN     = new PragmaTokenType("PRAGMA_ARGS_POP_BEGIN",     "<*$>");    //$NON-NLS-1$
    TokenType PRAGMA_ARGS_PUSH_BEGIN    = new PragmaTokenType("PRAGMA_ARGS_PUSH_BEGIN",    "<*$<");    //$NON-NLS-1$
    TokenType PRAGMA_ARGS_POPPUSH_BEGIN = new PragmaTokenType("PRAGMA_ARGS_POPPUSH_BEGIN", "<*$|");    //$NON-NLS-1$

    TokenType DEFINED_PRAGMA_KEYWORD = new PragmaKeywordType("DEFINED");     //$NON-NLS-1$
    TokenType NEW_PRAGMA_KEYWORD     = new PragmaKeywordType("NEW");         //$NON-NLS-1$
    TokenType POP_PRAGMA_KEYWORD     = new PragmaKeywordType("POP");         //$NON-NLS-1$
    TokenType PUSH_PRAGMA_KEYWORD    = new PragmaKeywordType("PUSH");        //$NON-NLS-1$
    
}
