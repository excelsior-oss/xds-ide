package com.excelsior.xds.core.model;

import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public interface IXdsType extends IXdsElement, IXdsElementWithSymbol {
	@Override
	public ITypeSymbol getSymbol();
}
