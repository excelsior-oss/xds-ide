package com.excelsior.xds.core.model;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;


public interface IEditableXdsModel extends IXdsModel 
{
    void handleAddResource(IResourceDelta rootDelta, IResource affectedResource);
    void handleChangeResource(IResourceDelta rootDelta, IResource affectedResource, boolean isContentChanged);
    void handleRemoveResource(IResourceDelta rootDelta, IResource affectedResource);
    
    void notifyChanged();

    void editElement(IXdsElement element, IXdsElementOperation operation);
    
    IXdsNonWorkspaceCompilationUnit createNonWorkspaceXdsElement(IFileStore sourceFile);
    void removeNonWorkspaceXdsElement(IXdsNonWorkspaceCompilationUnit xdsElement);
	void endDeltaProcessing(IResourceDelta rootDelta);
}
