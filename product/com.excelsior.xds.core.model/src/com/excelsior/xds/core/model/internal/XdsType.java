package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsCompilationUnit;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsType;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public class XdsType extends XdsElementWithDefinitions implements IXdsType {
	private final IModulaSymbolReference<ITypeSymbol> symbolRef;
	
	public XdsType(IXdsProject xdsProject, IXdsCompilationUnit compilationUnit, IXdsContainer parent, String elementName, IModulaSymbolReference<ITypeSymbol> symbolRef, SourceBinding sourceBinding) {
		super(xdsProject, compilationUnit, parent, elementName, sourceBinding);
		
		this.symbolRef = symbolRef;
	}

	@Override
	public ITypeSymbol getSymbol() {
		return ReferenceUtils.resolve(symbolRef);
	}

    @Override
    public String getElementName() {
        ITypeSymbol symbol = getSymbol();
        return symbol != null? symbol.getName() : super.getElementName();
    }
}
