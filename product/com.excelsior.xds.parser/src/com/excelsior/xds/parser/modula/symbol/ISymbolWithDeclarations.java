package com.excelsior.xds.parser.modula.symbol;



public interface ISymbolWithDeclarations extends ISymbolWithDefinitions {

    public void addLocalModule(ILocalModuleSymbol s);
    public Iterable<ILocalModuleSymbol> getLocalModules();
    
}
