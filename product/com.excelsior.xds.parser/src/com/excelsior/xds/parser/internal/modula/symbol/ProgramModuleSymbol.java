package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.iterators.FilterIterator;
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
import com.excelsior.xds.parser.internal.modula.symbol.reference.IStaticModulaSymbolReference;
import com.excelsior.xds.parser.internal.modula.symbol.reference.ReferenceFactory;
import com.excelsior.xds.parser.modula.XdsLanguage;
import com.excelsior.xds.parser.modula.symbol.IFinallyBodySymbol;
import com.excelsior.xds.parser.modula.symbol.ILocalModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolUsages;
import com.excelsior.xds.parser.modula.symbol.IModuleBodySymbol;
import com.excelsior.xds.parser.modula.symbol.IProgramModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.binding.ModulaSymbolCache;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.google.common.collect.Lists;

public class ProgramModuleSymbol extends    SymbolWithDeclarations 
                                 implements IProgramModuleSymbol 
                                          , IReferenceResolverProvider  
                                          , IMutableBlockSymbolTextBinding 
{
    private final Map<String, ILocalModuleSymbol> localModules;
    private final Map<String, IModulaSymbolReference<IModulaSymbol>> imports;

    private final IModulaSymbolUsages symbolUsages;
    private final IReferenceResolver referenceResolver;

    private IFileStore sourceFile;
    
    private IStaticModulaSymbolReference<IModuleBodySymbol>  moduleBodySymbolRef;
    private IStaticModulaSymbolReference<IFinallyBodySymbol> finallyBodySymbolRef;
	private final BuildSettings buildSettings;
	
	private final ParsedModuleKey key;
    
    protected ProgramModuleSymbol(String name, ISymbolWithScope parentScope, ParsedModuleKey key, BuildSettings buildSettings, IFileStore sourceFile) {
        super(name, parentScope);
        this.sourceFile = sourceFile;
        this.buildSettings = buildSettings;
        this.key = key;
        localModules = new HashMap<String, ILocalModuleSymbol>();
        imports      = new HashMap<String, IModulaSymbolReference<IModulaSymbol>>();
        attach(localModules);
        symbolUsages = createSymbolUsages();
        referenceResolver = new DefaultReferenceResolver();
        removeAttribute(SymbolAttribute.PERVASIVE);
    }
    
    public ProgramModuleSymbol(String name, XdsLanguage language, ParsedModuleKey key, BuildSettings buildSettings, IFileStore sourceFile) {
        this(name, ModulaSymbolCache.getSuperModule(language), key, buildSettings, sourceFile);
        setLanguage(language);
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
    public IModuleBodySymbol getModuleBodySymbol() {
        return ReferenceUtils.resolve(moduleBodySymbolRef);
    }

    public void setModuleBodySymbol(IModuleBodySymbol symbol) {
        moduleBodySymbolRef = ReferenceFactory.createStaticRef(symbol);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFinallyBodySymbol getFinallyBodySymbol() {
        return ReferenceUtils.resolve(finallyBodySymbolRef);
    }

    public void setFinallyBodySymbol(IFinallyBodySymbol symbol) {
        finallyBodySymbolRef = ReferenceFactory.createStaticRef(symbol);
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
        return sourceFile;
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
    public void addLocalModule(ILocalModuleSymbol s) {
        if (s != null) {
            localModules.put(s.getName(), s);
        }
    }

    public Collection<ILocalModuleSymbol> getLocalModules() {
        return localModules.values();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IModulaSymbol> getImports() {
    	return ReferenceUtils.transformToSymbols(imports.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addImport(IModulaSymbolReference<IModulaSymbol> ref) {
        IModulaSymbol s = ReferenceUtils.resolve(ref);
        if (s != null) {
            imports.put(s.getName(), ref);
        }
    }
    
    
	public void acceptChildren(ModulaSymbolVisitor visitor) {
		super.acceptChildren(visitor);
        acceptChildren(visitor, getLocalModules());
		acceptChild(visitor, getModuleBodySymbol());
	}

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<IModulaSymbol> getExportedSymbols() {
    	Iterable<IModulaSymbol> iterable = () -> {
    		return new FilterIterator(ProgramModuleSymbol.this.iterator(), o -> !imports.containsValue(o)
                    && ((IModulaSymbol)o).isAttributeSet(SymbolAttribute.PUBLIC));
    	};
    	
    	return Lists.newArrayList(iterable);
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
     * {@inheritDoc}
     */
	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		boolean isVisitChildren = visitor.visit(this);
		if (isVisitChildren) {
			acceptChildren(visitor);
		}
	}


    //--------------------------------------------------------------------------
    // Implementation of IModulaSymbolUsages interface 
    //--------------------------------------------------------------------------
    
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

    
    /**
     * {@inheritDoc}
     */
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
