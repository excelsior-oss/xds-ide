package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.excelsior.xds.parser.modula.symbol.IProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithProcedures;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;

public abstract class SymbolWithProcedures extends    SymbolWithScope 
                                           implements ISymbolWithProcedures 
{
    protected final Map<String, IProcedureSymbol> procedures;

    public SymbolWithProcedures(String name, ISymbolWithScope parentScope) {
        super(name, parentScope);
        procedures = new HashMap<String, IProcedureSymbol>();
        attach(procedures);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProcedure(IProcedureSymbol s) {
        if (s != null) {
            procedures.put(s.getName(), s);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IProcedureSymbol> getProcedures() {
        return procedures.values();
    }
    
    protected void acceptChildren(ModulaSymbolVisitor visitor) {
        acceptChildren(visitor, getProcedures());
    }
    
}
