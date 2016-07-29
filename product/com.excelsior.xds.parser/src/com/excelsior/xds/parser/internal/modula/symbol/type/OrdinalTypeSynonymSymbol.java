package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.IOrdinalTypeSymbol;
import com.excelsior.xds.parser.modula.type.OrdinalType;

public class OrdinalTypeSynonymSymbol<T extends OrdinalType>  extends    TypeSynonymSymbol<T, OrdinalTypeSymbol<T>>
                                                              implements IOrdinalTypeSymbol 
{
    public OrdinalTypeSynonymSymbol( String name, ISymbolWithScope parentScope
                                   , IModulaSymbolReference<OrdinalTypeSymbol<T>> originalSymbolRef, 
                                   IModulaSymbolReference<IModulaSymbol> referencedSymbolRef ) 
    {
        super(name, parentScope, originalSymbolRef, referencedSymbolRef);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IOrdinalTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new OrdinalTypeSynonymSymbol<T>(name, parentScope, refFactory.createRef(getOriginalSymbol()), refFactory.createRef((IModulaSymbol)this)); 
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit((IOrdinalTypeSymbol)this);
	}
}
