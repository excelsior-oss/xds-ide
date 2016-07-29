package com.excelsior.xds.parser.commons.symbol;

import java.util.Map;

import org.apache.commons.collections.map.CompositeMap;

/**
 * A scope of a symbol.
 */
public class SymbolScope implements ISymbolScope 
{
    private ISymbolScope parentScope;
    private CompositeMap allSymbols;
    
    public SymbolScope(ISymbolScope parentScope) {
        this.parentScope = parentScope;
        allSymbols = new CompositeMap();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ISymbolScope getParentScope() {
        return parentScope;
    }


    public void attach(Map<String, ? extends ISymbol> collection) {
        allSymbols.addComposited(collection);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public ISymbol resolveName(String name) {
        ISymbol s = findSymbolInScope(name);
        if (s == null) {
            ISymbolScope parentScope = getParentScope();
            if (parentScope != null) {
                s = parentScope.resolveName(name);
            }
        }
        return s;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public ISymbol findSymbolInScope(String name) {
        return (ISymbol) allSymbols.get(name);
    }
        
}
