package com.excelsior.xds.parser.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.type.OrdinalType;

public interface IOrdinalTypeSymbol extends ITypeSymbol {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public OrdinalType getType();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IOrdinalTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory);
    
}
