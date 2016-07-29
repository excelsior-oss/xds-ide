package com.excelsior.xds.ui.editor.modula;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Keywords and pervasive identifiers of Modula-2 language.
 * 
 * An identifier is a list of alphanumeric and low line ("_") characters starting
 * with a letter. The ISO Standard permits an implementation-defined set of national 
 * alphanumeric characters to be used in identifiers. XDS defines this set as empty.  
 */
public interface IModulaKeywords {

    /**
     * Keywords in fact are identifiers reserved by the language for special use. 
     * They can not be redeclared, unlike the pervasive identifiers.
     */
    public static final HashSet<String> KEYWORDS = new HashSet<String>(Arrays.asList(
        "ASM",                                                     //$NON-NLS-1$                                
        "AND",            "ARRAY",      "BEGIN",       "BY",       //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "CASE",           "CONST",      "DEFINITION",  "DIV",      //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "DO",             "ELSE",       "ELSIF",       "END",      //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "EXIT",           "EXCEPT",     "EXPORT",      "FINALLY",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "FOR",            "FORWARD",    "FROM",        "IF",       //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "IMPLEMENTATION", "IMPORT" ,    "IN",          "LOOP",     //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "MOD",            "MODULE",     "NOT",         "OF",       //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "OR",             "PACKEDSET",  "POINTER",     "PROCEDURE",//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "QUALIFIED",      "RECORD",     "REM",         "RETRY",    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "REPEAT",         "RETURN",     "SET",         "SEQ",      //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$    
        "THEN",           "TO",         "TYPE",        "UNTIL",    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "VAR",            "WHILE",       "WITH",                   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-4$
        "~",              "&"                                      //$NON-NLS-1$ //$NON-NLS-2$
    ));

    /**
     *  Pervasive identifiers are not reserved words; if redeclared in a program, 
     *  they will no longer have their predefined meaning in the scope of 
     *  the redeclared identifier.
     */
    public static final HashSet<String> PERVASIVE_IDENTIFIERS = new HashSet<String>(Arrays.asList(
        "ABS",            "ASSERT",           "BITSET",    "BOOLEAN",   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "CARDINAL",       "CAP",              "CHR",       "CHAR",      //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "COMPLEX",        "CMPLX",            "DEC",       "DISPOSE",   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "EXCL",           "FALSE",            "FLOAT",     "HALT",      //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "HIGH",           "IM",               "INC",       "INCL",      //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "INT",            "INTERRUPTIBLE",    "INTEGER",   "LENGTH",    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "LFLOAT",         "LONGCOMPLEX",      "LONGINT",   "LONGREAL",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "MAX",            "MIN",              "NEW",       "NIL",       //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "ODD",            "ORD",              "PROC",      "PROTECTION",//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "RE",             "REAL",             "SIZE",      "TRUE",      //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "TRUNC",          "UNINTERRUPTIBLE",  "VAL"                     //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    ));

    
    /**
     *  Subset of pervasive identifiers which includes identifiers of Modula-2 
     *  built-in constants.
     */
    public static final HashSet<String> PERVASIVE_CONSTANTS = new HashSet<String>(Arrays.asList(
        "FALSE",          "NIL",              "TRUE"   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    ));
    
    
    /**
     * Identifiers declared in the SYSTEM module.
     */
    public static final HashSet<String> SYSTEM_IDENTIFIERS = new HashSet<String>(Arrays.asList(
        "SYSTEM",       "BITSPERLOC",  "LOCSPERWORD",  "LOCSPERBYTE",   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "LOC",          "ADDRESS",     "WORD",         "BYTE",          //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$  
        "ADDADR",       "SUBADR",      "DIFADR",       "MAKEADR",       //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "ADR",          "REF",         "ROTATE",       "SHIFT",         //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "CAST",         "TSIZE",       "INT8",         "INT16",         //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "INT32",        "CARD8",       "CARD16",       "CARD32",        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "BOOL8",        "BOOL16",      "BOOL32",       "INDEX",         //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "DIFADR_TYPE",  "INT",         "CARD",         "int",           //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "unsigned",     "size_t",      "void",         "MOVE",          //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "FILL",         "GET",         "PUT",          "CC"             //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    ));
    
}
