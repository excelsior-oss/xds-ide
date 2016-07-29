package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.parser.commons.symbol.BlockSymbolTextBinding;
import com.excelsior.xds.parser.commons.symbol.IMutableBlockSymbolTextBinding;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.internal.modula.symbol.reference.DefaultReferenceResolver;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceResolver;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceResolverProvider;
import com.excelsior.xds.parser.modula.XdsLanguage;
import com.excelsior.xds.parser.modula.symbol.IDefinitionModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IImplemantationModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolUsages;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.binding.ModulaSymbolCache;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.google.common.collect.Lists;

public class DefinitionModuleSymbol extends    SymbolWithDefinitions 
                                    implements IDefinitionModuleSymbol
                                             , IReferenceResolverProvider  
                                             , IMutableBlockSymbolTextBinding
{
    private final Map<String, IModulaSymbolReference<IModulaSymbol>> imports;
    private final IModulaSymbolUsages symbolUsages;
    private final IFileStore sourceFile;
    
    private IModulaSymbolReference<IImplemantationModuleSymbol> implemantationModuleRef;
    
    private IReferenceResolver referenceResolver;
    private final ParsedModuleKey key;
	private final BuildSettings buildSettings;
    
    public DefinitionModuleSymbol( String name, XdsLanguage language, ParsedModuleKey key
                                 , BuildSettings buildSettings, IFileStore sourceFile
                                 , boolean reconstructed ) 
    {
        super(name, ModulaSymbolCache.getSuperModule(language));
        setLanguage(language);
        this.sourceFile = sourceFile;
        this.buildSettings = buildSettings;
        this.key = key;
        imports = new HashMap<String, IModulaSymbolReference<IModulaSymbol>>();
        symbolUsages = createSymbolUsages();
        referenceResolver = new DefaultReferenceResolver();
        removeAttribute(SymbolAttribute.PERVASIVE);
        if (reconstructed) {
            addAttribute(SymbolAttribute.RECONSTRUCTED);
        }
    }
    
	@Override
	public ParsedModuleKey getKey() {
		return key;
	}
	
	@Override
	public BuildSettings getBuildSettings() {
		return buildSettings;
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
        return ! isAttributeSet(SymbolAttribute.NOMODULEINIT);
    }

    /**
     * {@inheritDoc}
     * 
     *  TODO : remove when possible
     */
    @Deprecated
    public IImplemantationModuleSymbol getImplemantationModuleSymbol() {
        return ReferenceUtils.resolve(implemantationModuleRef);
    }

    /**
     * TODO : remove when possible
     */ 
    @Deprecated
    public void setImplemantationModule(IModulaSymbolReference<IImplemantationModuleSymbol> implemantationModuleRef) {
        this.implemantationModuleRef = implemantationModuleRef;
    }    

    
    /**
     * {@inheritDoc}
     */
    @Override
    public IFileStore getSourceFile() {
        return sourceFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addImport(IModulaSymbolReference<IModulaSymbol> symbolRef) {
        IModulaSymbol s = ReferenceUtils.resolve(symbolRef);
        if (s != null) {
            imports.put(s.getName(), symbolRef);
        }
    }
    
    public Collection<IModulaSymbol> getImports() {
    	return ReferenceUtils.transformToSymbols(imports.values());
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(String name) {
        IModulaSymbol symbol = super.findSymbolInScope(name);
        if (symbol == null) {
            symbol = ReferenceUtils.resolve(imports.get(name));
        }
        return symbol;
    }
    
    
    /**
     * Searches for symbol which is exported from the symbol's scope.
     * 
     * @param symbolName the name of symbol which is to be searched
     * @return the first exported symbol with specified name, or
     *         {@code null} if there is no such symbol.
     */
    public IModulaSymbol findExportedSymbol(String name) {
        IModulaSymbol symbol = ReferenceUtils.resolve(imports.get(name));
        if (symbol == null) {
            symbol = findSymbolInScope(name);
        }
        else {
            symbol = null;
        }
        return symbol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IModulaSymbol> getExportedSymbols() {
        return Lists.newArrayList(this); 
    }

    
    /**
     * {@inheritDoc}
     */
	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		boolean isVisitChildren = visitor.visit(this);
		if (isVisitChildren){
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
        return new ModulaSymbolUsages();    
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
        return new BlockSymbolTextBinding();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IMutableBlockSymbolTextBinding getTextBinding() {
        return (BlockSymbolTextBinding)super.getTextBinding();
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
    
}
