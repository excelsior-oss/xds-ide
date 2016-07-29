package com.excelsior.xds.core.model;

import com.excelsior.xds.parser.modula.symbol.IRecordFieldSymbol;

public interface IXdsRecordField extends IXdsElementWithSymbol
{
	@Override
	public IRecordFieldSymbol getSymbol();
}
