package com.excelsior.xds.parser.modula.type;

public class OrdinalType extends Type {

    private final Number minValue;
    private final Number maxValue;

    public OrdinalType(String debugName, Number minValue, Number maxValue) {
        super(debugName);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public Number getMinValue() {
        return minValue;
    }

    public Number getMaxValue() {
        return maxValue;
    }
    
}
