package com.excelsior.xds.parser.internal.modula.symbol.reference;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public interface IReferenceFactory {
	<T extends IModulaSymbol> IModulaSymbolReference<T> createRef(T symbol);
}
