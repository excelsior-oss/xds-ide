package com.excelsior.xds.parser.modula.symbol;

import java.util.Collection;

import com.excelsior.xds.core.text.TextPosition;

/**
 * Interface of a Modula-2 symbol which is providing information about symbols
 * are used by this symbol.
 */
public interface IModulaSymbolUsages
{
    /**
     * Returns all symbols with known usage information.
     * 
     * @return symbols with known usage information.
     */    
    public Collection<IModulaSymbol> getUsedSymbols();
    

   /**
    * Returns the positions in which the specified symbol is used,
    * or {@code null} if there is no usages of the symbol.
    * 
    * @param symbol the symbol whose usages is to be returned
    * @return the positions in which the specified symbol is used, or
    *         {@code null} if there is no usages of the symbol.
    */
    public Collection<TextPosition> getSymbolUsages(IModulaSymbol symbol);
    
    /**
     * Appends the specified position to the list of usages of the specified symbol. 
     *
     * @param symbol the symbol with which the specified position is to be associated.
     * @param usagePosition position to be associated with the specified symbol.
     */
    public void addSymbolUsage(IModulaSymbol symbol, TextPosition usagePosition);

}
