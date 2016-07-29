package com.excelsior.xds.core.model.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.model.CompilationUnitType;
import com.excelsior.xds.core.model.IXdsCompilationUnit;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsElementWithSymbol;
import com.excelsior.xds.core.model.IXdsModule;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.core.utils.XdsFileUtils;
import com.excelsior.xds.core.utils.time.ModificationStamp;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.modula.XdsParserManager;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;

public abstract class XdsCompilationUnit implements IXdsCompilationUnit
{
	private IXdsContainer parent;
    
    private final IXdsProject xdsProject;
    
    private ModificationStamp astModificationStamp = new ModificationStamp();
    
	private XdsModule moduleElement;
	
	private final Map<IModulaSymbolReference<IModulaSymbol>, IXdsElementWithSymbol> symbolRef2XdsElement = new HashMap<IModulaSymbolReference<IModulaSymbol>, IXdsElementWithSymbol>();
	
    public XdsCompilationUnit( IXdsProject xdsProject
                             , IXdsContainer parent) 
    {
        this.parent = parent;
        this.xdsProject = xdsProject;
    }
    
    @Override
    public IXdsProject getXdsProject() {
        return xdsProject;
    }

    @Override
    public synchronized IXdsContainer getParent() {
        return parent;
    }
    
    public synchronized void setParent(IXdsContainer parent) {
		this.parent = parent;
	}

	protected static CompilationUnitType determineCompilationUnitType(String fileName) {
    	CompilationUnitType compilationUnitType = null;
        if (XdsFileUtils.isProgramModuleFile(fileName))
            compilationUnitType =  CompilationUnitType.PROGRAM_MODULE;
        else if (XdsFileUtils.isDefinitionModuleFile(fileName))
            compilationUnitType =  CompilationUnitType.DEFINITION_MODULE;
        else if (XdsFileUtils.isSymbolFile(fileName))
             compilationUnitType =  CompilationUnitType.SYMBOL_FILE;
        else
            LogHelper.logError("Unknown compilation unit type of [" + fileName + "]");  //$NON-NLS-1$ //$NON-NLS-2$
        
        return compilationUnitType;
    }

	/**
	 * Method is synchronized because two different request to re-build compilation unit structure
	 * (coming from competing threads) must not corrupt data structures.
	 */
	private synchronized void buildUnitStructure(ModulaAst ast) {
		symbolRef2XdsElement.clear();
		XdsCompilationUnitBuilder compilationUnitBuilder = new XdsCompilationUnitBuilder(
				convertToConcreteSymbols(symbolRef2XdsElement), this, ast);
		compilationUnitBuilder.buildUnitStructure();
		astModificationStamp = ast.getModificationStamp();
	}
	
	private Map<IModulaSymbol, IXdsElementWithSymbol> convertToConcreteSymbols(Map<IModulaSymbolReference<IModulaSymbol>, IXdsElementWithSymbol> symbolRef2XdsElement) {
	    Map<IModulaSymbol, IXdsElementWithSymbol> symbol2XdsElement = new HashMap<IModulaSymbol, IXdsElementWithSymbol>();
	    for (Map.Entry<IModulaSymbolReference<IModulaSymbol>, IXdsElementWithSymbol> pair : symbolRef2XdsElement.entrySet()) {
            IModulaSymbol symbol = ReferenceUtils.resolve(pair.getKey());
            if (symbol != null) {
                symbol2XdsElement.put(symbol, pair.getValue());
            }
        }
	    
	    return symbol2XdsElement;
	}

	@Override
	public synchronized Collection<IXdsElement> getChildren() {
		return Collections.singletonList(moduleElement);
	}

	@Override
	public synchronized IXdsModule getModuleElement() {
	    ModulaAst ast = getModulaAst();
	    if (ast != null && !astModificationStamp.equals(ast.getModificationStamp())) {
	        buildUnitStructure(ast);
	    }
		return moduleElement;
	}

	synchronized void mapSymbol2XdsElement(IModulaSymbolReference<IModulaSymbol> symbolRef, IXdsElementWithSymbol element) {
		symbolRef2XdsElement.put(symbolRef, element);
	}

	@Override
	public IModuleSymbol getSymbol() {
	    IXdsModule moduleElement = getModuleElement();
	    IModuleSymbol symbol = null;
	    if (moduleElement != null) {
	        symbol = moduleElement.getSymbol();
	    }
        return symbol;
	}

	@Override
	public SourceBinding getSourceBinding() {
		return null;
	}

    public synchronized void setModuleElement(XdsModule moduleElement) {
        this.moduleElement = moduleElement;
    }

	/* (non-Javadoc)
	 * @see com.excelsior.xds.core.model.IXdsCompilationUnit#getModulaAst()
	 */
	public ModulaAst getModulaAst() {
		// Not synchronized since it only access final fields
		IFileStore fileStore = getAbsoluteFile();
		IProject project = null;
		if (xdsProject != null) {
			project = xdsProject.getProject();
		}

		return XdsParserManager.getModulaAst(new ParsedModuleKey(project, fileStore));
    }
	
}
