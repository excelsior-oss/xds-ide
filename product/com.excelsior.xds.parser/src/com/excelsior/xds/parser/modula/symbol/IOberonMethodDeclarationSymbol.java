package com.excelsior.xds.parser.modula.symbol;

public interface IOberonMethodDeclarationSymbol extends IOberonMethodSymbol 
                                                      , IProcedureDeclarationSymbol
{
    /**
     * {@inheritDoc}
     */
    @Override
    public IOberonMethodDefinitionSymbol getDefinitionSymbol();
    
}
