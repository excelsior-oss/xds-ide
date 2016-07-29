package com.excelsior.xds.core.model;

import java.util.Collection;

import com.excelsior.xds.parser.modula.symbol.IProcedureSymbol;

public interface IXdsProcedure extends IXdsElementWithDefinitions 
{
	@Override
	public IProcedureSymbol getSymbol();
	
	public ProcedureType getProcedureType();
	
    public Collection<IXdsFormalParameter> getParameters();

	boolean isForwardDeclaration();
	
}
