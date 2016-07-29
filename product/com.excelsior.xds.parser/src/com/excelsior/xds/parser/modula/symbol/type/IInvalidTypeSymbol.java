package com.excelsior.xds.parser.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IInvalidModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.type.VoidType;

/**
 * Type symbol of invalid or unknown type.
 */
public interface IInvalidTypeSymbol extends ITypeSymbol, IInvalidModulaSymbol
{
    /**
     * {@inheritDoc}
     */
    @Override
    public VoidType getType();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IInvalidTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory);
    
}
