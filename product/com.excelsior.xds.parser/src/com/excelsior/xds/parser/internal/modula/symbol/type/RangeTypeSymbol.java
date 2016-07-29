package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.internal.modula.symbol.reference.ReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IOrdinalTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IRangeTypeSymbol;
import com.excelsior.xds.parser.modula.type.RangeType;

public class RangeTypeSymbol extends NumericalTypeSymbol implements IRangeTypeSymbol
{
    private IModulaSymbolReference<IOrdinalTypeSymbol> baseTypeSymbolRef;
    
    public RangeTypeSymbol( String name, ISymbolWithScope parentScope
                          , IOrdinalTypeSymbol baseTypeSymbol
                          , Number minValue, Number maxValue ) 
    {
        super(name, parentScope, new RangeType(name, baseTypeSymbol.getType(), minValue, maxValue));
        this.baseTypeSymbolRef = ReferenceFactory.createRef(baseTypeSymbol);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRangeTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new RangeTypeSynonymSymbol(name, parentScope, refFactory.createRef(this), refFactory.createRef((IModulaSymbol)this)); 
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IOrdinalTypeSymbol getBaseTypeSymbol() {
        return ReferenceUtils.resolve(baseTypeSymbolRef);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RangeType getType() {
        return (RangeType) super.getType();
    }
    
}
