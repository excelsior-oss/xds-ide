package com.excelsior.xds.parser.modula.scanner.jflex;

import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.ast.tokens.PragmaTokenTypes;

%%

//-----------------------------------------------------------------------------
// Character sets
//-----------------------------------------------------------------------------
%unicode

//-----------------------------------------------------------------------------
// Class options 
//-----------------------------------------------------------------------------

%public
%class _XdsFlexScanner
%implements ModulaTokenTypes, PragmaTokenTypes


%function nextToken
%type TokenType

%line
%column

//%debug


//-----------------------------------------------------------------------------
// User Class Code 
//-----------------------------------------------------------------------------

%eof{ 
    return;
%eof}

%{
  int startCommentPos;
  int startCommentLine;
  int endCommentState;

  int endPragmaState;

  public _XdsFlexScanner() {
    this((java.io.Reader)null);
  }

  /**
   * Returns the number of characters up to the start of the matched text.
   */
  public final int getTokenOffset() {
    return zzStartRead;
  }

  public TextPosition getTokenPosition() {
    return new TextPosition(yyline + 1, yycolumn + 1, getTokenOffset());
  }

  public TokenRestorePosition getTokenRestorePosition() {
    return new TokenRestorePosition(yyline, yycolumn, getTokenOffset(), yystate());
  }

  public void setPosition(TokenRestorePosition position) {
    setPosition( position.getOffset()
               , position.getLine(), position.getColumn()
               , position.getState() ); 
  }
%}


//-----------------------------------------------------------------------------
// Macro definitions 
//-----------------------------------------------------------------------------

// NewLines and spaces
NewLine    = [\r\n]
WhiteSpace = [\ \t\f] 
NewLineWhiteSpace =  {WhiteSpace} | {NewLine}

IdentifierPart = "_" | [:letter:] | [:digit:] 
Identifier     = (["_"] | [:letter:])+ {IdentifierPart}*

// Digits
OctalDigit = [0-7]
Digit      = {OctalDigit} | [89]
HexDigit   = {Digit} | [A-Fa-f]

// Integer Numbers
OctalInteger   = {OctalDigit}+ ("B"|"b") 
DecimalInteger = {Digit}+ 
HexInteger     = {Digit} {HexDigit}* ("H"|"h")

// Real Numbers
RealScaleFactor = "E" ["+""-"]? {Digit}+
RealLiteral     = {Digit}+ "." {Digit}* {RealScaleFactor}?

LongRealScaleFactor = "D" ["+""-"]? {Digit}+
LongRealLiteral     = {Digit}+ "." {Digit}* {LongRealScaleFactor}?

DigitRangeHack = {Digit}+ ".."

// Complex Numbers
ComplexLiteral     = {RealLiteral} "i"
LongComplexLiteral = {LongRealLiteral} "i"

// Chars
CharOctLiteral = {Digit} {OctalDigit}* ("C"|"c")
CharHexLiteral = {Digit} {HexDigit}*   ("X"|"x")

// Strings
StringLiteralDoubleQuote = \" ( [^\"\n\r] )* (\" | {NewLine})
StringLiteralSingleQuote = \' ( [^\'\n\r] )* (\' | {NewLine})
StringLiteral = {StringLiteralDoubleQuote} | {StringLiteralSingleQuote}

// Comments
EndOfLineComment    = "-""-"[^\r\n]*
CppEndOfLineComment = "/""/"[^\r\n]*

BlockCommentBegin  = "(*" 
BlockCommentEnd    = "*)" 
SimpleCommentBody  = ([^"*""("]*("*"+[^"*"")"])?("("+[^"(""*"])?)*
SimpleBlockComment = {BlockCommentBegin} {SimpleCommentBody} {BlockCommentEnd}

CppBlockCommentBegin  = "/*" 
CppBlockCommentEnd    = "*/" 
CppSimpleCommentBody  = ([^"*""/"]*("*"+[^"*"")"])?("/"+[^"/""*"])?)*
CppSimpleBlockComment = {CppBlockCommentBegin} {CppSimpleCommentBody} {CppBlockCommentEnd}


// Compiler pragmas
PragmaBegin = "<*" 
PragmaEnd   = "*>" 

PragmaArgsBegin        = {PragmaBegin} "$"
PopPragmaArgsBegin     = {PragmaArgsBegin} {WhiteSpace}* ">"
PushPragmaArgsBegin    = {PragmaArgsBegin} {WhiteSpace}* "<"
PopPushPragmaArgsBegin = {PragmaArgsBegin} {WhiteSpace}* "|"

PopPragma  = {PragmaBegin} {WhiteSpace}* "POP" {WhiteSpace}* {PragmaEnd}
           | {PopPragmaArgsBegin} {WhiteSpace}* {PragmaEnd}

