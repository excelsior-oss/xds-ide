package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsQualifiedImportElement;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public class XdsQualifiedImportElement extends SimpleXdsElementWithSymbol<IModuleSymbol> implements
		IXdsQualifiedImportElement {

	public XdsQualifiedImportElement(String name, IXdsProject project, XdsCompilationUnit compilationUnit, IXdsContainer parent, IModulaSymbolReference<IModuleSymbol> moduleSymbolRef, SourceBinding sourceBinding ) {
		super(name, project, compilationUnit, parent, moduleSymbolRef, sourceBinding);
	}

}
