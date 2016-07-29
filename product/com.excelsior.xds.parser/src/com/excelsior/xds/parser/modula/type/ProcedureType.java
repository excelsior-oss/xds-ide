package com.excelsior.xds.parser.modula.type;

import com.excelsior.xds.parser.modula.symbol.type.IProcedureTypeSymbol;

public class ProcedureType extends Type 
{
    private final IProcedureTypeSymbol typeSymbol;

    public ProcedureType(String debugName, IProcedureTypeSymbol symbol) {
        super(debugName);
        typeSymbol = symbol;
    }

    public IProcedureTypeSymbol getSymbol() {
        return typeSymbol;
    }

}
