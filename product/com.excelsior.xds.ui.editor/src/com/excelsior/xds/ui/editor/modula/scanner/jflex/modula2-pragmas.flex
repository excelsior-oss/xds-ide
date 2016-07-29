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
%class _ModulaPragmaFlexScanner
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
  public _ModulaPragmaFlexScanner() {
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

Keywords = "ELSE"    | "ELSIF"  | "END"  | "IF"    |  "THEN"  |
           "DEFINED" | "NEW"    | "POP"  | "PUSH"  |
           "AND"     | ""NOT    | "OR"   | "~"

//-----------------------------------------------------------------------------
// State declarations 
//-----------------------------------------------------------------------------


%%
//-----------------------------------------------------------------------------
// Lexical rules
//-----------------------------------------------------------------------------


// Keywords 
<YYINITIAL> {
    {Keywords}  { return ModulaTokens.PragmaKeyword.getToken(); }
}

// Identifier
<YYINITIAL> {
    {Identifier}    { return ModulaTokens.Pragma.getToken(); }
}

// White spaces, end of file and un matched symbols
{WhiteSpace}+   { return SpecialTokenDescriptors.WHITESPACE; }
<<EOF>>         { return SpecialTokenDescriptors.EOF; }
[^]             { return ModulaTokens.Pragma.getToken(); }

