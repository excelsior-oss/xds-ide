package com.excelsior.xds.core.model.internal;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

import com.excelsior.xds.core.compiler.compset.CompilationSetManager;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.model.CompilationUnitType;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsWorkspaceCompilationUnit;
import com.excelsior.xds.core.resource.ResourceUtils;

public class XdsWorkspaceCompilationUnit extends XdsCompilationUnit implements
		IXdsWorkspaceCompilationUnit, IAdaptable  {

	protected IResource resource;

	public XdsWorkspaceCompilationUnit(IXdsProject xdsProject, IResource resource,  IXdsContainer parent) {
		super(xdsProject, parent);
		this.resource = resource;
	}

	@Override
	public synchronized boolean isInCompilationSet() {
		String projectName = resource.getProject().getName();
		String path = null;
		try {
			path = ResourceUtils.getAbsolutePath(getAbsoluteFile());
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
		return CompilationSetManager.getInstance().isInCompilationSet(projectName, path);
	}

	@Override
    public synchronized String getElementName() {
        return resource.getName();
    }

	@Override
	public synchronized IResource getResource() {
		return resource;
	}
	
	public synchronized void setResource(IResource resource) {
		this.resource = resource;
	}

	@Override
	public void resourceChanged() {
	}

	@Override
	public IFileStore getAbsoluteFile() {
		return ResourceUtils.toFileStore(getResource());
	}

	@Override
	public CompilationUnitType getCompilationUnitType() {
		return determineCompilationUnitType(getResource().getName());
	}

	/**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
    	return XdsElementCommons.adaptToResource(this, adapter);
    }

	/*
	 * Debug only
	 */
	@Override
	public synchronized String toString() {
		return "XdsWorkspaceCompilationUnit:" + resource;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((resource == null) ? 0 : resource.hashCode());
		result = prime * result
				+ ((getXdsProject() == null) ? 0 : getXdsProject().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XdsWorkspaceCompilationUnit other = (XdsWorkspaceCompilationUnit) obj;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		if (getXdsProject() == null) {
			if (other.getXdsProject() != null)
				return false;
		} else if (!getXdsProject().equals(other.getXdsProject()))
			return false;
		return true;
	}
	
}
