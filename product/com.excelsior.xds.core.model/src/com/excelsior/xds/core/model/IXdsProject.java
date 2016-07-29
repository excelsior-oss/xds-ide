package com.excelsior.xds.core.model;

import org.eclipse.core.resources.IProject;

import com.excelsior.xds.core.project.XdsProjectSettings;

public interface IXdsProject extends IXdsContainer, IXdsResource 
{
    IProject getProject();
    IXdsModel getModel();
    XdsProjectSettings getXdsProjectSettings();
    XdsProjectConfiguration getProjectConfiguration();
    
    void refreshExternalDependencies();
    void refreshSdkLibrary();
    
    IXdsExternalDependenciesContainer getXdsExternalDependenciesContainer();
    IXdsSdkLibraryContainer getXdsSdkLibraryContainer();
}
