package com.excelsior.xds.core.model;

import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;

public interface IXdsFormalParameter extends IXdsElementWithSymbol
{
    @Override
    public IFormalParameterSymbol getSymbol();

}
