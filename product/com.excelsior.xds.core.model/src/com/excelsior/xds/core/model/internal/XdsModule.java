package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsCompilationUnit;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsModule;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;

public class XdsModule extends XdsElementWithDefinitions implements IXdsModule 
{
	private final IModulaSymbolReference<IModuleSymbol> symbolRef;
	private XdsImportSection importSection;
	
	public XdsModule( IXdsProject xdsProject, IXdsCompilationUnit compilationUnit
	                , IXdsContainer parent, String moduleName, IModulaSymbolReference<IModuleSymbol> symbolRef
	                , SourceBinding sourceBinding ) 
	{
		super(xdsProject, compilationUnit, parent, moduleName, sourceBinding);
		
		this.symbolRef = symbolRef;
	}

	@Override
	public IModuleSymbol getSymbol() {
		return ReferenceUtils.resolve(symbolRef);
	}
	
	public synchronized XdsImportSection getOrCreateImportSection(SourceBinding sourceBinding) {
		if (importSection == null) {
			importSection = new XdsImportSection(getXdsProject(), this, sourceBinding);
			importSections.add(importSection);
		}
		return importSection;
	}
	
}
