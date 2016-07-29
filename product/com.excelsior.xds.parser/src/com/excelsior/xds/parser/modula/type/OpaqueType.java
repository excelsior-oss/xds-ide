package com.excelsior.xds.parser.modula.type;

import com.excelsior.xds.parser.modula.symbol.type.IOpaqueTypeSymbol;

public class OpaqueType extends Type
{
    private IOpaqueTypeSymbol typeSymbol;

    public OpaqueType(String debugName) {
        this(debugName, null);
    }

    public OpaqueType(String debugName, IOpaqueTypeSymbol typeSymbol) {
        super(debugName);
        setSymbol(typeSymbol);
    }
    
    public IOpaqueTypeSymbol getSymbol() {
        return typeSymbol;
    }

    public void setSymbol(IOpaqueTypeSymbol s) {
        this.typeSymbol = s;
    }
    
}
