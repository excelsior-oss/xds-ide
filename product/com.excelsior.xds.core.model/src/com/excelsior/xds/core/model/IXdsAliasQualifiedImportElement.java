package com.excelsior.xds.core.model;

import com.excelsior.xds.parser.modula.symbol.IModuleAliasSymbol;

public interface IXdsAliasQualifiedImportElement extends IXdsQualifiedImportElement 
{
	public IModuleAliasSymbol getAliasSymbol();
}
