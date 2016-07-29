package com.excelsior.xds.parser.modula.symbol;

import java.util.Collection;

/**
 * A symbols that can defines procedures.
 */
public interface ISymbolWithProcedures extends ISymbolWithScope 
{
    /**
     * Includes the specified procedure into this symbol's scope.
     * @param s procedure to be included to symbol's scope.
     */
    public void addProcedure(IProcedureSymbol s);
    
    /**
     * Returns a collection containing all of the procedures defined in this symbol's scope.
     * @return a collection containing all of the procedures defined in this symbol's scope.
     */
    public Collection<IProcedureSymbol> getProcedures();
    
}
