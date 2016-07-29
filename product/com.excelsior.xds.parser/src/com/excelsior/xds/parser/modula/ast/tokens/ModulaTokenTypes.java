package com.excelsior.xds.parser.modula.ast.tokens;

import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.commons.ast.TokenTypes;

/**
 * Modula-2/Oberon-2 scanner token types.
 */
public interface ModulaTokenTypes extends TokenTypes 
{
    TokenType BLOCK_COMMENT           = new ModulaTokenType("BLOCK_COMMENT",       "block comment");            //$NON-NLS-1$
    TokenType END_OF_LINE_COMMENT     = new ModulaTokenType("END_OF_LINE_COMMENT", "end of line comment");      //$NON-NLS-1$
    
    TokenType CPP_BLOCK_COMMENT       = new ModulaTokenType("CPP_BLOCK_COMMENT",       "C++ block comment");        //$NON-NLS-1$
    TokenType CPP_END_OF_LINE_COMMENT = new ModulaTokenType("CPP_END_OF_LINE_COMMENT", "C++ end of line comment");  //$NON-NLS-1$
    
    TokenType IDENTIFIER = new ModulaTokenType("Identifier", "identifier");  //$NON-NLS-1$

    TokenType DEC_INTEGER_LITERAL  = new ModulaTokenType("DEC_INTEGER_Literal", "decimal literal");      //$NON-NLS-1$  val_integer
    TokenType OCT_INTEGER_LITERAL  = new ModulaTokenType("OCT_INTEGER_Literal", "octal literal");        //$NON-NLS-1$  val_integer
    TokenType HEX_INTEGER_LITERAL  = new ModulaTokenType("HEX_INTEGER_Literal", "hexadecimal literal");  //$NON-NLS-1$  val_integer

    TokenType REAL_LITERAL         = new ModulaTokenType("REAL_Literal",        "real literal");         //$NON-NLS-1$  val_real
    TokenType LONG_REAL_LITERAL    = new ModulaTokenType("LONG_REAL_Literal",   "long real literal");    //$NON-NLS-1$  val_long_real

    TokenType COMPLEX_LITERAL      = new ModulaTokenType("COMPLEX_Literal",      "complex literal");        //$NON-NLS-1$  val_cmplx
    TokenType LONG_COMPLEX_LITERAL = new ModulaTokenType("LONG_COMPLEX_Literal", "long complex literal");   //$NON-NLS-1$  val_long_cmplx
    
    TokenType CHAR_HEX_LITERAL = new ModulaTokenType("CHAR_HEX_Literal", "hex code of character");    //$NON-NLS-1$  val_char
    TokenType CHAR_OCT_LITERAL = new ModulaTokenType("CHAR_OCT_Literal", "octal code of character" );    //$NON-NLS-1$  val_string 
    
    TokenType STRING_LITERAL   = new ModulaTokenType("STRING_Literal", "string");     //$NON-NLS-1$  val_string
    
    TokenType LBRACKET  = new ModulaTokenType("LBRACKET",  "[");    //$NON-NLS-1$   lbr 
    TokenType RBRACKET  = new ModulaTokenType("RBRACKET",  "]");    //$NON-NLS-1$   rbr
    TokenType LPARENTH  = new ModulaTokenType("LPARENTH",  "(");    //$NON-NLS-1$   lpar
    TokenType RPARENTH  = new ModulaTokenType("RPARENTH",  ")");    //$NON-NLS-1$   rpar
    TokenType LBRACE    = new ModulaTokenType("LBRACE",    "{");    //$NON-NLS-1$                  
    TokenType RBRACE    = new ModulaTokenType("RBRACE",    "}");    //$NON-NLS-1$   
    
    TokenType PLUS      = new ModulaTokenType("PLUS",  "+");        //$NON-NLS-1$   
    TokenType MINUS     = new ModulaTokenType("MINUS", "-");        //$NON-NLS-1$   
    TokenType TIMES     = new ModulaTokenType("TIMES", "*");        //$NON-NLS-1$     
    TokenType COLON     = new ModulaTokenType("COLON", ":");        //$NON-NLS-1$   
    TokenType SLASH     = new ModulaTokenType("SLASH", "/");        //$NON-NLS-1$   
    TokenType BAR       = new ModulaTokenType("BAR",   "^");        //$NON-NLS-1$   '^', '@'
    TokenType SEP       = new ModulaTokenType("SEP",   "|");        //$NON-NLS-1$   
    TokenType COMMA     = new ModulaTokenType("COMMA", ",");        //$NON-NLS-1$   
    TokenType DOT       = new ModulaTokenType("DOT",   ".");        //$NON-NLS-1$   period
    TokenType RANGE     = new ModulaTokenType("RANGE", "..");       //$NON-NLS-1$   
    TokenType BECOMES   = new ModulaTokenType("BECOMES",   ":=");   //$NON-NLS-1$   
    TokenType SEMICOLON = new ModulaTokenType("SEMICOLON", ";");    //$NON-NLS-1$    

