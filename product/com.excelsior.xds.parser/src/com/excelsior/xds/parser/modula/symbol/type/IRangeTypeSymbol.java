package com.excelsior.xds.parser.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.type.NumericalType;
import com.excelsior.xds.parser.modula.type.RangeType;


public interface IRangeTypeSymbol extends INumericalTypeSymbol<NumericalType> {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RangeType getType();

    /**
     * {@inheritDoc}
     */
    @Override
    public IRangeTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory);
    
    public IOrdinalTypeSymbol getBaseTypeSymbol();
}
