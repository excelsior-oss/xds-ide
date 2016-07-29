package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.IInvalidModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;

public class UnknownModulaSymbol extends    ModulaSymbol
                                implements IInvalidModulaSymbol
{
    public UnknownModulaSymbol(String name, IModulaSymbolScope parentScope) {
        super(name, adaptToSymbolWithScope(parentScope));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaSymbolVisitor visitor) {
        visitor.visit(this);
    }

    static private ISymbolWithScope adaptToSymbolWithScope(IModulaSymbolScope scope) {
        while (scope != null) {
            if (scope instanceof ISymbolWithScope) {
                return (ISymbolWithScope)scope;
            }
            scope = scope.getParentScope();
        }
        return null;
    }
    
}
