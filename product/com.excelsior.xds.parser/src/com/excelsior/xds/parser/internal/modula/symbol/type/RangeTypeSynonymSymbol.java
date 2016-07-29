package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.IOrdinalTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IRangeTypeSymbol;
import com.excelsior.xds.parser.modula.type.NumericalType;
import com.excelsior.xds.parser.modula.type.RangeType;

public class RangeTypeSynonymSymbol extends    TypeSynonymSymbol<NumericalType, RangeTypeSymbol> 
                                    implements IRangeTypeSymbol
{
    public RangeTypeSynonymSymbol( String name, ISymbolWithScope parentScope
                                 , IModulaSymbolReference<RangeTypeSymbol> originalSymbolRef, 
                                 IModulaSymbolReference<IModulaSymbol> referencedSymbolRef ) 
    {
        super(name, parentScope, originalSymbolRef, referencedSymbolRef);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RangeType getType() {
        return getOriginalSymbol().getType();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IRangeTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new RangeTypeSynonymSymbol(name, parentScope, refFactory.createRef(getOriginalSymbol()), refFactory.createRef((IModulaSymbol)this)); 
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IOrdinalTypeSymbol getBaseTypeSymbol() {
        return getOriginalSymbol().getBaseTypeSymbol();
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit((IRangeTypeSymbol)this);
	}
}
