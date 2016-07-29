package com.excelsior.xds.core.model;

import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;

public interface IXdsQualifiedImportElement extends IXdsImportElement
                                                  , IXdsElementWithSymbol 
{
	@Override
	public IModuleSymbol getSymbol();
}
