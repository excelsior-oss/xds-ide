package com.excelsior.xds.core.model;

import com.excelsior.xds.parser.modula.symbol.IVariableSymbol;

public interface IXdsVariable extends IXdsElementWithSymbol 
{
	@Override
	public IVariableSymbol getSymbol();
}
