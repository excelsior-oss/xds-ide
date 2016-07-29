package com.excelsior.xds.parser.modula.symbol.binding;

import static com.excelsior.xds.parser.internal.modula.symbol.reference.ReferenceFactory.createStaticRef;

import com.excelsior.xds.parser.internal.modula.symbol.BooleanConstantSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.ConstantSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.StandardModuleSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.StandardProcedureSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.WholeConstantSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IStaticModulaSymbolReference;
import com.excelsior.xds.parser.internal.modula.symbol.reference.InternalReferenceUtils;
import com.excelsior.xds.parser.internal.modula.symbol.reference.StaticRefFactory;
import com.excelsior.xds.parser.internal.modula.symbol.type.ArrayTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.NumericalTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.OrdinalTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.PointerTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.ProcedureTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.RangeTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.SetTypeSymbol;
import com.excelsior.xds.parser.internal.modula.symbol.type.TypeSymbol;
import com.excelsior.xds.parser.modula.XdsStandardNames;
import com.excelsior.xds.parser.modula.symbol.IConstantSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IOrdinalTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IPointerTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.type.NumericalType;
import com.excelsior.xds.parser.modula.type.OrdinalType;
import com.excelsior.xds.parser.modula.type.SetType;
import com.excelsior.xds.parser.modula.type.Type;
import com.excelsior.xds.parser.modula.type.XdsStandardTypes;

abstract class StandardModuleBuilder implements XdsStandardNames 
{
    private static int BITSPERLOC_VALUE  = 8;
    private static int LOCSPERWORD_VALUE = 4;
    private static int LOCSPERBYTE_VALUE = 1;
    
    private static final StaticRefFactory staticRefFactory = StaticRefFactory.instance();

    public static StandardModuleSymbol buildStandardModuleSymbol(boolean isOberon) {
        StandardModuleSymbol superModule;
        if (isOberon) {
            superModule = new StandardModuleSymbol(OBERON_SUPERMODULE, true);
            buildOberonSuperModule(superModule);
        }
        else {
            superModule = new StandardModuleSymbol(MODULA_SUPERMODULE, false);
            buildModulaSuperModule(superModule);
        }
        return superModule;
    }
    
    public static StandardModuleSymbol buildSystemModuleSymbol(boolean isOberon) {
        StandardModuleSymbol systemModule = new StandardModuleSymbol(SYSTEM, isOberon); 
        buildSystemModule(systemModule, isOberon);
        return systemModule;
    }
    
    public static StandardModuleSymbol buildCompilerModuleSymbol() {
        StandardModuleSymbol compilerModule = new StandardModuleSymbol(COMPILER, false); 
        buildCompilerModule(compilerModule);
        return compilerModule;
    }
    
    private static void buildModulaSuperModule(StandardModuleSymbol parentModule) {
        addNumericalTypeSymbol(INTEGER, parentModule, XdsStandardTypes.INT32);    
        addNumericalTypeSymbol(CARDINAL, parentModule, XdsStandardTypes.CARD32);  
// IF env.config.Option(M2ADDTYPES) THEN
        addNumericalTypeSymbol(SHORTINT, parentModule, XdsStandardTypes.INT8);    
        addNumericalTypeSymbol(LONGINT, parentModule, XdsStandardTypes.INT32);    
        addNumericalTypeSymbol(LONGLONGINT, parentModule, XdsStandardTypes.INT64);

        addNumericalTypeSymbol(SHORTCARD, parentModule, XdsStandardTypes.CARD8);     
        addNumericalTypeSymbol(LONGCARD, parentModule, XdsStandardTypes.CARD32);     
        addNumericalTypeSymbol(LONGLONGCARD, parentModule, XdsStandardTypes.CARD64); 
// END;        
        addOrdinalTypeSymbol(BOOLEAN, parentModule, XdsStandardTypes.BOOLEAN);  
        addOrdinalTypeSymbol(CHAR, parentModule, XdsStandardTypes.CHAR);        
        
        addNumericalTypeSymbol(REAL, parentModule, XdsStandardTypes.REAL);           
        addNumericalTypeSymbol(LONGREAL, parentModule, XdsStandardTypes.LONG_REAL);  
// <* IF TARGET_386 OR TARGET_VAX OR MERINS THEN *>
        addNumericalTypeSymbol(LONGLONGREAL, parentModule, XdsStandardTypes.LONGLONG_REAL); 
// <* END *>
// IF NOT oberon THEN
        addProcedureTypeSymbol(PROC, parentModule);   
        addBitSetTypeSymbol(BITSET, parentModule, XdsStandardTypes.SET32, SHORTCARD);    
// <* IF TARGET64 THEN *>
        addBitSetTypeSymbol(BITSET64, parentModule, XdsStandardTypes.SET64, SHORTCARD);  
// <* END *>
        addNumericalTypeSymbol(PROTECTION, parentModule, XdsStandardTypes.PROTECTION);  
// END;
// IF NOT oberon OR o2_num_ext THEN
        addTypeSymbol(COMPLEX, parentModule, XdsStandardTypes.COMPLEX);           
        addTypeSymbol(LONGCOMPLEX, parentModule, XdsStandardTypes.LONG_COMPLEX);  
// END;
        
        addPervasiveConstants(parentModule);
        addStandardProcedures(parentModule, false);
    }
    
