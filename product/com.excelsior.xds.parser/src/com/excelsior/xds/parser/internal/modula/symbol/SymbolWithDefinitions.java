package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.excelsior.xds.parser.modula.symbol.IConstantSymbol;
import com.excelsior.xds.parser.modula.symbol.IEnumElementSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithDefinitions;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.IVariableSymbol;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public abstract class SymbolWithDefinitions extends    SymbolWithProcedures
                                            implements ISymbolWithDefinitions 
{
    protected final Map<String, IConstantSymbol>     constants;
    protected final Map<String, IVariableSymbol>     variables;
    protected final Map<String, ITypeSymbol>         types;
    protected final Map<String, IEnumElementSymbol>  enumElements;
    
    public SymbolWithDefinitions(String name, ISymbolWithScope parentScope) {
        super(name, parentScope);

        this.variables    = new HashMap<String, IVariableSymbol>();
        this.constants    = new HashMap<String, IConstantSymbol>() ;
        this.types        = new HashMap<String, ITypeSymbol>();
        this.enumElements = new HashMap<String, IEnumElementSymbol>();

        attach(variables);
        attach(types);
        attach(constants);
        attach(enumElements);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addConstant(IConstantSymbol s) {
        if (s != null) {
            constants.put(s.getName(), s);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IConstantSymbol> getConstants() {
        return constants.values();
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEnumElements(IEnumElementSymbol s) {
        if (s != null) {
            enumElements.put(s.getName(), s);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IEnumElementSymbol> getEnumElements() {
        return enumElements.values();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addVariable(IVariableSymbol s) {
        if (s != null) {
            variables.put(s.getName(), s);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IVariableSymbol> getVariables() {
        return variables.values();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addType(ITypeSymbol s) {
        if (s != null) {
            types.put(s.getName(), s);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ITypeSymbol> getTypes() {
        return types.values();
    }


    //--------------------------------------------------------------------------
    // Implementation of IModulaSymbolScope interface
    //--------------------------------------------------------------------------
        
    /**
     * {@inheritDoc}
     */
    @Override
    protected void acceptChildren(ModulaSymbolVisitor visitor) {
    	acceptChildren(visitor, getConstants());
    	acceptChildren(visitor, getVariables());
    	acceptChildren(visitor, getTypes());
    	super.acceptChildren(visitor);
    }
    
}

