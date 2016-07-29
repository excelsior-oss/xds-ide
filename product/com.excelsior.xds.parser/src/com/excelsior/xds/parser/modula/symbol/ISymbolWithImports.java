package com.excelsior.xds.parser.modula.symbol;

import java.util.Collection;

import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;


/**
 * A symbol that can import symbols from separate modules. 
 */
public interface ISymbolWithImports extends ISymbolWithScope 
{
    public Collection<IModulaSymbol> getImports();
    
    public void addImport(IModulaSymbolReference<IModulaSymbol> symbolRef);
    
}
