package com.excelsior.xds.parser.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.type.PointerType;

public interface IPointerTypeSymbol extends ITypeSymbol 
{
    /**
     * {@inheritDoc}
     */
    @Override
    public PointerType getType();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IPointerTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory);
    
    public ITypeSymbol getBoundTypeSymbol();
}
