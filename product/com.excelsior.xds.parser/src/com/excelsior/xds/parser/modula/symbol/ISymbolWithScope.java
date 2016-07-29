package com.excelsior.xds.parser.modula.symbol;

/**
 * A Modula-2/Oberon-2 symbol with its own scope to hold other symbols.
 */
public interface ISymbolWithScope extends IModulaSymbol
                                        , IModulaSymbolScope 
{

}
