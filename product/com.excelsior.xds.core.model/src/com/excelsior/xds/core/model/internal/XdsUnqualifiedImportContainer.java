package com.excelsior.xds.core.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsImportElement;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsUnqualifiedImportContainer;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;

public class XdsUnqualifiedImportContainer extends    SourceBoundXdsElement 
                                           implements IXdsUnqualifiedImportContainer 
{
	private final List<IXdsImportElement> importedElements = new ArrayList<IXdsImportElement>();

	public XdsUnqualifiedImportContainer(String name, IXdsProject project, IXdsContainer parent, SourceBinding sourceBinding) {
		super(name, project, parent, sourceBinding);
	}

	@Override
	public synchronized Collection<IXdsElement> getChildren() {
		return CollectionsUtils.unmodifiableArrayList(importedElements, IXdsElement.class);
	}
	
	public synchronized void addImportedElement(IXdsImportElement e) {
		importedElements.add(e);
	}

}
