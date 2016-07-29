package com.excelsior.xds.parser.modula.type;

public class ArrayType extends Type {

    private final OrdinalType indexType;
    private final Type        elementType;
    
    public ArrayType(String debugName, OrdinalType indexType, Type elementType) {
        super(debugName);
        this.indexType = indexType;
        this.elementType = elementType;
    }

    public Type getElementType() {
        return elementType;
    }
    
    public OrdinalType getIndexType() {
        return indexType;
    }
    
}
