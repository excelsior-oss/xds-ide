package com.excelsior.xds.parser.modula.symbol;

public interface IProgramModuleSymbol extends IModuleSymbol
                                            , ISymbolWithDeclarations
{
    public IModuleBodySymbol getModuleBodySymbol();
    
    public IFinallyBodySymbol getFinallyBodySymbol();
}
