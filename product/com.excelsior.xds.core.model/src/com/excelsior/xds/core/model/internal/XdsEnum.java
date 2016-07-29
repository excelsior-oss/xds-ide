package com.excelsior.xds.core.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsEnumElement;
import com.excelsior.xds.core.model.IXdsEnumType;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.IEnumTypeSymbol;

public class XdsEnum extends    SimpleXdsElementWithSymbol<IEnumTypeSymbol> 
                     implements IXdsEnumType 
{
	private final List<IXdsEnumElement> enumElements = new ArrayList<IXdsEnumElement>();

	public XdsEnum( IXdsProject xdsProject, XdsCompilationUnit compilationUnit
	              , IXdsContainer parent
	              , String enumName, IModulaSymbolReference<IEnumTypeSymbol> symbolRef
	              , SourceBinding sourceBinding ) 
	{
		super(enumName, xdsProject, compilationUnit, parent, symbolRef, sourceBinding);
	}

	@Override
	public synchronized Collection<IXdsElement> getChildren() {
		return CollectionsUtils.unmodifiableArrayList(enumElements, IXdsElement.class);
	}
	
	public synchronized void addEnumElement(IXdsEnumElement enumElement) {
		enumElements.add(enumElement);
	}
}