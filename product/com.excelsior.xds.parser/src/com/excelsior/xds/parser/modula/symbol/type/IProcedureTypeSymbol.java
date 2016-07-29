package com.excelsior.xds.parser.modula.symbol.type;

import java.util.Collection;

import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.type.ProcedureType;

/**
 * A symbol that defines a procedure type and its structure: parameters and return type. 
 */
public interface IProcedureTypeSymbol extends ITypeSymbol, ISymbolWithScope 
{
    /**
     * {@inheritDoc}
     */
    @Override
    public ProcedureType getType();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IProcedureTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory);
    
    /**
     * Returns a ITypeSymbol of procedure return value or null if procedure has no return value.
     * 
     * @return type symbol of procedure return value or null.
     */
    public ITypeSymbol getReturnTypeSymbol();
    
    public Collection<IFormalParameterSymbol> getParameters();

}
