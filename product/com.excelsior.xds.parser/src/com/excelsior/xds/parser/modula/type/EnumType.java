package com.excelsior.xds.parser.modula.type;

public class EnumType extends OrdinalType {

    private int elementCount; 
    
    public EnumType(String debugName, int elementCount) {
        super(debugName, 0, elementCount - 1);
    }
    
    public int getElementCount() {
        return elementCount;
    }

}
