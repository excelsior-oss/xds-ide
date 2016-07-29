package com.excelsior.xds.parser.modula.type;

public class RealType extends NumericalType {

    public RealType(String debugName) {
        super(debugName, Float.MIN_VALUE, Float.MAX_VALUE);
    }
}
