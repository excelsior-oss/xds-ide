package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.ISymbolWithDefinitions;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.IOrdinalTypeSymbol;

public class BooleanConstantSymbol extends ConstantSymbol<IOrdinalTypeSymbol> {

    private final boolean value;
    
    public BooleanConstantSymbol( String name, ISymbolWithDefinitions parentScope
                                , IModulaSymbolReference<IOrdinalTypeSymbol> typeSymbolRef
                                , boolean value) 
    {
        super(name, parentScope, typeSymbolRef);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }
}
