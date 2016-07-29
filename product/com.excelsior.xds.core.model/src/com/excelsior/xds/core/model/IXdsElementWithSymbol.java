package com.excelsior.xds.core.model;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

public interface IXdsElementWithSymbol extends IXdsElement, ISourceBound {
	public IModulaSymbol getSymbol();
}
