package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.IPointerTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.type.PointerType;

public class PointerTypeSynonymSymbol extends    TypeSynonymSymbol<PointerType, PointerTypeSymbol>
                                      implements IPointerTypeSymbol 
{
    public PointerTypeSynonymSymbol( String name, ISymbolWithScope parentScope
                                   , IModulaSymbolReference<PointerTypeSymbol> originalSymbolRef, 
                                   IModulaSymbolReference<IModulaSymbol> referencedSymbolRef) 
    {
        super(name, parentScope, originalSymbolRef, referencedSymbolRef);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IPointerTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new PointerTypeSynonymSymbol(name, parentScope, refFactory.createRef(getOriginalSymbol()), refFactory.createRef((IModulaSymbol)this)); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getBoundTypeSymbol() {
        return getOriginalSymbol().getBoundTypeSymbol();
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit((IPointerTypeSymbol)this);
	}
}
