package com.excelsior.xds.parser.commons.symbol;


/**
 * Base class to represents an entity from the source code.  
 */
public abstract class Symbol implements ISymbol 
{
    private final String       name;
    private final ISymbolScope parentScope;

    public Symbol(String name, ISymbolScope parentScope) {
        this.name = name;
        this.parentScope = parentScope;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISymbolScope getParentScope() {
        return parentScope;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String name = getName();
        if (name == null) {
            return super.toString();
        }
        else {
            return name;
        }
    }
}
