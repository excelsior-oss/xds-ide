package com.excelsior.xds.parser.modula.symbol.reference;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

/**
 * Delegates all reference resolve requests to the inner reference 
 */
public interface IProxyReference<T extends IModulaSymbol> extends IModulaSymbolReference<T>
{
    void setReference(IModulaSymbolReference<T> reference);
    IModulaSymbolReference<T> getReference();
}
