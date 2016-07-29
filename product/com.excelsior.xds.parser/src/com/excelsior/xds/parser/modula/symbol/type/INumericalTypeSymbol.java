package com.excelsior.xds.parser.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.type.NumericalType;


public interface INumericalTypeSymbol<T extends NumericalType> extends IOrdinalTypeSymbol 
{
    /**
     * {@inheritDoc}
     */
    @Override
    public T getType();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public INumericalTypeSymbol<T> createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory);
    
}
