package com.excelsior.xds.parser.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.type.IOberonMethodTypeSymbol;

public interface IOberonMethodSymbol extends IProcedureSymbol
{
    /**
     * {@inheritDoc}
     */
    @Override
    public IOberonMethodTypeSymbol getTypeSymbol();

    public IOberonMethodReceiverSymbol getReceiverSymbol();
    
}
