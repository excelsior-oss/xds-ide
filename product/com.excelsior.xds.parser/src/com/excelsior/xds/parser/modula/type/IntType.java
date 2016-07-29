package com.excelsior.xds.parser.modula.type;

public class IntType extends NumericalType {

    public IntType(String debugName) {  
        super(debugName, Short.MIN_VALUE, Short.MAX_VALUE);
    }
}
