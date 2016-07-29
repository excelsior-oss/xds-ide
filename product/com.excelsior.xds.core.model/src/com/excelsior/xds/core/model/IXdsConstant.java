package com.excelsior.xds.core.model;

import com.excelsior.xds.parser.modula.symbol.IConstantSymbol;

public interface IXdsConstant extends IXdsElement, IXdsElementWithSymbol {
	@Override
	public IConstantSymbol getSymbol();
}
