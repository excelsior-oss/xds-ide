package com.excelsior.xds.parser.modula.type;

public class LongLongRealType extends NumericalType {

    public LongLongRealType(String debugName) {
        //TODO MIN and MAX value should be adjusted 
        super(debugName, Double.MIN_VALUE, Double.MAX_VALUE);
    }

}
