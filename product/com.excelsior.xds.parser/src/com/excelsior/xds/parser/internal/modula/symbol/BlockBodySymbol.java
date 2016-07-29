package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Collection;

import com.excelsior.xds.parser.internal.modula.symbol.type.ProcedureTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.IBlockBodySymbol;
import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.type.IProcedureTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

/**
 * Base symbol for special procedures (BEGIN, FINALLY).
 * Created for the purpose of the AST access unification (from Outline view).
 * 
 * Direct symbol reference is used, since it cannot be changed from outside 
 * - so we don't use IModulaSymbolReference here
 *  
 * @author lion
 */
public abstract class BlockBodySymbol extends    SymbolWithScope
                                      implements IBlockBodySymbol
{
    private final ProcedureTypeSymbol typeSymbol;
    
    public BlockBodySymbol(String name, ISymbolWithScope parentScope) {
        super(name, parentScope);
        typeSymbol = new ProcedureTypeSymbol(name, parentScope);
    }

    //--------------------------------------------------------------------------
    // Implementation of IProcedureSymbol interface
    //--------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public IProcedureTypeSymbol getTypeSymbol() {
        return typeSymbol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IFormalParameterSymbol> getParameters() {
        return getTypeSymbol().getParameters();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITypeSymbol getReturnTypeSymbol() {
        return getTypeSymbol().getReturnTypeSymbol();
    }
    
}
