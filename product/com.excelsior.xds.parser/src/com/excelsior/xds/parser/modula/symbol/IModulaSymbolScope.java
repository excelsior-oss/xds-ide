package com.excelsior.xds.parser.modula.symbol;

import com.excelsior.xds.parser.commons.symbol.ISymbolScope;

/**
 * A scope of a Modula-2/Oberon-2 symbol.
 */
public interface IModulaSymbolScope extends ISymbolScope, Iterable<IModulaSymbol>  
{
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbolScope getParentScope();
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol resolveName(String symbolName);

    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(String symbolName);
    
    
    /**
     * Searches for symbol in the current scope only, respecting whether it 
     * is public or not.
     * 
     * @param symbolName the name of symbol which is to be searched
     * @return the first symbol with specified name in this scope and required public attribute, or
     *         {@code null} if there is no such symbol.
     */
    public IModulaSymbol findSymbolInScope(String symbolName, boolean isPublic);

}