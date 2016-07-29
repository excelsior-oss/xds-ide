package com.excelsior.xds.parser.modula;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface XdsStandardNames 
{
    public String OBERON_SUPERMODULE = "#OBERON_SUPERMODULE#";    //$NON-NLS-1$ 
    public String MODULA_SUPERMODULE = "#MODULA_SUPERMODULE#";    //$NON-NLS-1$ 
    public String COMPILER           = "COMPILER";                //$NON-NLS-1$ 
    public String SYSTEM             = "SYSTEM";                  //$NON-NLS-1$ 

    public String INDEX              = "INDEX";                   //$NON-NLS-1$ 
    
    public String INTEGER      = "INTEGER";         //$NON-NLS-1$ 
    public String CARDINAL     = "CARDINAL";        //$NON-NLS-1$ 
    public String M2ADDTYPES   = "M2ADDTYPES";      //$NON-NLS-1$ 
    public String SHORTINT     = "SHORTINT";        //$NON-NLS-1$ 
    public String LONGINT      = "LONGINT";         //$NON-NLS-1$ 
    public String LONGLONGINT  = "LONGLONGINT";     //$NON-NLS-1$ 
    public String SHORTCARD    = "SHORTCARD";       //$NON-NLS-1$ 
    public String LONGCARD     = "LONGCARD";        //$NON-NLS-1$ 
    public String LONGLONGCARD = "LONGLONGCARD";    //$NON-NLS-1$ 
    public String BOOLEAN      = "BOOLEAN";         //$NON-NLS-1$ 
    public String CHAR         = "CHAR";            //$NON-NLS-1$ 
    public String REAL         = "REAL";            //$NON-NLS-1$ 
    public String LONGREAL     = "LONGREAL";        //$NON-NLS-1$ 
    public String LONGLONGREAL = "LONGLONGREAL";    //$NON-NLS-1$ 

    /**
     * The standard type PROC denotes a parameterless procedure, defined as PROC = PROCEDURE.
     */
    public String PROC = "PROC";    //$NON-NLS-1$ 

    public String BITSET      = "BITSET";          //$NON-NLS-1$ 
    public String BITSET64    = "BITSET64";       //$NON-NLS-1$ 
    public String PROTECTION  = "PROTECTION";     //$NON-NLS-1$ 
    public String COMPLEX     = "COMPLEX";        //$NON-NLS-1$ 
    public String LONGCOMPLEX = "LONGCOMPLEX";    //$NON-NLS-1$ 
    public String SET         = "SET";            //$NON-NLS-1$ 
    public String CARD8       = "CARD8";          //$NON-NLS-1$ 
    
    public String LOC     = "LOC";        //$NON-NLS-1$ 
    public String WORD    = "WORD";       //$NON-NLS-1$ 
    public String ADDRESS = "ADDRESS";    //$NON-NLS-1$ 
    public String BYTE    = "BYTE";       //$NON-NLS-1$ 

    public String INT8     = "INT8";        //$NON-NLS-1$ 
    public String INT16    = "INT16";       //$NON-NLS-1$ 
    public String CARD16   = "CARD16";      //$NON-NLS-1$ 
    public String INT32    = "INT32";       //$NON-NLS-1$ 
    public String CARD32   = "CARD32";      //$NON-NLS-1$ 
    public String SET8     = "SET8";        //$NON-NLS-1$ 
    public String SET16    = "SET16";       //$NON-NLS-1$ 
    public String SET32    = "SET32";       //$NON-NLS-1$ 
    public String SET64    = "SET64";       //$NON-NLS-1$ 
    public String BOOL8    = "BOOL8";       //$NON-NLS-1$ 
    public String BOOL16   = "BOOL16";      //$NON-NLS-1$ 
    public String BOOL32   = "BOOL32";      //$NON-NLS-1$ 
    public String Int      = "int";         //$NON-NLS-1$ 
    public String Unsigned = "unsigned";    //$NON-NLS-1$ 
    public String Size_t   = "size_t";      //$NON-NLS-1$ 
    public String Void     = "void";        //$NON-NLS-1$ 
    public String INT      = "INT";         //$NON-NLS-1$ 
    public String CARD     = "CARD";        //$NON-NLS-1$ 
    public String INT64    = "INT64";       //$NON-NLS-1$ 
    public String CARD64   = "CARD64";      //$NON-NLS-1$ 
    
    public String BITSPERLOC  = "BITSPERLOC";     //$NON-NLS-1$ 
    public String LOCSPERWORD = "LOCSPERWORD";    //$NON-NLS-1$ 
    public String LOCSPERBYTE = "LOCSPERBYTE";    //$NON-NLS-1$ 
    
    public String ADDADR  = "ADDADR";     //$NON-NLS-1$ 
    public String SUBADR  = "SUBADR";     //$NON-NLS-1$ 
    public String DIFADR  = "DIFADR";     //$NON-NLS-1$ 
    public String DIFADRC = "DIFADRC";    //$NON-NLS-1$ 
    public String MAKEADR = "MAKEADR";    //$NON-NLS-1$ 
    public String ADR     = "ADR";        //$NON-NLS-1$ 
    public String M2ADR   = "M2ADR";      //$NON-NLS-1$ 
    public String SHIFT   = "SHIFT";      //$NON-NLS-1$ 
    public String ROTATE  = "ROTATE";     //$NON-NLS-1$ 
    public String CAST    = "CAST";       //$NON-NLS-1$ 
    public String TSIZE   = "TSIZE";      //$NON-NLS-1$ 
    public String ROT     = "ROT";        //$NON-NLS-1$ 
    public String LSH     = "LSH";        //$NON-NLS-1$ 
    public String NEW     = "NEW";        //$NON-NLS-1$ 
    public String DISPOSE = "DISPOSE";    //$NON-NLS-1$ 
    public String PTR     = "PTR";        //$NON-NLS-1$ 
    
    public String CC        = "CC";          //$NON-NLS-1$ 
    public String BIT       = "BIT";         //$NON-NLS-1$ 
    public String VAL       = "VAL";         //$NON-NLS-1$ 
    public String GET       = "GET";         //$NON-NLS-1$ 
    public String GETREG    = "GETREG";      //$NON-NLS-1$ 
    public String PUT       = "PUT";         //$NON-NLS-1$ 
    public String PUTREG    = "PUTREG";      //$NON-NLS-1$ 
    public String MOVE      = "MOVE";        //$NON-NLS-1$ 
    public String CODE      = "CODE";        //$NON-NLS-1$ 
    public String BYTES     = "BYTES";       //$NON-NLS-1$ 
    public String BITS      = "BITS";        //$NON-NLS-1$ 
    public String REF       = "REF";         //$NON-NLS-1$ 
    public String VALID     = "VALID";       //$NON-NLS-1$ 
    public String FILL      = "FILL";        //$NON-NLS-1$ 
    public String EVAL      = "EVAL";        //$NON-NLS-1$ 
    public String FIELDOFS  = "FIELDOFS";    //$NON-NLS-1$ 
    public String PRED      = "PRED";        //$NON-NLS-1$ 
    public String SUCC      = "SUCC";        //$NON-NLS-1$ 
    public String TARGET    = "TARGET";      //$NON-NLS-1$ 
    public String OPTION    = "OPTION";      //$NON-NLS-1$ 
    public String EQUATION  = "EQUATION";    //$NON-NLS-1$ 
    public String TIMESTAMP = "TIMESTAMP";   //$NON-NLS-1$ 

    public String TRUE  = "TRUE";     //$NON-NLS-1$ 
    public String FALSE = "FALSE";    //$NON-NLS-1$ 
    public String NIL   = "NIL";      //$NON-NLS-1$
    
    public String INTERRUPTIBLE   = "INTERRUPTIBLE";     //$NON-NLS-1$ 
    public String UNINTERRUPTIBLE = "UNINTERRUPTIBLE";   //$NON-NLS-1$ 
    
    public String ABS      = "ABS";         //$NON-NLS-1$ 
    public String DEC      = "DEC";         //$NON-NLS-1$ 
    public String INC      = "INC";         //$NON-NLS-1$ 
    public String INCL     = "INCL";        //$NON-NLS-1$ 
    public String EXCL     = "EXCL";        //$NON-NLS-1$ 
    public String HALT     = "HALT";        //$NON-NLS-1$ 
    public String ASSERT   = "ASSERT";      //$NON-NLS-1$ 
    public String COPY     = "COPY";        //$NON-NLS-1$ 
    public String RESIZE   = "RESIZE";      //$NON-NLS-1$ 
    public String CAP      = "CAP";         //$NON-NLS-1$ 
    public String CHR      = "CHR";         //$NON-NLS-1$ 
    public String FLOAT    = "FLOAT";       //$NON-NLS-1$ 
    public String HIGH     = "HIGH";        //$NON-NLS-1$ 
    public String PROT     = "PROT";        //$NON-NLS-1$ 
    public String LFLOAT   = "LFLOAT";      //$NON-NLS-1$ 
    public String CMPLX    = "CMPLX";       //$NON-NLS-1$ 
    public String IM       = "IM";          //$NON-NLS-1$ 
    public String RE       = "RE";          //$NON-NLS-1$ 
    public String MAX      = "MAX";         //$NON-NLS-1$ 
    public String MIN      = "MIN";         //$NON-NLS-1$ 
    public String ODD      = "ODD";         //$NON-NLS-1$ 
    public String ORD      = "ORD";         //$NON-NLS-1$ 
    public String SIZE     = "SIZE";        //$NON-NLS-1$ 
    public String TRUNC    = "TRUNC";       //$NON-NLS-1$ 
    public String LENGTH   = "LENGTH";      //$NON-NLS-1$ 
    public String ASH      = "ASH";         //$NON-NLS-1$ 
    public String LEN      = "LEN";         //$NON-NLS-1$ 
    public String ENTIER   = "ENTIER";      //$NON-NLS-1$ 
    public String LONG     = "LONG";        //$NON-NLS-1$ 
    public String SHORT    = "SHORT";       //$NON-NLS-1$ 
    public String FieldOfs = "FieldOfs";    //$NON-NLS-1$ 

    
    Set<String> STANDART_MODULE_SET = new HashSet<String>(Arrays.asList(
            SYSTEM,
            COMPILER
    ));
    
}