    TokenType AND  = new ModulaTokenType("AND",  "&");   //$NON-NLS-1$
    TokenType NOT  = new ModulaTokenType("NOT",  "~");   //$NON-NLS-1$
    
    TokenType EQU  = new ModulaTokenType("EQU",  "=");   //$NON-NLS-1$ 
    TokenType NEQ  = new ModulaTokenType("NEQ",  "#");   //$NON-NLS-1$   '#', '<>'
    TokenType LSS  = new ModulaTokenType("LSS",  "<");   //$NON-NLS-1$
    TokenType GTR  = new ModulaTokenType("GTR",  ">");   //$NON-NLS-1$
    TokenType LTEQ = new ModulaTokenType("LTEQ", "<=");  //$NON-NLS-1$
    TokenType GTEQ = new ModulaTokenType("GTEQ", ">=");  //$NON-NLS-1$
    
    TokenType LEFT_SHIFT  = new ModulaTokenType("LEFT_SHIFT",  "<<");   //$NON-NLS-1$
    TokenType RIGHT_SHIFT = new ModulaTokenType("RIGHT_SHIFT", ">>");   //$NON-NLS-1$
    
    TokenType EXPONENT    = new ModulaTokenType("EXPONENT", "**");      //$NON-NLS-1$  
    TokenType ALIAS       = new ModulaTokenType("ALIAS",    "::=");     //$NON-NLS-1$  TopSpeed token
    

