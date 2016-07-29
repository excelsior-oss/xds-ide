package com.excelsior.xds.parser.modula.symbol.binding;

import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;

public interface IModulaSymbolCacheListener {
	void moduleSymbolAdded(IModuleSymbol oldSymbol, IModuleSymbol newSymbol);
	void moduleSymbolRemoved(IModuleSymbol symbol);
}
