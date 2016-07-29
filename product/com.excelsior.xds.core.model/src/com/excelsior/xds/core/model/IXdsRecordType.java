package com.excelsior.xds.core.model;

import java.util.Collection;

import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;

public interface IXdsRecordType extends IXdsCompositeType 
{
	@Override
	public IRecordTypeSymbol getSymbol();
	
	public Collection<IXdsRecordField> getFields();
	public Collection<IXdsProcedure> getProcedures();
}
