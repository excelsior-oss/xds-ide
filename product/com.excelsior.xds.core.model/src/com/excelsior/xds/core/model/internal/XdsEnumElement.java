package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsEnumElement;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.parser.modula.symbol.IEnumElementSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public class XdsEnumElement extends XdsConstant<IEnumElementSymbol> implements IXdsEnumElement {
    
	public XdsEnumElement(IXdsProject project, XdsCompilationUnit compilationUnit, String name, IXdsContainer parent, IModulaSymbolReference<IEnumElementSymbol> symbolRef, SourceBinding sourceBinding) {
		super(project, compilationUnit, name, parent, symbolRef, sourceBinding);
	}
}
