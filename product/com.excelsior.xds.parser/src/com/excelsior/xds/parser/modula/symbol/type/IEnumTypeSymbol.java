package com.excelsior.xds.parser.modula.symbol.type;

import java.util.Collection;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IEnumElementSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.type.EnumType;

public interface IEnumTypeSymbol extends IOrdinalTypeSymbol 
                                       , ISymbolWithScope
{
    /**
     * {@inheritDoc}
     */
    @Override
    public IEnumElementSymbol findSymbolInScope(String symbolName);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IEnumElementSymbol findSymbolInScope(String symbolName, boolean isPublic);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public EnumType getType();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IEnumTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory);
    
    public Collection<IEnumElementSymbol> getElements();
    
    public int getElementCount();
        
}
