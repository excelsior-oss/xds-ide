package com.excelsior.xds.core.model;

import org.eclipse.core.resources.IResource;

import com.excelsior.xds.core.resource.IResourceAccess;

public interface IXdsResource extends IXdsElement, IResourceAccess 
{
	IResource getResource();
    void resourceChanged();
}
