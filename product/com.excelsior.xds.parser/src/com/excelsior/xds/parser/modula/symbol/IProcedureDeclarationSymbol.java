package com.excelsior.xds.parser.modula.symbol;

import java.util.Collection;

import com.excelsior.xds.parser.commons.symbol.IBlockSymbolTextBinding;

public interface IProcedureDeclarationSymbol extends IProcedureSymbol
                                                   , ISymbolWithDeclarations
                                                   , IBlockSymbolTextBinding
{
    public IProcedureBodySymbol getProcedureBodySymbol();
    
    /**
     * Returns all forward declarations related to this procedure declaration
     */
    public Collection<IProcedureDefinitionSymbol> getForwardDeclarations();
    
    /**
     * Returns if exists the procedure definition symbol of this procedure.
     * 
     * @return the procedure definition symbol or 
     *         <tt>null</tt> if procedure definition doesn't exist.  
     */
    public IProcedureDefinitionSymbol getDefinitionSymbol();
    
}
