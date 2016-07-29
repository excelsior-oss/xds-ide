package com.excelsior.xds.core.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsImportElement;
import com.excelsior.xds.core.model.IXdsImportSection;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.core.model.internal.nls.Messages;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;

public class XdsImportSection extends    SourceBoundXdsElement 
                              implements IXdsImportSection 
{
	private final List<IXdsImportElement> importElements = new ArrayList<IXdsImportElement>();

	public XdsImportSection( IXdsProject project, IXdsContainer parent
	                       , SourceBinding sourceBinding ) 
	{
		super(Messages.XdsImportSection_Name, project, parent, sourceBinding);
	}

	@Override
	public synchronized Collection<IXdsElement> getChildren() {
		return CollectionsUtils.unmodifiableArrayList(importElements, IXdsElement.class);
	}

	@Override
	public synchronized Collection<IXdsImportElement> getImportElements() {
		return CollectionsUtils.unmodifiableArrayList(importElements, IXdsImportElement.class);
	}
	
	public synchronized void addImportElement(IXdsImportElement e) {
		importElements.add(e);
	}

}
