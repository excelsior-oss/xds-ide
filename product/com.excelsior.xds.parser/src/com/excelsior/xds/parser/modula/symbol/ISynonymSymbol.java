package com.excelsior.xds.parser.modula.symbol;


/**
 * A symbol that defines a synonym to other symbol. 
 */
public interface ISynonymSymbol extends IModulaSymbol
{
    public IModulaSymbol getOriginalSymbol();    

    public IModulaSymbol getReferencedSymbol();    
}
