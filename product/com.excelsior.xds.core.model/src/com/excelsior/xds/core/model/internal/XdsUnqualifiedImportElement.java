package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsUnqualifiedImportElement;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public class XdsUnqualifiedImportElement extends SimpleXdsElementWithSymbol<IModulaSymbol> implements
		IXdsUnqualifiedImportElement {
	public XdsUnqualifiedImportElement(String name, IXdsProject project, XdsCompilationUnit compilationUnit, IXdsContainer parent, IModulaSymbolReference<IModulaSymbol> symbolRef, SourceBinding sourceBinding) {
		super(name, project, compilationUnit, parent, symbolRef, sourceBinding);
	}
}
