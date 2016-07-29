package com.excelsior.xds.parser.modula.symbol;

import java.util.EnumSet;

public enum SymbolAttribute {
    
    /**
     * Symbol is to be available for use in other modules by import from this module.
     */
    PUBLIC, 
    
    /**
     * Symbol is exported from local module.
     */
    EXPORTED,     
    
    /**
     * Qualified Export from local module.
     */
    QUALIFIED_EXPORT,     
    
    /**
     * Symbol with read-only access.
     */
    READ_ONLY,
    
    /**
     * Symbol is defined as volatile. The value of the symbol may change in a way
     * that can not be determined at compile time.
     */
    VOLATILE,
    
    /**
     * Symbol is external, its declaration is located in the separate module.
     */
    EXTERNAL,
    
    /**
     * Symbol is the forward declaration of the another symbol.
     */
    FORWARD_DECLARATION,
    
    /**
     * Generation of the BEGIN-part is disabled for this module.
     */
    NOMODULEINIT,
    
    /**
     * Symbol is variable parameter. The formal parameter refers to the same variable 
     * as the actual, so assignments to the formal parameter also change 
     * the value of the variable that is passed as an actual parameter.
     */
    VAR_PARAMETER,
    
    /**
     * Value of variable parameter may be NIL.
     */
    NIL_ALLOWED,

    /**
     * Symbol is variable number of parameters. The last formal parameter of a procedure 
     * may be declared as a “sequence of bytes” (SEQ-parameter). In a procedure 
     * call, any (possibly empty) sequence of actual parameters of any types 
     * may be substituted in place of that parameter.
     */
    SEQ_PARAMETER,
    
    /**
     * Symbol is procedure parameter with default value. 
     */
    DEFAULT,
    
    /**
     * The name of symbol wasn't specified. 
     */
    ANONYMOUS_NAME,
    
    /**
     * Symbol with the same name was already defined in the parent scope. 
     */
    ALREADY_DEFINED,
    
    /**
     * Symbol is pervasive identifier of Modula-2/Oberon-2 language or 
     * identifier from standard modules: SYSTEM and COMPILER. 
     */
    PERVASIVE,
    
    /**
     * Symbol was reconstructed from sym-file or odf-file. 
     */
    RECONSTRUCTED,
    
    /**
     * Symbol is record type with variant fields
     */
    VARIANT_RECORD
    ;

    
    static public EnumSet<SymbolAttribute> createEmptySet() {
        return EnumSet.noneOf(SymbolAttribute.class);
    }
    
}
