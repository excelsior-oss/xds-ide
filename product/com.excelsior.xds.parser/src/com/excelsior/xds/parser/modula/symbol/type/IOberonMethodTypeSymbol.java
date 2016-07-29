package com.excelsior.xds.parser.modula.symbol.type;

import com.excelsior.xds.parser.modula.symbol.IOberonMethodReceiverSymbol;
import com.excelsior.xds.parser.modula.type.OberonMethodType;

public interface IOberonMethodTypeSymbol extends IProcedureTypeSymbol 
{
    /**
     * {@inheritDoc}
     */
    @Override
    public OberonMethodType getType();
    
    public IOberonMethodReceiverSymbol getReceiverSymbol();
    
}
