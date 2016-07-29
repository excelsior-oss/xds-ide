package com.excelsior.xds.ui.variables;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.project.XdsProjectType;
import com.excelsior.xds.ui.commons.utils.SelectionUtils;

public final class VariableResolverUtils {

    public static IFile getPrjFile() {
        IResource resource = getFirstSelectedResource();
        if (resource != null) {
            XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(resource.getProject());
            if (XdsProjectType.PROJECT_FILE.equals(xdsProjectSettings.getProjectType())) {
                return ProjectUtils.getPrjFile(xdsProjectSettings);
            }
        }
        return null;
    }

    public static IFile getMainModule() {
        IResource resource = getFirstSelectedResource();
        if (resource != null) {
            XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(resource.getProject());
            if (XdsProjectType.MAIN_MODULE.equals(xdsProjectSettings.getProjectType())) {
                return ProjectUtils.getMainModuleFile(xdsProjectSettings);
            }
        }
        return null;
    }
    
    public static IFile getApplicationExecutable() {
        IResource resource = getFirstSelectedResource();
        if (resource != null) {
            XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(resource.getProject());
            return ProjectUtils.getApplicationExecutableFile(xdsProjectSettings);
        }
        return null;
    }

    public static IFile getSelectedFile() {
        IResource resource = getFirstSelectedResource();
        if (resource != null) {
            return (IFile) resource.getAdapter(IFile.class);
        }
        return null;
    }

    public static IResource getFirstSelectedResource() {
        List<IResource> selectedResources = SelectionUtils.getSelectedResources();
        if (selectedResources.isEmpty()) {
        	return null;
        }
        else {
        	return selectedResources.get(0);
        }
    }

    public static List<IResource> getSelectedResources() {
        return SelectionUtils.getSelectedResources();
    }
    
    /**
     * Static methods only
     */
    private VariableResolverUtils(){
    }
}
