package com.excelsior.xds.parser.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.type.SetType;

public interface ISetTypeSymbol extends IOrdinalTypeSymbol {

    /**
     * {@inheritDoc}
     */
    @Override
    public SetType getType();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ISetTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory);
    
    public IOrdinalTypeSymbol getBaseTypeSymbol();
    
    public boolean isPacked();
    
}
