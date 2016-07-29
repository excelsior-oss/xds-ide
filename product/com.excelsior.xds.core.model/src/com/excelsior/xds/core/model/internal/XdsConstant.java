package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsConstant;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.parser.modula.symbol.IConstantSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public class XdsConstant<T extends IConstantSymbol> extends SimpleXdsElementWithSymbol<T> implements IXdsConstant {
	public XdsConstant(IXdsProject project, XdsCompilationUnit compilationUnit, String name, IXdsContainer parent, IModulaSymbolReference<T> symbolRef, SourceBinding sourceBinding) {
		super(name, project, compilationUnit, parent, symbolRef, sourceBinding);
	}
}
