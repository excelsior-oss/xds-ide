package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureBodySymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;

public class ProcedureBodySymbol extends    BlockBodySymbol
                                 implements IProcedureBodySymbol 
{
    public ProcedureBodySymbol(String name, ISymbolWithScope parentScope) {
        super(name, parentScope);
    }

    //--------------------------------------------------------------------------
    // Implementation of IProcedureSymbol interface
    //--------------------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLocal() {
        IModulaSymbol parentScope = getParentScope();
        if (parentScope instanceof IProcedureSymbol) {
            return ((IProcedureSymbol)parentScope).isLocal();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPublic() {
        IModulaSymbol parentScope = getParentScope();
        if (parentScope instanceof IProcedureSymbol) {
            return ((IProcedureSymbol)parentScope).isPublic();
        }
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaSymbolVisitor visitor) {
        visitor.visit(this);
    }
    
}
