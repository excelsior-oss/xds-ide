package com.excelsior.xds.parser.commons.symbol;

/**
 * An object that represents an entity from the source code.  
 */
public interface ISymbol {

    public String getName();
    
    public ISymbolScope getParentScope();
    
}
