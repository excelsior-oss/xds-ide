package com.excelsior.xds.parser.modula.type;

import com.excelsior.xds.parser.modula.symbol.type.IOberonMethodTypeSymbol;

public class OberonMethodType extends ProcedureType 
{

    public OberonMethodType(String debugName, IOberonMethodTypeSymbol symbol) {
        super(debugName, symbol);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IOberonMethodTypeSymbol getSymbol() {
        return (IOberonMethodTypeSymbol)super.getSymbol();
    }
    
}
