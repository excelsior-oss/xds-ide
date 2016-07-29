package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

/**
 * AST node with reference to a Modula-2/Oberon-2 symbol.
 * 
 * @param <T> the type of referenced symbol 
 */
public interface IAstSymbolRef 
{
    /**
     * Returns the name of symbol which is bound with this node.
     * 
     * @return name of symbol which is bound with this node, 
     *         or empty string if symbol was not bound with node.
     */
    public String getName();

    /**
     * Returns the symbol which is bound with this node.
     * 
     * @return symbol bound with this node.
     */
    public IModulaSymbol getSymbol();
    
}
