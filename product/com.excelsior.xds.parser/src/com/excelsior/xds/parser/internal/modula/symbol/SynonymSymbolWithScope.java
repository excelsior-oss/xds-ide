package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Iterator;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public abstract class SynonymSymbolWithScope<T extends ISymbolWithScope> 
                extends    SynonymSymbol<T>  
                implements ISymbolWithScope 
{
    public SynonymSymbolWithScope( String name, ISymbolWithScope parentScope
                                 , IModulaSymbolReference<T> originalSymbolRef 
                                 , IModulaSymbolReference<IModulaSymbol> referencedSymbolRef ) 
    {
        super(name, parentScope, originalSymbolRef, referencedSymbolRef);
    }
   

    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(String symbolName) {
        return getOriginalSymbol().findSymbolInScope(symbolName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(String symbolName, boolean isPublic) {
        return getOriginalSymbol().findSymbolInScope(symbolName, isPublic);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol resolveName(String symbolName) {
        IModulaSymbol s = findSymbolInScope(symbolName);
        if (s == null) {
            IModulaSymbolScope parentScope = getParentScope();
            if (parentScope != null) {
                s = parentScope.resolveName(symbolName);
            }
        }
        return s;
    }


	@Override
	public Iterator<IModulaSymbol> iterator() {
		return getOriginalSymbol().iterator();
	}
}
