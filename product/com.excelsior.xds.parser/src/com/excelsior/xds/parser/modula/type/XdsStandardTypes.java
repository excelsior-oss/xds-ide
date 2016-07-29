package com.excelsior.xds.parser.modula.type;

public interface XdsStandardTypes 
{
    VoidType VOID = new VoidType("void");   //$NON-NLS-1$
    
    /**
     * Values of the LOC type are the uninterpreted contents of the smallest  
     * addressable unit of a storage in implementation. 
     * The value of the call TSIZE(LOC) is therefore equal to one. 
     */
    LocType LOC   = new LocType("loc");     //$NON-NLS-1$
    
    /**
     * BYTE is defined as LOC and has all the properties of the type LOC.
     */
    LocType BYTE = LOC;

    /**
     * The ADDRESS type is an assignment compatible with all pointer types and
     * vice versa (See Assignment compatibility). A formal variable parameter of
     * the ADDRESS type is a parameter compatible with an actual parameter of
     * any pointer type. Variables of type ADDRESS are no longer expression
     * compatible with CARDINAL (as it was in PIM) and they cannot directly
     * occur in expressions that include arithmetic operators. Functions ADDADR,
     * SUBADR and DIFADR were introduced for address arithmetic.
     */
    PointerType ADDRESS = new PointerType("address");   //$NON-NLS-1$
    
    
    BooleanType BOOLEAN = new BooleanType("boolean");   //$NON-NLS-1$
    CharType    CHAR    = new CharType("char");         //$NON-NLS-1$
    
    ShortIntType    INT8  = new ShortIntType("int8");      //$NON-NLS-1$
    IntType         INT16 = new IntType("int16");          //$NON-NLS-1$
    LongIntType     INT32 = new LongIntType("int32");      //$NON-NLS-1$
    LongLongIntType INT64 = new LongLongIntType("int64");  //$NON-NLS-1$
    
    ShortCardType    CARD8  = new ShortCardType("card8");      //$NON-NLS-1$
    CardType         CARD16 = new CardType("card16");          //$NON-NLS-1$
    LongCardType     CARD32 = new LongCardType("card32");      //$NON-NLS-1$
    LongLongCardType CARD64 = new LongLongCardType("card64");  //$NON-NLS-1$
    
    RealType         REAL          = new RealType("real");                   //$NON-NLS-1$
    LongRealType     LONG_REAL     = new LongRealType("longreal");           //$NON-NLS-1$
    LongLongRealType LONGLONG_REAL = new LongLongRealType("longlongreal");  //$NON-NLS-1$
    
    ComplexType COMPLEX      = new ComplexType("complex");        //$NON-NLS-1$
    ComplexType LONG_COMPLEX = new ComplexType("longcomplex");    //$NON-NLS-1$
    
    BitSetType SET8  = new BitSetType("set8",   8);    //$NON-NLS-1$
    BitSetType SET16 = new BitSetType("set16", 16);    //$NON-NLS-1$
    BitSetType SET32 = new BitSetType("set16", 32);    //$NON-NLS-1$
    BitSetType SET64 = new BitSetType("set64", 64);    //$NON-NLS-1$

    NumericalType PROTECTION = new NumericalType("protection", Short.MIN_VALUE, Short.MAX_VALUE);   //$NON-NLS-1$

}