package com.excelsior.xds.parser.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;


public interface IModuleAliasSymbol extends ISymbolWithScope {

    public IModuleSymbol getReference();

    public void setReference(IModulaSymbolReference<IModuleSymbol> referenceSymbolRef);
}