package com.excelsior.xds.parser.modula.symbol;

import java.util.Collection;

import com.excelsior.xds.parser.modula.symbol.type.IProcedureTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public interface IProcedureSymbol extends ISymbolWithScope
                                        , ISymbolWithType
{
    public boolean isLocal();

    public boolean isPublic();

    /**
     * {@inheritDoc}
     */
    @Override
    public IProcedureTypeSymbol getTypeSymbol();

    public Collection<IFormalParameterSymbol> getParameters();

    public ITypeSymbol getReturnTypeSymbol();
    
}