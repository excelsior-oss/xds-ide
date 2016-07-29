package com.excelsior.xds.parser.internal.modula.symbol.type;

import java.util.Collection;
import java.util.Iterator;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IEnumElementSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.IEnumTypeSymbol;
import com.excelsior.xds.parser.modula.type.EnumType;

public class EnumTypeSynonymSymbol extends    TypeSynonymSymbol<EnumType, EnumTypeSymbol>
                                   implements IEnumTypeSymbol
{
    public EnumTypeSynonymSymbol( String name, ISymbolWithScope parentScope
                                , IModulaSymbolReference<EnumTypeSymbol> originalSymbolRef, 
                                IModulaSymbolReference<IModulaSymbol> referencedSymbolRef ) 
    {
        super(name, parentScope, originalSymbolRef, referencedSymbolRef);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IEnumTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new EnumTypeSynonymSymbol(name, parentScope, refFactory.createRef(getOriginalSymbol()), refFactory.createRef((IModulaSymbol)this)); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IEnumElementSymbol> getElements() {
        return getOriginalSymbol().getElements();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getElementCount() {
        return getOriginalSymbol().getElementCount();
    }

    //--------------------------------------------------------------------------
    // Symbol Scope operation
    //--------------------------------------------------------------------------

	@Override
    public IModulaSymbol resolveName(String symbolName) {
        return getOriginalSymbol().resolveName(symbolName);
    }

    @Override
    public IEnumElementSymbol findSymbolInScope(String symbolName) {
        return getOriginalSymbol().findSymbolInScope(symbolName);
    }

    @Override
    public IEnumElementSymbol findSymbolInScope(String symbolName, boolean isPublic) {
        return getOriginalSymbol().findSymbolInScope(symbolName, isPublic);
    }    

    @Override
    public Iterator<IModulaSymbol> iterator() {
        return getOriginalSymbol().iterator();
    }
    

    @Override
    protected void doAccept(ModulaSymbolVisitor visitor) {
        visitor.visit((IEnumTypeSymbol)this);
    }

}
