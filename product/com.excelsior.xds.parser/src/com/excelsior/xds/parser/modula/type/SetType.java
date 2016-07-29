package com.excelsior.xds.parser.modula.type;

public class SetType extends OrdinalType {

    private OrdinalType baseType;
   
    public SetType(String debugName, OrdinalType baseType) {
        super(debugName, baseType.getMinValue(), baseType.getMaxValue());
        this.baseType = baseType;
    }

    public OrdinalType getBaseType() {
        return baseType;
    }

}
