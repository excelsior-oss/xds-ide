package com.excelsior.xds.core.model.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.resource.IResourceAccess;

public final class XdsElementCommons {
	public static IResource adaptToResource(IResourceAccess resourceAccess, Class<?> adapter) {
		IResource resource = null;
		if (IResource.class.isAssignableFrom(adapter)) {
			try {
				resource = resourceAccess.getResource();
			} catch (CoreException e) {
				LogHelper.logError(e);
			}
			
			if (resource != null && adapter == IProject.class) {
				resource = resource.getProject();
			}
		}
		if (resource != null && adapter.isAssignableFrom(resource.getClass())) {
			return resource;
		}
		else {
			return null;
		}
	}

	/**
	 * Static methods only
	 */
	private XdsElementCommons(){
	}
}
