package com.excelsior.xds.parser.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.type.IEnumTypeSymbol;

public interface IEnumElementSymbol extends IWholeConstantSymbol {

    /**
     * {@inheritDoc}
     */
    @Override
    public IEnumTypeSymbol getTypeSymbol();
 
}
