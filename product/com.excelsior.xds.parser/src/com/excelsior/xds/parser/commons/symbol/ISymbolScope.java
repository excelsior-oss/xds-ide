package com.excelsior.xds.parser.commons.symbol;

/**
 * A symbol that has ability to hold other symbols.
 */
public interface ISymbolScope 
{

    public ISymbolScope getParentScope();
    
    /**
     * Searches for symbol in the current scope, and (recursively) in all parent scopes.
     * 
     * @param symbolName the name of symbol which is to be searched 
     * @return the first symbol with specified name in this and parent scope, or
     *         {@code null} if there is no symbol with specified name.
     */
    public ISymbol resolveName(String symbolName);
    
    /**
     * Searches for symbol in the current scope only.
     * 
     * @param symbolName the name of symbol which is to be searched.
     * @return the first symbol with specified name in this scope, or
     *         {@code null} if there is no symbol with specified name.
     */
    public ISymbol findSymbolInScope(String symbolName);
    
}
