package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.type.INumericalTypeSymbol;
import com.excelsior.xds.parser.modula.type.NumericalType;

public class NumericalTypeSymbol extends    OrdinalTypeSymbol<NumericalType>
                                 implements INumericalTypeSymbol<NumericalType> 
{
    public NumericalTypeSymbol(String name, ISymbolWithScope parentScope, NumericalType type) {
        super(name, parentScope, type);
    }
    
    @Override
    public INumericalTypeSymbol<NumericalType> createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new NumericalTypeSynonymSymbol(name, parentScope, refFactory.createRef(this), refFactory.createRef((IModulaSymbol)this)); 
    }
}
