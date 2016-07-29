package com.excelsior.xds.parser.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.type.ArrayType;

public interface IArrayTypeSymbol extends ITypeSymbol {

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayType getType();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IArrayTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory);

    public IOrdinalTypeSymbol getIndexTypeSymbol();
    
    public ITypeSymbol getElementTypeSymbol();

}
