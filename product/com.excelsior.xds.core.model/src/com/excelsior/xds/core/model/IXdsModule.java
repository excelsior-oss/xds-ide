package com.excelsior.xds.core.model;

import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;

public interface IXdsModule extends IXdsElementWithDefinitions, IXdsElementWithSymbol 
{
	@Override
	public IModuleSymbol getSymbol();
}
