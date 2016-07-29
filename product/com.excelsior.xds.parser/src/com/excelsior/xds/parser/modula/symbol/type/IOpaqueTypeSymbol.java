package com.excelsior.xds.parser.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.type.OpaqueType;

/**
 * A symbol that defines a opaque type. 
 */
public interface IOpaqueTypeSymbol extends ITypeSymbol 
{
    /**
     * {@inheritDoc}
     */
    @Override
    public OpaqueType getType();

    /**
     * {@inheritDoc}
     */
    @Override
    public IOpaqueTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory);
    
    public ITypeSymbol getActualTypeSymbol();
}