PushPragma = {PragmaBegin} {WhiteSpace}* "PUSH" {WhiteSpace}* {PragmaEnd}
           | {PushPragmaArgsBegin} {WhiteSpace}* {PragmaEnd}


//-----------------------------------------------------------------------------
// State declarations 
//-----------------------------------------------------------------------------

%xstate IN_BLOCK_COMMENT
%xstate IN_CPP_BLOCK_COMMENT

%state IN_PRAGMA_BLOCK


%%
//-----------------------------------------------------------------------------
// Lexical rules
//-----------------------------------------------------------------------------

// Keywords
"AND"        { return AND_KEYWORD; }
"ASM"        { return ASM_KEYWORD; }
"ARRAY"      { return ARRAY_KEYWORD; }
"BEGIN"      { return BEGIN_KEYWORD; }
"BY"         { return BY_KEYWORD; }
"CASE"       { return CASE_KEYWORD; }
"CONST"      { return CONST_KEYWORD; }
"DEFINITION" { return DEFINITION_KEYWORD; }
"DIV"        { return DIV_KEYWORD; }
"DO"         { return DO_KEYWORD; }
"ELSE"       { return ELSE_KEYWORD; }
"ELSIF"      { return ELSIF_KEYWORD; }
"END"        { return END_KEYWORD; }
"EXCEPT"     { return EXCEPT_KEYWORD; }
"EXIT"       { return EXIT_KEYWORD; }
"EXPORT"     { return EXPORT_KEYWORD; }
"FINALLY"    { return FINALLY_KEYWORD; }
"FOR"        { return FOR_KEYWORD; }
"FORWARD"    { return FORWARD_KEYWORD; }
"FROM"       { return FROM_KEYWORD; }
"IF"         { return IF_KEYWORD; }
"IMPLEMENTATION" { return IMPLEMENTATION_KEYWORD; }
"IMPORT"     { return IMPORT_KEYWORD; }
"IN"         { return IN_KEYWORD; }
"IS"         { return IS_KEYWORD; }
"LOOP"       { return LOOP_KEYWORD; }
"MOD"        { return MOD_KEYWORD; }
"MODULE"     { return MODULE_KEYWORD; }
"NOT"        { return NOT_KEYWORD; }
"OF"         { return OF_KEYWORD; }
"OR"         { return OR_KEYWORD; }
"PACKEDSET"  { return PACKEDSET_KEYWORD; }
"POINTER"    { return POINTER_KEYWORD; }
"PROCEDURE"  { return PROCEDURE_KEYWORD; }
"QUALIFIED"  { return QUALIFIED_KEYWORD; }
"RECORD"     { return RECORD_KEYWORD; }
"REM"        { return REM_KEYWORD; }
"REPEAT"     { return REPEAT_KEYWORD; }
"RETRY"      { return RETRY_KEYWORD; }
"RETURN"     { return RETURN_KEYWORD; }
"SEQ"        { return SEQ_KEYWORD; }
"SET"        { return SET_KEYWORD; }
"THEN"       { return THEN_KEYWORD; }
"TO"         { return TO_KEYWORD; }
"TYPE"       { return TYPE_KEYWORD; }
"UNTIL"      { return UNTIL_KEYWORD; }
"VAR"        { return VAR_KEYWORD; }
"WHILE"      { return WHILE_KEYWORD; }
"WITH"       { return WITH_KEYWORD; }

"LABEL"      { return LABEL_KEYWORD; }
"GOTO"       { return GOTO_KEYWORD; }


// Brackets
"["   { return LBRACKET; }
"]"   { return RBRACKET; }
"("   { return LPARENTH; }
")"   { return RPARENTH; }
"{"   { return LBRACE;   }
"}"   { return RBRACE;   }

"(!"  { return LBRACKET; }
"!)"  { return RBRACKET; }
"(:"  { return LBRACE;   }


// Operations
"+"   { return PLUS;  }
"-"   { return MINUS; }
"*"   { return TIMES; }
":"   { return COLON; }
"/"   { return SLASH; }
"^"   { return BAR;   }
"@"   { return BAR;   }
"&"   { return AND;   }
"|"   { return SEP;   }
"!"   { return SEP;   }
";"   { return SEMICOLON; }
","   { return COMMA; }
"~"   { return NOT;   }
"."   { return DOT;   }
":="  { return BECOMES; }

"="   { return EQU;  }
"#"   { return NEQ;  }
"<>"  { return NEQ;  }
"<"   { return LSS;  }
">"   { return GTR;  }
"<="  { return LTEQ; }
">="  { return GTEQ; }

".."  { return RANGE;       }
"<<"  { return LEFT_SHIFT;  }
">>"  { return RIGHT_SHIFT; }

"**"  { return EXPONENT; }
"::=" { return ALIAS;    }


