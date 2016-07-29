package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.map.CompositeMap;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;

/**
 * A Modula-2/Oberon-2 symbol with its own scope to hold other symbols.
 */
public abstract class SymbolWithScope extends    ModulaSymbol 
                                      implements ISymbolWithScope 
{
    private final CompositeMap allSymbols;

    public SymbolWithScope(String name, ISymbolWithScope parentScope) {
        super(name, parentScope); 
        allSymbols = new CompositeMap();
    }

    public void attach(Map<String, ? extends IModulaSymbol> collection) {
        allSymbols.addComposited(collection);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol resolveName(String symbolName) {
        IModulaSymbol symbol = findSymbolInScope(symbolName);
        if (symbol == null) {
            IModulaSymbolScope parentScope = getParentScope();
            if (parentScope != null) {
                symbol = parentScope.resolveName(symbolName);
            }
        }
        return symbol;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(String name) {
        return (IModulaSymbol) allSymbols.get(name);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(String symbolName, boolean isPublic) {
        IModulaSymbol symbol = findSymbolInScope(symbolName);
        if (symbol != null) {
            if (isPublic != symbol.isAttributeSet(SymbolAttribute.PUBLIC)) {
                symbol = null;
            }
        }
        return symbol;
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<IModulaSymbol> iterator() {
        return allSymbols.values().iterator();
    }
    
}
