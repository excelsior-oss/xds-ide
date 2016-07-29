package com.excelsior.xds.parser.commons.symbol;

/**
 * Engine for the indirect access to the symbol 
 * 
 * @author lion, lsa80
 */
public interface ISymbolReference<T extends ISymbol>
{
    /**
     * Obtains actual symbol
     */
    public T resolve();
}
