package com.excelsior.xds.parser.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

/**
 * A typed symbol. A symbol that refers to a ITypeSymbol. 
 */
public interface ISymbolWithType extends IModulaSymbol {

    public ITypeSymbol getTypeSymbol();
    
}
