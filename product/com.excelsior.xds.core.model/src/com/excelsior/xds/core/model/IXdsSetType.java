package com.excelsior.xds.core.model;

import com.excelsior.xds.parser.modula.symbol.type.ISetTypeSymbol;

public interface IXdsSetType  extends IXdsCompositeType{
	@Override
	public ISetTypeSymbol getSymbol();
}
