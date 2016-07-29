package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ISynonymSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;

public abstract class SynonymSymbol<T extends IModulaSymbol> extends    ModulaSymbol
                                                             implements ISynonymSymbol
{
    private final IModulaSymbolReference<T> originalSymbolRef;
    private final IModulaSymbolReference<IModulaSymbol> referencedSymbolRef;

    public SynonymSymbol( String name, ISymbolWithScope parentScope
                        , IModulaSymbolReference<T> originalSymbolRef 
                        , IModulaSymbolReference<IModulaSymbol> referencedSymbolRef ) 
    {
        super(name, parentScope);
        this.originalSymbolRef   = originalSymbolRef;
        this.referencedSymbolRef = referencedSymbolRef;
        T originalSymbol = getOriginalSymbol();
        if (originalSymbol != null) {
            setAttributes(originalSymbol.getAttributes());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getOriginalSymbol() {
        return ReferenceUtils.resolve(originalSymbolRef);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol getReferencedSymbol() {
        return ReferenceUtils.resolve(referencedSymbolRef);
    }
    
}
