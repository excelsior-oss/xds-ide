package com.excelsior.xds.parser.modula.ast.tokens;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.excelsior.xds.parser.commons.ast.TokenType;

public interface XdsParserTokenSets extends ModulaTokenSets
{
    Set<TokenType> RETURN_TERMINATION_SET = new HashSet<TokenType>(Arrays.asList(
            END_KEYWORD,   ELSE_KEYWORD,   ELSIF_KEYWORD,
            SEMICOLON,     SEP
    ));

    Set<TokenType> STATEMENT_TERMINATION_SET = new HashSet<TokenType>(Arrays.asList(
            END_KEYWORD,      ELSE_KEYWORD,     ELSIF_KEYWORD,
            UNTIL_KEYWORD,    SEP,
            FINALLY_KEYWORD,  EXCEPT_KEYWORD
    ));
    
    
    Set<TokenType> TERM_LEVEL_OPERATION_SET = new HashSet<TokenType>(Arrays.asList(
            DIV_KEYWORD,  MOD_KEYWORD,  REM_KEYWORD,  SLASH, 
            TIMES,        AND,          LEFT_SHIFT,   RIGHT_SHIFT
    ));

    
    Set<TokenType> END_ELSE_SEP_SET = new HashSet<TokenType>(Arrays.asList(
            END_KEYWORD,   ELSE_KEYWORD,   SEP  
    ));

    Set<TokenType> CONST_VAR_TYPE_SET = new HashSet<TokenType>(Arrays.asList(
            CONST_KEYWORD,   VAR_KEYWORD,   TYPE_KEYWORD  
    ));

    Set<TokenType> DECLARATION_SET = new HashSet<TokenType>(Arrays.asList(
            CONST_KEYWORD,    VAR_KEYWORD,        TYPE_KEYWORD,
            MODULE_KEYWORD,   PROCEDURE_KEYWORD
    ));

    
    Set<TokenType> SKIP_CONSTRUCTOR_SET = new HashSet<TokenType>(Arrays.asList(
            SEMICOLON, 
            END_KEYWORD,      BEGIN_KEYWORD     
    ));
    
    
    Set<TokenType> DECLARATION_SYNCHRONIZATION_SET = new HashSet<TokenType>(Arrays.asList(
            CONST_KEYWORD,    VAR_KEYWORD,        TYPE_KEYWORD,
            MODULE_KEYWORD,   PROCEDURE_KEYWORD,
            BEGIN_KEYWORD,    END_KEYWORD
    ));

    Set<TokenType> MODULE_DECLARATION_SYNCHRONIZATION_SET = new HashSet<TokenType>(Arrays.asList(
           CONST_KEYWORD,    VAR_KEYWORD,        TYPE_KEYWORD,
           MODULE_KEYWORD,   PROCEDURE_KEYWORD,
           BEGIN_KEYWORD,    END_KEYWORD,
           IMPORT_KEYWORD,   FROM_KEYWORD                                                                                      
    ));

    Set<TokenType> OBERON_PROCEDURE_SYNCHRONIZATION_SET = new HashSet<TokenType>(Arrays.asList(
            END_KEYWORD,      RPARENTH
    ));

    Set<TokenType> FACTOR_SYNCHRONIZATION_SET = new HashSet<TokenType>(Arrays.asList(
            END_KEYWORD,       SEMICOLON,        RPARENTH     
    ));

}
