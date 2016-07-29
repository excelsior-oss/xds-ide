package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.excelsior.xds.parser.modula.symbol.ILocalModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithDeclarations;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;

public abstract class SymbolWithDeclarations extends    SymbolWithDefinitions
                                             implements ISymbolWithDeclarations 
{
    private final Map<String, ILocalModuleSymbol> localModules;

    public SymbolWithDeclarations(String name, ISymbolWithScope parentScope) {
        super(name, parentScope);
        this.localModules = new LinkedHashMap<String, ILocalModuleSymbol>();
        attach(localModules);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addLocalModule(ILocalModuleSymbol s) {
        if (s != null) {
            localModules.put(s.getName(), s);
        }
    }

    public Collection<ILocalModuleSymbol> getLocalModules() {
        return localModules.values();
    }
    

    //--------------------------------------------------------------------------
    // Implementation of IModulaSymbolScope interface
    //--------------------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(String name) {
        IModulaSymbol symbol = super.findSymbolInScope(name);
        if (symbol == null) {
            for (ILocalModuleSymbol localModule: getLocalModules()) {
                if (!localModule.isExportQualified()) {
                    symbol = localModule.findSymbolInExport(name);
                    if (symbol != null) {
                        break;
                    }
                }
            }
        }
        return symbol;
        
    }
    
    protected void acceptChildren(ModulaSymbolVisitor visitor) {
    	acceptChildren(visitor, getLocalModules());
    	super.acceptChildren(visitor);
    }
    
}
