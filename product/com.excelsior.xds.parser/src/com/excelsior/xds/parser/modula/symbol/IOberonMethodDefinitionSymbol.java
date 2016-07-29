package com.excelsior.xds.parser.modula.symbol;

public interface IOberonMethodDefinitionSymbol extends IOberonMethodSymbol
                                                     , IProcedureDefinitionSymbol
{
    /**
     * {@inheritDoc}
     */
    @Override
    public IOberonMethodDeclarationSymbol getDeclarationSymbol();
    
}
