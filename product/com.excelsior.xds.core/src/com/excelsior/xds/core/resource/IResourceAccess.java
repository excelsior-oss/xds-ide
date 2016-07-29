package com.excelsior.xds.core.resource;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Common protocol for obtaining {@link IResource}.
 * @author lsa80
 */
public interface IResourceAccess {
	/**
	 * @return {@link IResource} if it is accessible, null otherwise
	 */
	IResource getResource() throws CoreException;
}