    private static void buildOberonSuperModule(StandardModuleSymbol parentModule) {
        addNumericalTypeSymbol(SHORTINT, parentModule, XdsStandardTypes.INT8); 
        addNumericalTypeSymbol(INTEGER, parentModule, XdsStandardTypes.INT16); 
        addNumericalTypeSymbol(LONGINT, parentModule, XdsStandardTypes.INT32); 
        addNumericalTypeSymbol(LONGLONGINT, parentModule, XdsStandardTypes.INT64);   
        addNumericalTypeSymbol(LONGLONGCARD, parentModule, XdsStandardTypes.CARD64); 

        addOrdinalTypeSymbol(BOOLEAN, parentModule, XdsStandardTypes.BOOLEAN);     
        addOrdinalTypeSymbol(CHAR, parentModule, XdsStandardTypes.CHAR);           
        
        addNumericalTypeSymbol(REAL, parentModule, XdsStandardTypes.REAL);           
        addNumericalTypeSymbol(LONGREAL, parentModule, XdsStandardTypes.LONG_REAL);  
// <* IF TARGET_386 OR TARGET_VAX OR MERINS THEN *>        
        addNumericalTypeSymbol(LONGLONGREAL, parentModule, XdsStandardTypes.LONGLONG_REAL); 
// <* END *>        
        addBitSetTypeSymbol(SET, parentModule, XdsStandardTypes.SET32, "#CARD8#");   //$NON-NLS-1$ 
// IF NOT oberon OR o2_num_ext THEN        
        addTypeSymbol(COMPLEX, parentModule, XdsStandardTypes.COMPLEX);          
        addTypeSymbol(LONGCOMPLEX, parentModule, XdsStandardTypes.LONG_COMPLEX); 
// <* END *>        

        addPervasiveConstants(parentModule);
        addStandardProcedures(parentModule, true);
    }
    
