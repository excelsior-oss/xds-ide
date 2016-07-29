package com.excelsior.xds.parser.modula.symbol;

import java.util.Collection;

import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

/**
 * A symbols that can defines types, variables, constants and procedures.
 */
public interface ISymbolWithDefinitions extends ISymbolWithScope
                                              , ISymbolWithProcedures
{
    
    /**
     * Includes the specified constant into this symbol's scope.
     * @param s constant to be included to symbol's scope.
     */
    public void addConstant(IConstantSymbol s);

    /**
     * Returns a collection containing all of the constants defined in this symbol's scope.
     * @return a collection containing all of the constants defined in this symbol's scope.
     */
    public Collection<IConstantSymbol> getConstants();
    
    
    /**
     * Includes the specified variable into this symbol's scope.
     * @param s variable to be included to symbol's scope.
     */
    public void addVariable(IVariableSymbol s);

    /**
     * Returns a collection containing all of the variables defined in this symbol's scope.
     * @return a collection containing all of the variables defined in this symbol's scope.
     */
    public Collection<IVariableSymbol> getVariables();
    

    /**
     * Includes the specified type into this symbol's scope.
     * @param s type to be included to symbol's scope.
     */
    public void addType(ITypeSymbol s);

    /**
     * Returns a collection containing all of the types defined in this symbol's scope.
     * @return a collection containing all of the types defined in this symbol's scope.
     */
    public Collection<ITypeSymbol> getTypes();

    
    /**
     * Includes the specified enumeration element into this symbol's scope.
     * @param s enumeration element to be included to symbol's scope.
     */
    public void addEnumElements(IEnumElementSymbol s);
    
    /**
     * Returns a collection containing all of the enumeration elements defined in this symbol's scope.
     * @return a collection containing all of the enumeration elements defined in this symbol's scope.
     */
    public Collection<IEnumElementSymbol> getEnumElements();

}
