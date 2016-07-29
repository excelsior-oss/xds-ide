package com.excelsior.xds.parser.modula.type;

public class BitSetType extends SetType {

    public BitSetType(String debugName, int length) {
        super(debugName, createBaseType(debugName + "_range", length));       //$NON-NLS-1$
    }
 
    private static OrdinalType createBaseType(String debugName, int length) {
        return new RangeType( debugName, XdsStandardTypes.CARD8
                            , (short)0, (short)(length - 1) 
                            );
    }
    
}
