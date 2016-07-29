package com.excelsior.xds.core.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsSetElement;
import com.excelsior.xds.core.model.IXdsSetType;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.ISetTypeSymbol;

public class XdsSet extends SimpleXdsElementWithSymbol<ISetTypeSymbol> implements
		IXdsSetType {
	
	private final List<IXdsSetElement> setElements = new ArrayList<IXdsSetElement>();

	public XdsSet(String name, IXdsProject project,
			XdsCompilationUnit compilationUnit, IXdsContainer parent,
			IModulaSymbolReference<ISetTypeSymbol> symbolRef,
			SourceBinding sourceBinding) {
		super(name, project, compilationUnit, parent, symbolRef, sourceBinding);
	}

	@Override
	public synchronized Collection<IXdsElement> getChildren() {
		return CollectionsUtils.unmodifiableArrayList(setElements, IXdsElement.class);
	}

	public synchronized void addSetElement(IXdsSetElement e) {
		setElements.add(e);
	}
}
