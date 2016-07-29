package com.excelsior.xds.parser.modula.type;

public class RangeType extends NumericalType {
    
    private OrdinalType baseType;
    
    public RangeType( String debugName, OrdinalType baseType
                    , Number minValue, Number maxValue ) 
    {
        super(debugName, minValue, maxValue);
        this.baseType = baseType;
    }

    protected OrdinalType getBaseType() {
        return baseType;
    }
    
}
