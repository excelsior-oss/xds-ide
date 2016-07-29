package com.excelsior.xds.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.resource.ResourceUtils;

public class XdsProjectConfiguration {
    private IProject project;
    
    public XdsProjectConfiguration(IProject project) {
        this.project = project;
    }

    public String getExePath() {
    	if (!ProjectUtils.isXdsProject(project)) {
    		return null;
    	}
    	XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(project);
        IFile file = ProjectUtils.getApplicationExecutableFile(xdsProjectSettings);
        return (file == null) ? null : ResourceUtils.getAbsolutePath(file);
    }
    
}
