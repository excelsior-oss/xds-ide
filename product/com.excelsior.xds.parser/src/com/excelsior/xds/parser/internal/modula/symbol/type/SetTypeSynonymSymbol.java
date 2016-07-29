package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.IOrdinalTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ISetTypeSymbol;
import com.excelsior.xds.parser.modula.type.SetType;

public class SetTypeSynonymSymbol extends    TypeSynonymSymbol<SetType, SetTypeSymbol> 
                                  implements ISetTypeSymbol 
{
    public SetTypeSynonymSymbol( String name, ISymbolWithScope parentScope
                               , IModulaSymbolReference<SetTypeSymbol> originalSymbolRef, 
                               IModulaSymbolReference<IModulaSymbol> referencedSymbolRef ) 
    {
        super(name, parentScope, originalSymbolRef, referencedSymbolRef);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public ISetTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new SetTypeSynonymSymbol(name, parentScope, refFactory.createRef(getOriginalSymbol()), refFactory.createRef((IModulaSymbol)this)); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IOrdinalTypeSymbol getBaseTypeSymbol() {
        return getOriginalSymbol().getBaseTypeSymbol();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPacked() {
        return getOriginalSymbol().isPacked();
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit((ISetTypeSymbol)this);
	}
}
