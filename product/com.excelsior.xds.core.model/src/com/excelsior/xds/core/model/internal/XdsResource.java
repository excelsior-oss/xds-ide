package com.excelsior.xds.core.model.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsResource;

public abstract class XdsResource implements IXdsResource, IAdaptable {
    private final IXdsProject xdsProject;
	private final IXdsContainer parent;
	private final IResource resource;
	
	public XdsResource(IXdsProject xdsProject, IXdsContainer parent, IResource resource) {
	    this.xdsProject = xdsProject;
		this.parent = parent;
		this.resource = resource;
	}
	
	@Override
    public IXdsProject getXdsProject() {
        return xdsProject;
    }

    @Override
	public String getElementName() {
		return resource.getName();
	}

	@Override
	public IXdsContainer getParent() {
		return parent;
	}

	@Override
	public IResource getResource() {
		return resource;
	}

    @Override
    public void resourceChanged() {
    }

    @Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return XdsElementCommons.adaptToResource(this, adapter);
	}
}
