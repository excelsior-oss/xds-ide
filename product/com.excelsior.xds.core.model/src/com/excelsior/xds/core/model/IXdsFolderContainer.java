package com.excelsior.xds.core.model;

import java.util.Collection;

import org.eclipse.core.resources.IResource;

/**
 * Container which is actually folder on the file system 
 * 
 * @author lsa80
 */
public interface IXdsFolderContainer extends IXdsContainer {
	/**
     * Gets all resources corresponding to immediate children
     */
    Collection<IResource> getChildResources();
}
