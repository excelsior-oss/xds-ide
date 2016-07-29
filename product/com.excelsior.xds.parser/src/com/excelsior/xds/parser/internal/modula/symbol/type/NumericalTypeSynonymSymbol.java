package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.INumericalTypeSymbol;
import com.excelsior.xds.parser.modula.type.NumericalType;

public class NumericalTypeSynonymSymbol extends    TypeSynonymSymbol<NumericalType, NumericalTypeSymbol> 
                                        implements INumericalTypeSymbol<NumericalType> 
{
    public NumericalTypeSynonymSymbol( String name, ISymbolWithScope parentScope
                                     , IModulaSymbolReference<NumericalTypeSymbol> originalSymbolRef, 
                                     IModulaSymbolReference<IModulaSymbol> referencedSymbolRef ) 
    {
        super(name, parentScope, originalSymbolRef, referencedSymbolRef);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public INumericalTypeSymbol<NumericalType> createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
    	return new NumericalTypeSynonymSymbol(name, parentScope, refFactory.createRef(getOriginalSymbol()), refFactory.createRef((IModulaSymbol)this)); 
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit((INumericalTypeSymbol<NumericalType>)this);
	}
}
