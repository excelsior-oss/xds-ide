package com.excelsior.xds.parser.modula.symbol.type;

import com.excelsior.xds.parser.modula.symbol.ISynonymSymbol;

/**
 * A symbol that defines a synonym to other type symbol. 
 */
public interface ITypeSynonymSymbol extends ITypeSymbol
                                          , ISynonymSymbol 
{
    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getOriginalSymbol();

    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getReferencedSymbol();    

}
