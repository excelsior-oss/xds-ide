package com.excelsior.xds.core.model.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsProjectFile;

public class XdsProjectDescriptor implements IXdsProjectFile, IAdaptable {
    private final IXdsProject xdsProject;
    private final IResource resource;
    private final IXdsContainer parent;
    
    public XdsProjectDescriptor(IXdsProject xdsProject, IResource resource, IXdsContainer parent) {
        this.xdsProject = xdsProject;
        this.resource = resource;
        this.parent = parent;
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
    public IResource getResource() {
        return resource;
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
    	return XdsElementCommons.adaptToResource(this, adapter);
    }

    @Override
    public IXdsContainer getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return resource.toString();
    }

    @Override
    public void resourceChanged() {
    }
}