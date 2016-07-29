package com.excelsior.xds.parser.modula.symbol.reference;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

public interface IChainedReference<T extends IModulaSymbol> extends IModulaSymbolReference<T>
                                                                  , Iterable<IModulaSymbolReference<T>> 
{
}
