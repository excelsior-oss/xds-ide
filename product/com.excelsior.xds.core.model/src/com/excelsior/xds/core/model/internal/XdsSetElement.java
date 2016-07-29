package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsSetElement;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public class XdsSetElement extends SimpleXdsElementWithSymbol<IModulaSymbol> implements
		IXdsSetElement {

	public XdsSetElement(String name, IXdsProject project,
			XdsCompilationUnit compilationUnit, IXdsContainer parent,
			IModulaSymbolReference<IModulaSymbol> symbolRef,
			SourceBinding sourceBinding) {
		super(name, project, compilationUnit, parent, symbolRef, sourceBinding);
	}
}
