package com.excelsior.xds.core.model;

import java.util.Collection;

public interface IXdsElementWithDefinitions extends IXdsElementWithSymbol
                                                  , IXdsContainer
{
    public IXdsCompilationUnit getCompilationUnit();
	public Collection<IXdsModule> getModules();
	public Collection<IXdsProcedure> getProcedures();
	public Collection<IXdsVariable> getVariables();
	public Collection<IXdsConstant> getConstants();
	public Collection<IXdsType> getTypes();
}