    private static void buildSystemModule(StandardModuleSymbol parentModule, boolean isOberon) {
        addSystemModuleTypes(parentModule, isOberon);

        IOrdinalTypeSymbol card8TypeSymbol = (IOrdinalTypeSymbol)parentModule.resolveName(CARD8);   
        IStaticModulaSymbolReference<IOrdinalTypeSymbol> card8TypeSymbolRef = createStaticRef(card8TypeSymbol);
        addConstant(new WholeConstantSymbol<IOrdinalTypeSymbol>(BITSPERLOC,  parentModule, card8TypeSymbolRef, BITSPERLOC_VALUE), parentModule);    
        addConstant(new WholeConstantSymbol<IOrdinalTypeSymbol>(LOCSPERWORD, parentModule, card8TypeSymbolRef, LOCSPERWORD_VALUE), parentModule);    
        addConstant(new WholeConstantSymbol<IOrdinalTypeSymbol>(LOCSPERBYTE, parentModule, card8TypeSymbolRef, LOCSPERBYTE_VALUE), parentModule);    
        
        addStandardProcedure(new StandardProcedureSymbol(ADDADR, parentModule), parentModule);  
        addStandardProcedure(new StandardProcedureSymbol(SUBADR, parentModule), parentModule);  
        addStandardProcedure(new StandardProcedureSymbol(DIFADR, parentModule), parentModule);  
        addStandardProcedure(new StandardProcedureSymbol(DIFADRC, parentModule), parentModule); 
        addStandardProcedure(new StandardProcedureSymbol(MAKEADR, parentModule), parentModule); 
        if (isOberon){
            addStandardProcedure(new StandardProcedureSymbol(ADR, parentModule), parentModule); 
        }
        else{
            addStandardProcedure(new StandardProcedureSymbol(ADR, parentModule), parentModule); 
        }
        addStandardProcedure(new StandardProcedureSymbol(M2ADR, parentModule), parentModule);   
        addStandardProcedure(new StandardProcedureSymbol(SHIFT, parentModule), parentModule);   
        addStandardProcedure(new StandardProcedureSymbol(ROTATE, parentModule), parentModule);  
        if (!isOberon) {
            addStandardProcedure(new StandardProcedureSymbol(CAST, parentModule), parentModule); 
        }
        addStandardProcedure(new StandardProcedureSymbol(TSIZE, parentModule), parentModule);   
        addStandardProcedure(new StandardProcedureSymbol(ROT, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(LSH, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(NEW, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(DISPOSE, parentModule), parentModule); 

        if (isOberon) {
            PointerTypeSymbol addrTypeSymbol = (PointerTypeSymbol)parentModule.resolveName(ADDRESS);    
            addType(parentModule, addrTypeSymbol.createSynonym(PTR, parentModule, staticRefFactory)); 
        }
        
        addStandardProcedure(new StandardProcedureSymbol(CC, parentModule), parentModule);    
        addStandardProcedure(new StandardProcedureSymbol(BIT, parentModule), parentModule);   
        
        if (isOberon) {
            addStandardProcedure(new StandardProcedureSymbol(VAL, parentModule), parentModule); 
        }
        addStandardProcedure(new StandardProcedureSymbol(GET, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(GETREG, parentModule), parentModule);  
        addStandardProcedure(new StandardProcedureSymbol(PUT, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(PUTREG, parentModule), parentModule);  
        addStandardProcedure(new StandardProcedureSymbol(MOVE, parentModule), parentModule);    
        addStandardProcedure(new StandardProcedureSymbol(CODE, parentModule), parentModule);    
        addStandardProcedure(new StandardProcedureSymbol(BYTES, parentModule), parentModule);   
        addStandardProcedure(new StandardProcedureSymbol(BITS, parentModule), parentModule);    
        addStandardProcedure(new StandardProcedureSymbol(REF, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(VALID, parentModule), parentModule);   
        addStandardProcedure(new StandardProcedureSymbol(FILL, parentModule), parentModule);    

// IF pcS.TS_ext() THEN
        addStandardProcedure(new StandardProcedureSymbol(EVAL, parentModule), parentModule);    
// END;

        addStandardProcedure(new StandardProcedureSymbol(FIELDOFS, parentModule), parentModule); 
        addStandardProcedure(new StandardProcedureSymbol(PRED, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(SUCC, parentModule), parentModule);     
    }
    
    private static void addSystemModuleTypes(StandardModuleSymbol parentModule, boolean isOberon) {
        addNumericalTypeSymbol(INT8,  parentModule, XdsStandardTypes.INT8);    
        addNumericalTypeSymbol(CARD8, parentModule, XdsStandardTypes.CARD8);   
        
        addNumericalTypeSymbol(INT16,  parentModule, XdsStandardTypes.INT16);  
        addNumericalTypeSymbol(CARD16, parentModule, XdsStandardTypes.CARD16); 
        
        addNumericalTypeSymbol(INT32,  parentModule, XdsStandardTypes.INT32);  
        addNumericalTypeSymbol(CARD32, parentModule, XdsStandardTypes.CARD32); 

        addBitSetTypeSymbol(SET8,  parentModule, XdsStandardTypes.SET8, CARD8);    
        addBitSetTypeSymbol(SET16, parentModule, XdsStandardTypes.SET16, CARD8);   
        addBitSetTypeSymbol(SET32, parentModule, XdsStandardTypes.SET32, CARD8);           
// <* IF TARGET64 OR TARGET_MIPS OR TARGET_386 THEN *>
        addBitSetTypeSymbol(SET64, parentModule, XdsStandardTypes.SET64, CARD8);   
// <* END *>

        addOrdinalTypeSymbol(BOOL8,  parentModule, XdsStandardTypes.BOOLEAN);  
        addOrdinalTypeSymbol(BOOL16, parentModule, XdsStandardTypes.BOOLEAN);  
        addOrdinalTypeSymbol(BOOL32, parentModule, XdsStandardTypes.BOOLEAN);  

        addNumericalTypeSymbol(Int, parentModule, XdsStandardTypes.INT32);       
        addNumericalTypeSymbol(Unsigned, parentModule, XdsStandardTypes.CARD32); 
        addNumericalTypeSymbol(Size_t, parentModule, XdsStandardTypes.CARD32);   
        addTypeSymbol(Void, parentModule, XdsStandardTypes.VOID);       

        addNumericalTypeSymbol(INT, parentModule, XdsStandardTypes.INT32);     
        addNumericalTypeSymbol(CARD, parentModule, XdsStandardTypes.CARD32);   

// <* IF  TARGET64 OR TARGET_MIPS OR TARGET_386 THEN *>
        addNumericalTypeSymbol(INT64, parentModule, XdsStandardTypes.INT64);   
        addNumericalTypeSymbol(CARD64, parentModule, XdsStandardTypes.CARD64); 
// <* END *>

        addNumericalTypeSymbol(INDEX, parentModule, XdsStandardTypes.CARD32);     
        addTypeSymbol(LOC, parentModule, XdsStandardTypes.LOC);      
        addTypeSymbol(BYTE, parentModule, XdsStandardTypes.BYTE);      
        addWordTypeSymbol(WORD, parentModule);

        addAddressType(ADDRESS, parentModule);
    }

    private static void buildCompilerModule(StandardModuleSymbol parentModule) {
        addStandardProcedure(new StandardProcedureSymbol(TARGET, parentModule), parentModule);    
        addStandardProcedure(new StandardProcedureSymbol(OPTION, parentModule), parentModule);    
        addStandardProcedure(new StandardProcedureSymbol(EQUATION, parentModule), parentModule);  
        addStandardProcedure(new StandardProcedureSymbol(TIMESTAMP, parentModule), parentModule); 
    }
    
    private static void addPervasiveConstants(StandardModuleSymbol parentModule) {
        IOrdinalTypeSymbol typeSymbol = (IOrdinalTypeSymbol)parentModule.resolveName(BOOLEAN);
        IStaticModulaSymbolReference<IOrdinalTypeSymbol> typeSymbolRef = createStaticRef(typeSymbol);
        addConstant(new BooleanConstantSymbol(TRUE,  parentModule, typeSymbolRef, true), parentModule);   
        addConstant(new BooleanConstantSymbol(FALSE, parentModule, typeSymbolRef, false), parentModule);  

        typeSymbol = (IOrdinalTypeSymbol)parentModule.resolveName(PROTECTION);    
        typeSymbolRef = createStaticRef(typeSymbol);
        addConstant(new WholeConstantSymbol<IOrdinalTypeSymbol>(INTERRUPTIBLE,   parentModule, typeSymbolRef, 0), parentModule);    
        addConstant(new WholeConstantSymbol<IOrdinalTypeSymbol>(UNINTERRUPTIBLE, parentModule, typeSymbolRef, 1), parentModule);    

        IPointerTypeSymbol addressTypeSymbol = (IPointerTypeSymbol)parentModule.resolveName(ADDRESS);
        addConstant(new ConstantSymbol<IPointerTypeSymbol>(NIL, parentModule, createStaticRef(addressTypeSymbol)), parentModule);    
    }

    private static void addStandardProcedures(StandardModuleSymbol parentModule, boolean isOberon) {
        addStandardProcedure(new StandardProcedureSymbol(ABS, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(DEC, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(DISPOSE, parentModule), parentModule); 
        addStandardProcedure(new StandardProcedureSymbol(INC, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(INCL, parentModule), parentModule);    
        addStandardProcedure(new StandardProcedureSymbol(EXCL, parentModule), parentModule);    
        addStandardProcedure(new StandardProcedureSymbol(NEW, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(HALT, parentModule), parentModule);    

// IF oberon OR lang_ext THEN
        addStandardProcedure(new StandardProcedureSymbol(ASSERT, parentModule), parentModule);  
        addStandardProcedure(new StandardProcedureSymbol(COPY, parentModule), parentModule);    
// END;

// IF lang_ext THEN
        addStandardProcedure(new StandardProcedureSymbol(RESIZE, parentModule), parentModule);  
// END;
        addStandardProcedure(new StandardProcedureSymbol(CAP, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(CHR, parentModule), parentModule);     

        if (!isOberon) {
            addStandardProcedure(new StandardProcedureSymbol(FLOAT, parentModule), parentModule);   
            addStandardProcedure(new StandardProcedureSymbol(HIGH, parentModule), parentModule);    
            addStandardProcedure(new StandardProcedureSymbol(PROT, parentModule), parentModule);    
            addStandardProcedure(new StandardProcedureSymbol(INT, parentModule), parentModule);     
            addStandardProcedure(new StandardProcedureSymbol(LFLOAT, parentModule), parentModule);  
        }
       
// IF NOT oberon OR o2_num_ext THEN
        addStandardProcedure(new StandardProcedureSymbol(CMPLX, parentModule), parentModule);   
        addStandardProcedure(new StandardProcedureSymbol(IM, parentModule), parentModule);      
        addStandardProcedure(new StandardProcedureSymbol(RE, parentModule), parentModule);      
// END;
        addStandardProcedure(new StandardProcedureSymbol(MAX, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(MIN, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(ODD, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(ORD, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(SIZE, parentModule), parentModule);    

        if (!isOberon) {
            addStandardProcedure(new StandardProcedureSymbol(TRUNC, parentModule), parentModule);   
        }

        // IF NOT oberon OR lang_ext THEN
        addStandardProcedure(new StandardProcedureSymbol(LENGTH, parentModule), parentModule);  
        addStandardProcedure(new StandardProcedureSymbol(VAL, parentModule), parentModule);     
// END;
// IF oberon OR lang_ext THEN
        addStandardProcedure(new StandardProcedureSymbol(ASH, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(LEN, parentModule), parentModule);     
        addStandardProcedure(new StandardProcedureSymbol(ENTIER, parentModule), parentModule);  
// END;
        
        if (isOberon) {
            addStandardProcedure(new StandardProcedureSymbol(LONG, parentModule), parentModule);    
            addStandardProcedure(new StandardProcedureSymbol(SHORT, parentModule), parentModule);   
        }
        
// IF pcS.TS_ext() THEN
        addStandardProcedure(new StandardProcedureSymbol(FieldOfs, parentModule), parentModule);   
// END;
    }


    private static <T extends Type> TypeSymbol<T> addTypeSymbol( 
        String typeName, StandardModuleSymbol parentModule, T type ) 
    {
        TypeSymbol<T> typeSymbol = new TypeSymbol<T>(typeName, parentModule, type); 
        addType(parentModule, typeSymbol);
        return typeSymbol;
    }

    private static <T extends OrdinalType> OrdinalTypeSymbol<T> addOrdinalTypeSymbol(
        String typeName, StandardModuleSymbol parentModule, T type )
    {
        OrdinalTypeSymbol<T> typeSymbol = new OrdinalTypeSymbol<T>(typeName, parentModule, type); 
        addType(parentModule, typeSymbol);
        return typeSymbol;
    }

    private static void addStandardProcedure( StandardProcedureSymbol symbol
                                            , StandardModuleSymbol parentModule )
    {
        parentModule.add(symbol);        
        InternalReferenceUtils.addSymbolForResolving(parentModule, symbol);
    }
    
    private static void addConstant( IConstantSymbol symbol
                                   , StandardModuleSymbol parentModule ) 
    {
        parentModule.addConstant(symbol);
        InternalReferenceUtils.addSymbolForResolving(parentModule, symbol);
    }
    
    private static ProcedureTypeSymbol addProcedureTypeSymbol
                                      ( String typeName
                                      , StandardModuleSymbol parentModule )
    {
        ProcedureTypeSymbol typeSymbol = new ProcedureTypeSymbol(typeName, parentModule);
        addType(parentModule, typeSymbol);
        return typeSymbol;
    }
    
    private static NumericalTypeSymbol addNumericalTypeSymbol(
        String typeName, StandardModuleSymbol parentModule, NumericalType type ) 
    {
        NumericalTypeSymbol typeSymbol = new NumericalTypeSymbol(typeName, parentModule, type);
        addType(parentModule, typeSymbol);
        return typeSymbol;
    }

    private static SetTypeSymbol addBitSetTypeSymbol(
        String typeName, StandardModuleSymbol parentModule, 
        SetType type, String rangeBaseTypeName ) 
    {
        SetTypeSymbol setTypeSymbol = new SetTypeSymbol(typeName, parentModule, true);
    
        IOrdinalTypeSymbol rangeBaseTypeSymbol = (IOrdinalTypeSymbol)parentModule.resolveName(rangeBaseTypeName);
        if (rangeBaseTypeSymbol == null) {
            rangeBaseTypeSymbol = new NumericalTypeSymbol(
                rangeBaseTypeName, parentModule, XdsStandardTypes.CARD8
            );
        }

        RangeTypeSymbol rangeTypeSymbol = new RangeTypeSymbol( 
            typeName + "_range", parentModule, rangeBaseTypeSymbol,
            type.getBaseType().getMinValue(), 
            type.getBaseType().getMaxValue() 
        );
        setTypeSymbol.setBaseTypeSymbol(rangeTypeSymbol);
        addType(parentModule, setTypeSymbol);
        return setTypeSymbol;
    }
 
    /**
     * The type WORD is defined as 
     *   CONST LOCSPERWORD = 4; 
     *   TYPE WORD = ARRAY [0..LOCSPERWORD-1] OF LOC; 
     * and the value of the call TSIZE(WORD) is equal to LOCSPERWORD. 
     * The only operation directly defined for the WORD type is an assignment. 
     * There are special rules affecting parameter compatibility for system 
     * storage types. See System parameter compatibility for further details.
     */
    private static void addWordTypeSymbol( String typeName
                                         , StandardModuleSymbol parentModule ) 
    {
        IOrdinalTypeSymbol indexBaseTypeSymbol = (IOrdinalTypeSymbol)parentModule.resolveName(INDEX);
        RangeTypeSymbol indexTypeSymbol = new RangeTypeSymbol( 
            typeName + "_index", parentModule, indexBaseTypeSymbol, 
            0, (LOCSPERWORD_VALUE - 1)
        );
        
        ITypeSymbol elementTypeSymbol = (ITypeSymbol) parentModule.resolveName(LOC);

        ArrayTypeSymbol wordTypeSymbol = new ArrayTypeSymbol( typeName, parentModule
                                                            , staticRefFactory.createRef((IOrdinalTypeSymbol)indexTypeSymbol)
                                                            , staticRefFactory.createRef(elementTypeSymbol) );
        addType(parentModule, wordTypeSymbol);
    }
    
    private static void addAddressType(String typeName, StandardModuleSymbol parentModule) 
    {
        PointerTypeSymbol addressPointerType = new PointerTypeSymbol(typeName, parentModule, XdsStandardTypes.ADDRESS);
        addType(parentModule, addressPointerType);
    }
    
    private static void addType(StandardModuleSymbol parentModule, ITypeSymbol typeSymbol) {
        parentModule.addType(typeSymbol);
        InternalReferenceUtils.addSymbolForResolving(parentModule, typeSymbol);
    }
}