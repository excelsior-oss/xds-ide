package com.excelsior.xds.core.model;

import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorInput;

import com.excelsior.xds.core.resource.PathWithLocationType;

public interface IXdsModel extends IXdsResource
{
    List<IXdsProject> getXdsProjects();
    IXdsProject getXdsProjectBy(IProject p);
    IXdsProject getXdsProjectBy(IResource r);
    
    /**
     * Returns IXdsElement by given <code>IEditorInput</code>. 
     * 
     * This method should be modified, whenever new editor input is supported 
     * for some editor requiring access to IXdsElement.
     * 
     * @param editorInput
     * @return IXdsElement corresponding to the give IEditorInput.
     */
    IXdsElement getXdsElement(IEditorInput editorInput);
    
    
    /**
     * Returns IXdsElement by the given location. 
     * 
     * @param location a path to the XDS element
     * 
     * @return IXdsElement corresponding to the give location.
     */
    IXdsElement getXdsElement(PathWithLocationType location);
    
    IXdsResource getXdsElement(IResource r);
    IXdsElement getNonWorkspaceXdsElement(IFileStore absoluteFile);
    IXdsElement getParentXdsElement(IResource r);
    
    void addElementChangedListener(IElementChangedListener listener);
    void removeElementChangedListener(IElementChangedListener listener);
}
