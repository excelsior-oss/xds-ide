package com.excelsior.xds.parser.modula.ast.tokens;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.excelsior.xds.parser.commons.ast.TokenType;

public interface ModulaTokenSets extends ModulaTokenTypes 
{
    Set<TokenType> COMMENT_SET = new HashSet<TokenType>(Arrays.asList(
            BLOCK_COMMENT,      END_OF_LINE_COMMENT,   
            CPP_BLOCK_COMMENT,  CPP_END_OF_LINE_COMMENT
    ));

    Set<TokenType> WHITE_SPACE_AND_COMMENT_SET = new HashSet<TokenType>(Arrays.asList(
            WHITE_SPACE,
            BLOCK_COMMENT,      END_OF_LINE_COMMENT,   
            CPP_BLOCK_COMMENT,  CPP_END_OF_LINE_COMMENT
    ));


    Set<TokenType> BRACKETS_SET = new HashSet<TokenType>(Arrays.asList(
            LBRACKET, RBRACKET,
            LPARENTH, RPARENTH,
            LBRACE, RBRACE   
    ));

    
    Set<TokenType> STRING_LITERAL_SET = new HashSet<TokenType>(Arrays.asList(
            CHAR_OCT_LITERAL,   
            STRING_LITERAL
    ));
    
    
    Set<TokenType> INTEGER_LITERAL_SET = new HashSet<TokenType>(Arrays.asList(
            OCT_INTEGER_LITERAL,   
            DEC_INTEGER_LITERAL,   
            HEX_INTEGER_LITERAL  
    ));

    
    Set<TokenType> REAL_LITERAL_SET = new HashSet<TokenType>(Arrays.asList(
            REAL_LITERAL,
            LONG_REAL_LITERAL
    ));

    
    Set<TokenType> COMPLEX_LITERAL_SET = new HashSet<TokenType>(Arrays.asList(
            COMPLEX_LITERAL,
            LONG_COMPLEX_LITERAL
    ));

    
    Set<TokenType> LITERAL_SET = new HashSet<TokenType>(Arrays.asList(
            CHAR_HEX_LITERAL,
            // --- STRING_LITERAL_SET 
            CHAR_OCT_LITERAL,   
            STRING_LITERAL,
            // --- INTEGER_LITERAL_SET
            OCT_INTEGER_LITERAL,   
            DEC_INTEGER_LITERAL,   
            HEX_INTEGER_LITERAL,
            // --- REAL_LITERAL_SET
            REAL_LITERAL,
            LONG_REAL_LITERAL,
            // --- COMPLEX_LITERAL_SET
            COMPLEX_LITERAL,
            LONG_COMPLEX_LITERAL
    ));
    
    
    Set<TokenType> NON_OBERON_KEYWORD_SET = new HashSet<TokenType>(Arrays.asList(
            DEFINITION_KEYWORD,            
            EXPORT_KEYWORD,            
            FORWARD_KEYWORD,            
            IMPLEMENTATION_KEYWORD,            
            QUALIFIED_KEYWORD,            
            REM_KEYWORD            
    ));
    
    
    Set<TokenType> KEYWORD_SET = new HashSet<TokenType>(Arrays.asList(
            AND_KEYWORD,        
            ASM_KEYWORD,        
            ARRAY_KEYWORD,      
            BEGIN_KEYWORD,      
            BY_KEYWORD,         
            CASE_KEYWORD,       
            CONST_KEYWORD,      
            DEFINITION_KEYWORD, 
            DIV_KEYWORD,        
            DO_KEYWORD,         
            ELSE_KEYWORD,       
            ELSIF_KEYWORD,      
            END_KEYWORD,        
            EXCEPT_KEYWORD,     
            EXIT_KEYWORD,       
            EXPORT_KEYWORD,     
            FINALLY_KEYWORD,    
            FOR_KEYWORD,        
            FORWARD_KEYWORD,    
            FROM_KEYWORD,       
            IF_KEYWORD,         
            IMPLEMENTATION_KEYWORD,
            IMPORT_KEYWORD,     
            IN_KEYWORD,         
            IS_KEYWORD,         
            LOOP_KEYWORD,       
            MOD_KEYWORD,        
            MODULE_KEYWORD,     
            NOT_KEYWORD,        
            OF_KEYWORD,         
            OR_KEYWORD,         
            PACKEDSET_KEYWORD,  
            POINTER_KEYWORD,    
            PROCEDURE_KEYWORD,  
            QUALIFIED_KEYWORD,  
            RECORD_KEYWORD,     
            REM_KEYWORD,        
            REPEAT_KEYWORD,     
            RETRY_KEYWORD,      
            RETURN_KEYWORD,     
            SEQ_KEYWORD,        
            SET_KEYWORD,        
            THEN_KEYWORD,       
            TO_KEYWORD,         
            TYPE_KEYWORD,       
            UNTIL_KEYWORD,      
            VAR_KEYWORD,        
            WHILE_KEYWORD,      
            WITH_KEYWORD,       

            LABEL_KEYWORD,      
            GOTO_KEYWORD       
    ));

}
