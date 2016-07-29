package com.excelsior.xds.parser.modula.type;

import com.excelsior.xds.parser.modula.symbol.type.IPointerTypeSymbol;

public class PointerType extends Type 
{
    private IPointerTypeSymbol typeSymbol;
    
    public PointerType(String debugName) {
        this(debugName, null);
    }

    public PointerType(String debugName, IPointerTypeSymbol typeSymbol) {
        super(debugName);
        this.setSymbol(typeSymbol);
    }
    
    public IPointerTypeSymbol getSymbol() {
        return typeSymbol;
    }

    public void setSymbol(IPointerTypeSymbol s) {
        this.typeSymbol = s;
    }
        
}
