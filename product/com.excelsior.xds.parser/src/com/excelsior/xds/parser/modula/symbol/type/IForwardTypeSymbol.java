package com.excelsior.xds.parser.modula.symbol.type;

import java.util.Collection;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

/**
 * A symbol that defines a forward type declaration.
 */
public interface IForwardTypeSymbol extends ITypeSymbol 
{
    public ITypeSymbol getActualTypeSymbol();
    
    public void addUsage(IModulaSymbol s);
    
    public Collection<IModulaSymbol> getUsages();
    
    public void releaseUsages();
    
}
