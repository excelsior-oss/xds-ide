package com.excelsior.xds.ui.editor.modula.scanner.jflex;

import com.excelsior.xds.ui.editor.modula.ModulaTokens;
import com.excelsior.xds.ui.editor.commons.scanner.jflex.IFlexScanner;
import com.excelsior.xds.ui.commons.syntaxcolor.TokenDescriptor;
import com.excelsior.xds.ui.commons.syntaxcolor.SpecialTokenDescriptors;

%%

//-----------------------------------------------------------------------------
// Character sets
//-----------------------------------------------------------------------------
%unicode

//-----------------------------------------------------------------------------
// Class options 
//-----------------------------------------------------------------------------

%public
%class _ModulaFlexScanner
%implements IFlexScanner

%function nextToken
%type TokenDescriptor

//%debug


//-----------------------------------------------------------------------------
// User Class Code 
//-----------------------------------------------------------------------------

%eof{ 
    return;
%eof}

%{
  public _ModulaFlexScanner() {
      this((java.io.Reader)null);
  }
%}


//-----------------------------------------------------------------------------
// Macro definitions 
//-----------------------------------------------------------------------------

// NewLines and spaces
WhiteSpace = [ \t\f\r\n]

IdentifierPart = "_" | [:letter:] | [:digit:] 
Identifier     = (["_"] | [:letter:])+ {IdentifierPart}*

// Digits
OctalDigit = [0-7]
Digit      = {OctalDigit} | [89]
HexDigit   = {Digit} | [A-Fa-f]
Integer    = {Digit}+ | {OctalDigit}+ "C" | {Digit} {HexDigit}* ("H"|"h")

ScaleFactor = "E" ["+""-"]? {Digit}+
Real        = {Digit}+ "." {Digit}* {ScaleFactor}?

DigitRangeHack = {Digit}+ ".."

Brackets = "(" | ")" | "[" | "]" | "{" | "}"
                          
Keywords = "ASM"            |
           "AND"            | "ARRAY"     | "BEGIN"      | "BY"        |
           "CASE"           | "CONST"     | "DEFINITION" | "DIV"       |
           "DO"             | "ELSE"      | "ELSIF"      | "END"       |
           "EXIT"           | "EXCEPT"    | "EXPORT"     | "FINALLY"   |
           "FOR"            | "FORWARD"   | "FROM"       | "IF"        |
           "IMPLEMENTATION" | "IMPORT"    | "IN"         | "LOOP"      |
           "MOD"            | "MODULE"    | "NOT"        | "OF"        |
           "OR"             | "PACKEDSET" | "POINTER"    | "PROCEDURE" |  
           "QUALIFIED"      | "RECORD"    | "REM"        | "RETRY"     |  
           "REPEAT"         | "RETURN"    | "SEQ"        | "SET"       | 
           "THEN"           | "TO"        | "TYPE"       | "UNTIL"     | 
           "VAR"            | "WHILE"     | "WITH"       | 
           "~"              | "&"         

PervasiveIdentifiers =
           "ABS"      | "ASSERT"          | "BITSET"    | "BOOLEAN"         |
           "CARDINAL" | "CAP"             | "CHR"       | "CHAR"            |
           "COMPLEX"  | "CMPLX"           | "DEC"       | "DISPOSE"         |
           "EXCL"     | "FALSE"           | "FLOAT"     | "HALT"            |
           "HIGH"     | "IM"              | "INC"       | "INCL"            |
           "INT"      | "INTERRUPTIBLE"   | "INTEGER"   | "LENGTH"          |
           "LFLOAT"   | "LONGCOMPLEX"     | "LONGINT"   | "LONGREAL"        |
           "MAX"      | "MIN"             | "NEW"       | "NIL"             | 
           "ODD"      | "ORD"             | "PROC"      | "PROTECTION"      | 
           "RE"       | "REAL"            | "SHORTCARD" | "SHORTINT"        |
           "SIZE"     | "TRUE"            | "TRUNC"     | "UNINTERRUPTIBLE" | 
           "VAL"         

PervasiveConstant = "FALSE" | "NIL" | "TRUE"
 
SystemMod =
           "SYSTEM"      | "BITSPERLOC"  | "LOCSPERWORD" | "LOCSPERBYTE" |
           "LOC"         | "ADDRESS"     | "WORD"        | "BYTE"        |
           "INT"         | "CARD"        | "int"         | "unsigned"    |
           "INT8"        | "INT16"       | "INT32"       | "INT64"       |
           "SET8"        | "SET16"       | "SET32"       | "SET64"       |
           "CARD8"       | "CARD16"      | "CARD32"      | "CARD64"      |
           "BOOL8"       | "BOOL16"      | "BOOL32"      | "INDEX"       |
           "void"        | "size_t"      | "TSIZE"       | "CC"          |
           "ADDADR"      | "SUBADR"      | "DIFADR"      | "MAKEADR"     |
           "ADR"         | "REF"         | "ROTATE"      | "SHIFT"       |
           "CAST"        | "DIFADR_TYPE" | "MOVE"        | "FILL"        |
           "GET"         | "PUT" 
                      
 

//-----------------------------------------------------------------------------
// State declarations 
//-----------------------------------------------------------------------------


%%
//-----------------------------------------------------------------------------
// Lexical rules
//-----------------------------------------------------------------------------


// Keywords 
<YYINITIAL> {
    {Keywords}              { return ModulaTokens.Keyword.getToken(); }
    {PervasiveConstant}     { return ModulaTokens.BuiltinConstant.getToken();  }
    {PervasiveIdentifiers}  { return ModulaTokens.Keyword.getToken(); }
}

// Keywords from System.mod
<YYINITIAL> {
    {SystemMod}             { return ModulaTokens.SystemModuleKeyword.getToken(); }
}

// Digits
<YYINITIAL> {
    {DigitRangeHack} { yypushback(2); 
                       return ModulaTokens.Number.getToken();
                     }
    {Integer}        { return ModulaTokens.Number.getToken(); }
    {Real}           { return ModulaTokens.Number.getToken(); }
}

// Identifier
<YYINITIAL> {
    {Identifier}    { return ModulaTokens.Default.getToken(); }
}


// Brackets
{Brackets}      { return ModulaTokens.Bracket.getToken(); }


// White spaces, end of file and un matched symbols
{WhiteSpace}+   { return SpecialTokenDescriptors.WHITESPACE; }
<<EOF>>         { return SpecialTokenDescriptors.EOF; }

// Un matched symbols
".."            { return ModulaTokens.Default.getToken(); }
[^]             { return ModulaTokens.Default.getToken(); }