// Integer literals
{OctalInteger}   { return OCT_INTEGER_LITERAL; }
{DecimalInteger} { return DEC_INTEGER_LITERAL; }
{HexInteger}     { return HEX_INTEGER_LITERAL; }

{DigitRangeHack} { yypushback(2); 
                   return DEC_INTEGER_LITERAL;
                 }

{CharOctLiteral} {return CHAR_OCT_LITERAL; }
{CharHexLiteral} {return CHAR_HEX_LITERAL; }


// Real literals
{RealLiteral}     { return REAL_LITERAL; }
{LongRealLiteral} { return LONG_REAL_LITERAL; }

// Complex literals
{ComplexLiteral}     { return COMPLEX_LITERAL; }
{LongComplexLiteral} { return LONG_COMPLEX_LITERAL; }


// String literal
{StringLiteral}  { return STRING_LITERAL; }


// Comments
{EndOfLineComment}    { return END_OF_LINE_COMMENT; }
{CppEndOfLineComment} { return CPP_END_OF_LINE_COMMENT; }

{SimpleBlockComment}    { return BLOCK_COMMENT;     }
{CppSimpleBlockComment} { return CPP_BLOCK_COMMENT; }

{BlockCommentBegin}     { nestLevel = 1;
                          startCommentPos  = zzStartRead;
                          startCommentLine = yyline;
                          endCommentState  = yystate();
                          yybegin(IN_BLOCK_COMMENT);
                        }
{CppBlockCommentBegin}  { nestLevel = 1; 
                          startCommentPos  = zzStartRead;
                          startCommentLine = yyline;
                          endCommentState  = yystate();
                          yybegin(IN_CPP_BLOCK_COMMENT); 
                        }

<IN_BLOCK_COMMENT> {
    {BlockCommentBegin}  { nestLevel++; }
    {BlockCommentEnd}    { nestLevel--; 
                           if (nestLevel == 0) {
                               zzStartRead = startCommentPos;
                               yyline = startCommentLine;
                               yybegin(endCommentState);
                               return BLOCK_COMMENT;
                           }
                         }
    <<EOF>>              { zzStartRead = startCommentPos;
                           yyline = startCommentLine;
                           yybegin(endCommentState); 
                           return BLOCK_COMMENT;
                         }
    [^]                  { }
}

<IN_CPP_BLOCK_COMMENT> {
    {CppBlockCommentBegin}  { nestLevel++; }
    {CppBlockCommentEnd}    { nestLevel--; 
                              if (nestLevel == 0) {
                                 zzStartRead = startCommentPos;
                                 yyline = startCommentLine;
                                 yybegin(endCommentState);
                                 return CPP_BLOCK_COMMENT;
                              }
                            }
    <<EOF>>                 { zzStartRead = startCommentPos;
                              yyline = startCommentLine;
                              yybegin(endCommentState); 
                              return CPP_BLOCK_COMMENT;
                            }
    [^]                     { }
}


// Compiler pragmas
<YYINITIAL> {
    {PopPragma}   { return PRAGMA_POP;  }
    {PushPragma}  { return PRAGMA_PUSH; }

    {PragmaBegin} { endPragmaState = yystate();
                    yybegin(IN_PRAGMA_BLOCK);
                    return PRAGMA_BEGIN;
                  }

   {PragmaArgsBegin}        { endPragmaState = yystate();
                              yybegin(IN_PRAGMA_BLOCK);
                              return PRAGMA_ARGS_BEGIN;         
                            }
   {PopPragmaArgsBegin}     { endPragmaState = yystate();
                              yybegin(IN_PRAGMA_BLOCK);
                              return PRAGMA_ARGS_POP_BEGIN;     
                            }
   {PushPragmaArgsBegin}    { endPragmaState = yystate();
                              yybegin(IN_PRAGMA_BLOCK);
                              return PRAGMA_ARGS_PUSH_BEGIN;    
                            }
   {PopPushPragmaArgsBegin} { endPragmaState = yystate();
                              yybegin(IN_PRAGMA_BLOCK);
                              return PRAGMA_ARGS_POPPUSH_BEGIN; 
                            }

}

<IN_PRAGMA_BLOCK> {
    "DEFINED"   { return DEFINED_PRAGMA_KEYWORD;  }
    "NEW"       { return NEW_PRAGMA_KEYWORD;      }
    "POP"       { return POP_PRAGMA_KEYWORD;      }
    "PUSH"      { return PUSH_PRAGMA_KEYWORD;     }

    {PragmaEnd} { yybegin(endPragmaState);
                  return PRAGMA_END;
                }
}


// Identifier
{Identifier}     { return IDENTIFIER; }

// White spaces, end of file and un matched symbols
{NewLineWhiteSpace}+  { return WHITE_SPACE; }
<<EOF>>               { return EOF; }
[^]                   { return BAD_CHARACTER; }
