package com.excelsior.xds.core.model;

import com.excelsior.xds.parser.modula.symbol.type.IEnumTypeSymbol;

public interface IXdsEnumType extends IXdsCompositeType 
{
	@Override
	public IEnumTypeSymbol getSymbol();
}
