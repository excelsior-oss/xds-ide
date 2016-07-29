package com.excelsior.xds.core.model;

import com.excelsior.xds.parser.modula.symbol.IEnumElementSymbol;

public interface IXdsEnumElement extends IXdsConstant {
	@Override
	public IEnumElementSymbol getSymbol();
}