    ModulaKeywordType AND_KEYWORD        = new ModulaKeywordType("AND");         //$NON-NLS-1$
    ModulaKeywordType ASM_KEYWORD        = new ModulaKeywordType("ASM");         //$NON-NLS-1$
    ModulaKeywordType ARRAY_KEYWORD      = new ModulaKeywordType("ARRAY");       //$NON-NLS-1$
    ModulaKeywordType BEGIN_KEYWORD      = new ModulaKeywordType("BEGIN");       //$NON-NLS-1$
    ModulaKeywordType BY_KEYWORD         = new ModulaKeywordType("BY");          //$NON-NLS-1$
    ModulaKeywordType CASE_KEYWORD       = new ModulaKeywordType("CASE");        //$NON-NLS-1$
    ModulaKeywordType CONST_KEYWORD      = new ModulaKeywordType("CONST");       //$NON-NLS-1$
    ModulaKeywordType DEFINITION_KEYWORD = new ModulaKeywordType("DEFINITION");  //$NON-NLS-1$
    ModulaKeywordType DIV_KEYWORD        = new ModulaKeywordType("DIV");         //$NON-NLS-1$
    ModulaKeywordType DO_KEYWORD         = new ModulaKeywordType("DO");          //$NON-NLS-1$
    ModulaKeywordType ELSE_KEYWORD       = new ModulaKeywordType("ELSE");        //$NON-NLS-1$
    ModulaKeywordType ELSIF_KEYWORD      = new ModulaKeywordType("ELSIF");       //$NON-NLS-1$
    ModulaKeywordType END_KEYWORD        = new ModulaKeywordType("END");         //$NON-NLS-1$
    ModulaKeywordType EXCEPT_KEYWORD     = new ModulaKeywordType("EXCEPT");      //$NON-NLS-1$
    ModulaKeywordType EXIT_KEYWORD       = new ModulaKeywordType("EXIT");        //$NON-NLS-1$
    ModulaKeywordType EXPORT_KEYWORD     = new ModulaKeywordType("EXPORT");      //$NON-NLS-1$
    ModulaKeywordType FINALLY_KEYWORD    = new ModulaKeywordType("FINALLY");     //$NON-NLS-1$
    ModulaKeywordType FOR_KEYWORD        = new ModulaKeywordType("FOR");         //$NON-NLS-1$
    ModulaKeywordType FORWARD_KEYWORD    = new ModulaKeywordType("FORWARD");     //$NON-NLS-1$
    ModulaKeywordType FROM_KEYWORD       = new ModulaKeywordType("FROM");        //$NON-NLS-1$
    ModulaKeywordType IF_KEYWORD         = new ModulaKeywordType("IF");          //$NON-NLS-1$
    ModulaKeywordType IMPLEMENTATION_KEYWORD = new ModulaKeywordType("IMPLEMENTATION");    //$NON-NLS-1$
    ModulaKeywordType IMPORT_KEYWORD     = new ModulaKeywordType("IMPORT");      //$NON-NLS-1$
    ModulaKeywordType IN_KEYWORD         = new ModulaKeywordType("IN");          //$NON-NLS-1$
    ModulaKeywordType IS_KEYWORD         = new ModulaKeywordType("IS");          //$NON-NLS-1$
    ModulaKeywordType LOOP_KEYWORD       = new ModulaKeywordType("LOOP");        //$NON-NLS-1$
    ModulaKeywordType MOD_KEYWORD        = new ModulaKeywordType("MOD");         //$NON-NLS-1$
    ModulaKeywordType MODULE_KEYWORD     = new ModulaKeywordType("MODULE");      //$NON-NLS-1$
    ModulaKeywordType NOT_KEYWORD        = new ModulaKeywordType("NOT");         //$NON-NLS-1$
    ModulaKeywordType OF_KEYWORD         = new ModulaKeywordType("OF");          //$NON-NLS-1$
    ModulaKeywordType OR_KEYWORD         = new ModulaKeywordType("OR");          //$NON-NLS-1$
    ModulaKeywordType PACKEDSET_KEYWORD  = new ModulaKeywordType("PACKEDSET");   //$NON-NLS-1$
    ModulaKeywordType POINTER_KEYWORD    = new ModulaKeywordType("POINTER");     //$NON-NLS-1$
    ModulaKeywordType PROCEDURE_KEYWORD  = new ModulaKeywordType("PROCEDURE");   //$NON-NLS-1$
    ModulaKeywordType QUALIFIED_KEYWORD  = new ModulaKeywordType("QUALIFIED");   //$NON-NLS-1$
    ModulaKeywordType RECORD_KEYWORD     = new ModulaKeywordType("RECORD");      //$NON-NLS-1$
    ModulaKeywordType REM_KEYWORD        = new ModulaKeywordType("REM");         //$NON-NLS-1$
    ModulaKeywordType REPEAT_KEYWORD     = new ModulaKeywordType("REPEAT");      //$NON-NLS-1$
    ModulaKeywordType RETRY_KEYWORD      = new ModulaKeywordType("RETRY");       //$NON-NLS-1$
    ModulaKeywordType RETURN_KEYWORD     = new ModulaKeywordType("RETURN");      //$NON-NLS-1$
    ModulaKeywordType SEQ_KEYWORD        = new ModulaKeywordType("SEQ");         //$NON-NLS-1$
    ModulaKeywordType SET_KEYWORD        = new ModulaKeywordType("SET");         //$NON-NLS-1$
    ModulaKeywordType THEN_KEYWORD       = new ModulaKeywordType("THEN");        //$NON-NLS-1$
    ModulaKeywordType TO_KEYWORD         = new ModulaKeywordType("TO");          //$NON-NLS-1$
    ModulaKeywordType TYPE_KEYWORD       = new ModulaKeywordType("TYPE");        //$NON-NLS-1$
    ModulaKeywordType UNTIL_KEYWORD      = new ModulaKeywordType("UNTIL");       //$NON-NLS-1$
    ModulaKeywordType VAR_KEYWORD        = new ModulaKeywordType("VAR");         //$NON-NLS-1$
    ModulaKeywordType WHILE_KEYWORD      = new ModulaKeywordType("WHILE");       //$NON-NLS-1$
    ModulaKeywordType WITH_KEYWORD       = new ModulaKeywordType("WITH");        //$NON-NLS-1$

    ModulaKeywordType LABEL_KEYWORD      = new ModulaKeywordType("LABEL");       //$NON-NLS-1$
    ModulaKeywordType GOTO_KEYWORD       = new ModulaKeywordType("GOTO");        //$NON-NLS-1$
    
}
