package com.excelsior.xds.parser.internal.modula.symbol.type;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.IArrayTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IOrdinalTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.type.ArrayType;

public class ArrayTypeSynonymSymbol extends    TypeSynonymSymbol<ArrayType, ArrayTypeSymbol>
                                    implements IArrayTypeSymbol
{
    public ArrayTypeSynonymSymbol( String name, ISymbolWithScope parentScope,
    		IModulaSymbolReference<ArrayTypeSymbol> originalSymbolRef, 
            IModulaSymbolReference<IModulaSymbol> referencedSymbolRef) 
    {
        super(name, parentScope, originalSymbolRef, referencedSymbolRef);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IArrayTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new ArrayTypeSynonymSymbol(name, parentScope, refFactory.createRef(getOriginalSymbol()), refFactory.createRef((IModulaSymbol)this)); 
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IOrdinalTypeSymbol getIndexTypeSymbol() {
        return getOriginalSymbol().getIndexTypeSymbol();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getElementTypeSymbol() {
        return getOriginalSymbol().getIndexTypeSymbol();
    }

	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		visitor.visit((IArrayTypeSymbol)this);
	}    
}
