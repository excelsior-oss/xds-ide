package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.builders.DefaultBuildSettingsHolder;
import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.parser.commons.symbol.BlockSymbolTextBinding;
import com.excelsior.xds.parser.commons.symbol.IMutableBlockSymbolTextBinding;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.internal.modula.symbol.reference.DefaultReferenceResolver;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceResolver;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceResolverProvider;
import com.excelsior.xds.parser.modula.XdsLanguage;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolUsages;
import com.excelsior.xds.parser.modula.symbol.IStandardModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.google.common.collect.Lists;

public class StandardModuleSymbol extends    SymbolWithDefinitions 
                                  implements IStandardModuleSymbol
                                           , IReferenceResolverProvider                
{
    private final Map<String, StandardProcedureSymbol> standardProcedures;
    private final IModulaSymbolUsages symbolUsages;
    private IReferenceResolver referenceResolver;

    public StandardModuleSymbol(String name, boolean isOberon) {
        super(name, null);
        setLanguage(isOberon ? XdsLanguage.Oberon2 : XdsLanguage.Modula2);
        standardProcedures = new HashMap<String, StandardProcedureSymbol>();
        attach(standardProcedures);
        symbolUsages = createSymbolUsages();
        referenceResolver = new DefaultReferenceResolver();
        addAttribute(SymbolAttribute.PERVASIVE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQualifiedName() {
        return getName();
    }
        
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasConstructor() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFileStore getSourceFile() {
        return null;
    }
    
    public void add(StandardProcedureSymbol s) {
        this.standardProcedures.put(s.getName(), s);
    }

    public Collection<StandardProcedureSymbol> getStandardProcedures() {
        return standardProcedures.values();
    }


    /**
     * -------------------------------------------------------------------------
     * Implementation of IDefinitionModuleSymbol interface 
     * -------------------------------------------------------------------------
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IModulaSymbol> getExportedSymbols() {
        return Lists.newArrayList(this);
    }
    
    /**
     * -------------------------------------------------------------------------
     * Implementation of ISymbolWithImports interface 
     * -------------------------------------------------------------------------
     */
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addImport(IModulaSymbolReference<IModulaSymbol> ref) {
        throw new UnsupportedOperationException("StandardModuleSymbol has no import section");  //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IModulaSymbol> getImports() {
        return Collections.emptyList();
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		boolean isVisitChildren = visitor.visit(this);
		if (isVisitChildren){
			acceptChildren(visitor, getStandardProcedures());
			acceptChildren(visitor);
		}
	}


    /**
     * -------------------------------------------------------------------------
     * Implementation of IModulaSymbolUsages interface 
     * -------------------------------------------------------------------------
     */
	
    /**
     * {@inheritDoc}
     */
	@Override
    public Collection<IModulaSymbol> getUsedSymbols() {
        return symbolUsages.getUsedSymbols();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<TextPosition> getSymbolUsages(IModulaSymbol symbol) {
        return symbolUsages.getSymbolUsages(symbol);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSymbolUsage(IModulaSymbol symbol, TextPosition usagePosition) {
        symbolUsages.addSymbolUsage(symbol, usagePosition);
    }

    protected IModulaSymbolUsages createSymbolUsages() {
        return ModulaSymbolUsages.EMPTY_USAGES;    
    }

    @Override
    public IReferenceResolver getReferenceResolver() {
        return referenceResolver;
    }
    
    
    //-------------------------------------------------------------------------
    // Symbol's location in the source text
    //-------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    protected IMutableBlockSymbolTextBinding createSymbolTextBinding() {
        return BlockSymbolTextBinding.EMPTY_TEXT_BINDING;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IMutableBlockSymbolTextBinding getTextBinding() {
        return (IMutableBlockSymbolTextBinding)super.getTextBinding();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ITextRegion> getNameTextRegions() {
        return getTextBinding().getNameTextRegions();
    }

    public void addNameTextRegion(ITextRegion region) {
        getTextBinding().addNameTextRegion(region);
    }

	@Override
	public ParsedModuleKey getKey() {
		return new ParsedModuleKey(getSourceFile());
	}

	@Override
	public BuildSettings getBuildSettings() {
		return DefaultBuildSettingsHolder.DefaultBuildSettings;
	}
}
