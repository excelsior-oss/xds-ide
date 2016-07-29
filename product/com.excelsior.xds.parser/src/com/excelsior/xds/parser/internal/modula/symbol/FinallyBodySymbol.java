package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.parser.modula.symbol.IFinallyBodySymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;

public class FinallyBodySymbol extends    BlockBodySymbol
                               implements IFinallyBodySymbol
{
    public FinallyBodySymbol(String name, ISymbolWithScope parentScope) {
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
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPublic() {
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
