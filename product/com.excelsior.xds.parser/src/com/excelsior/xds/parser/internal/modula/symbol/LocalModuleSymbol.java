package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.builders.DefaultBuildSettingsHolder;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceResolver;
import com.excelsior.xds.parser.modula.symbol.ILocalModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolUsages;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;

public class LocalModuleSymbol extends    ProgramModuleSymbol 
                               implements ILocalModuleSymbol
{
    private final Map<String, IModulaSymbol> exports;

    public LocalModuleSymbol(String name, ISymbolWithScope parentScope) {
        super(name, parentScope, new ParsedModuleKey(), DefaultBuildSettingsHolder.DefaultBuildSettings, null);
        this.exports = new HashMap<String, IModulaSymbol>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQualifiedName() {
        String qualifiedName = getName() + getNameCollosionId();
        ISymbolWithScope parentScope = getParentScope();
        if (parentScope != null) {
            qualifiedName = parentScope.getQualifiedName() + "." + qualifiedName;   //$NON-NLS-1$
        }
        return qualifiedName;
    }
        
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExportQualified() {
        return isAttributeSet(SymbolAttribute.QUALIFIED_EXPORT);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInExport(String name) {
        return exports.get(name);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IModulaSymbol> getExportedSymbols() {
        return exports.values();
    }
    
    public void addSymbolInExport(IModulaSymbol s) {
        if (s != null) {
            exports.put(s.getName(), s);
        }
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasConstructor() {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IFileStore getSourceFile() {
        IModuleSymbol parentModule = ModulaSymbolUtils.getHostModule(getParentScope());
        if (parentModule != null) {
            return parentModule.getSourceFile();
        }
        return null;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IReferenceResolver getReferenceResolver() {
        IModuleSymbol parentModule = ModulaSymbolUtils.getHostModule(getParentScope());
        if (parentModule instanceof IReferenceResolver) {
            return (IReferenceResolver)parentModule;
        }
        return null;
    }
    
    
    //--------------------------------------------------------------------------
    // Implementation of IModulaSymbolScope interface
    //--------------------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(String symbolName, boolean isPublic) {
        IModulaSymbol symbol = findSymbolInScope(symbolName);
        if (symbol != null) {
            if (isPublic != symbol.isAttributeSet(SymbolAttribute.EXPORTED)) {
                symbol = null;
            }
        }
        return symbol;
    }

    //--------------------------------------------------------------------------
    // Implementation of IModulaSymbolUsages interface 
    //--------------------------------------------------------------------------

	/**
     * {@inheritDoc}
	 * Only global module collects symbols usages information. 
	 */
    @Override
    protected IModulaSymbolUsages createSymbolUsages() {
        return ModulaSymbolUsages.EMPTY_USAGES;    
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaSymbolVisitor visitor) {
        boolean isVisitChildren = visitor.visit(this);
        if (isVisitChildren) {
            super.acceptChildren(visitor);
        }
    }

}
