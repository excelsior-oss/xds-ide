package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.IOpaqueTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.type.OpaqueType;

public class OpaqueTypeSynonymSymbol extends    TypeSynonymSymbol<OpaqueType, OpaqueTypeSymbol>
                                     implements IOpaqueTypeSymbol
{
    public OpaqueTypeSynonymSymbol( String name, ISymbolWithScope parentScope
                                  , IModulaSymbolReference<OpaqueTypeSymbol> originalSymbolRef, 
                                  IModulaSymbolReference<IModulaSymbol> referencedSymbolRef ) 
    {
        super(name, parentScope, originalSymbolRef, referencedSymbolRef);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public OpaqueType getType() {
        return getOriginalSymbol().getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getActualTypeSymbol() {
        return getOriginalSymbol().getActualTypeSymbol();
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public IOpaqueTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new OpaqueTypeSynonymSymbol(name, parentScope, refFactory.createRef(getOriginalSymbol()), refFactory.createRef((IModulaSymbol)this)); 
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit((IOpaqueTypeSymbol)this);
	}
}
