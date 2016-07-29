package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsAliasQualifiedImportElement;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.parser.modula.symbol.IModuleAliasSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;

/**
 * TODO: XdsAliasQualifiedImportElement should extend IModuleAliasSymbol
 */
public class XdsAliasQualifiedImportElement extends    SimpleXdsElementWithSymbol<IModuleSymbol> 
                                            implements IXdsAliasQualifiedImportElement 
{
	private final IModulaSymbolReference<IModuleAliasSymbol> moduleAliasSymbolRef;

	public XdsAliasQualifiedImportElement( String name, IXdsProject project
	                                     , XdsCompilationUnit compilationUnit
	                                     , IXdsContainer parent
	                                     , IModulaSymbolReference<IModuleSymbol> moduleSymbolRef
	                                     , IModulaSymbolReference<IModuleAliasSymbol> moduleAliasSymbolRef
	                                     , SourceBinding sourceBinding ) 
	{
		super(name, project, compilationUnit, parent, moduleSymbolRef, sourceBinding);
		this.moduleAliasSymbolRef = moduleAliasSymbolRef;
	}

	@Override
	public IModuleAliasSymbol getAliasSymbol() {
		return ReferenceUtils.resolve(moduleAliasSymbolRef);
	}

}
