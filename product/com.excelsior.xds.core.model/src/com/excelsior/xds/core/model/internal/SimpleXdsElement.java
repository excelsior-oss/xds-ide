package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsProject;

public abstract class SimpleXdsElement implements IXdsElement 
{
	private final String name;
	private final IXdsProject project;
	private final IXdsContainer parent;
	
	public SimpleXdsElement(String name, IXdsProject project, IXdsContainer parent) {
		this.name = name;
		this.project = project;
		this.parent = parent;
	}

	@Override
	public String getElementName() {
		return name;
	}

	@Override
	public IXdsProject getXdsProject() {
		return project;
	}

	@Override
	public IXdsContainer getParent() {
		return parent;
	}
	
}
