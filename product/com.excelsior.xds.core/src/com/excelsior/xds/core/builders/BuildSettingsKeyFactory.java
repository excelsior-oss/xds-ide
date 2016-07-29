package com.excelsior.xds.core.builders;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkManager;
import com.excelsior.xds.core.sdk.SdkUtils;

public class BuildSettingsKeyFactory {
	private static final BuildSettingsKey DEFAULT_KEY = DefaultBuildSettingsHolder.DefaultBuildSettingsKey;
	
	public static BuildSettingsKey createBuildSettingsKey(IProject p) {
		return createBuildSettingsKey(p, (File)null);
	}
	
	public static BuildSettingsKey createBuildSettingsKey(IFile sourceIFile) {
		if (sourceIFile == null) {
			return DEFAULT_KEY;
		}
		IProject project = sourceIFile.getProject();
		return createBuildSettingsKey(project, ResourceUtils.getAbsoluteFile(sourceIFile));
	}
	
	public static BuildSettingsKey createBuildSettingsKey(IProject project, File sourceFile) {
		if (project != null){
			XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(project);
			return createBuildSettingsKey(xdsProjectSettings, sourceFile);
		}
		else {
			return DEFAULT_KEY; // use default key
		}
	}
	
	public static BuildSettingsKey createBuildSettingsKey(IProject project, IFileStore sourceFile) {
		return createBuildSettingsKey(project, toFile(sourceFile));
	}
	
	private static File toFile(IFileStore moduleFileStore) {
		if (moduleFileStore == null) {
			return null;
		}
		
		try {
			return EFS.SCHEME_FILE.equals(moduleFileStore.toURI().getScheme()) ? ResourceUtils.getAbsoluteFile(moduleFileStore) : null;
		} catch (CoreException e) {
			return null;
		}
	}
	
	private static BuildSettingsKey createBuildSettingsKey(XdsProjectSettings xdsProjectSettings, File sourceFile) {
        Sdk sdk = xdsProjectSettings.getProjectSdk();
        if (SdkUtils.isInsideSdkLibraryDefinitions(sdk, ResourceUtils.toFileStore(sourceFile))) {
        	return DEFAULT_KEY;
        }
        
        IFile prjIFile = ProjectUtils.getPrjFile(xdsProjectSettings);
        
        File prjFile = prjIFile != null ? ResourceUtils.getAbsoluteFile(prjIFile) : null;
        File workDir = getWorkingDir(xdsProjectSettings, sourceFile, prjFile);
        return createBuildSettingsKey(sdk, prjFile, workDir);
	}

	private static BuildSettingsKey createBuildSettingsKey(Sdk sdk,
			File prjFile, File workDir) {
		if (sdk == null) {
            if (prjFile != null) {
            	try {
                    sdk = SdkManager.getInstance().getSdkSimulator();
                } catch (IOException e) {
                    LogHelper.logError("Build settings creation error", e);   //$NON-NLS-1$
                    return DEFAULT_KEY;
                }
            }
        }
        return new BuildSettingsKey(sdk, workDir, prjFile);
	}
	
	private static File getWorkingDir(XdsProjectSettings xdsProjectSettings, File sourceFile, File prjFile)
    {
		File workingDir = null;
		try {
			workingDir = xdsProjectSettings.getXdsWorkingDir();
		} catch (CoreException e) {
			// ignore
		}
        if (workingDir != null) {
            return workingDir;
        }
        else if (prjFile != null) {
            return prjFile.getParentFile();
        }
        else if (sourceFile != null) {
            return ResourceUtils.getAbsolutePathAsInFS(sourceFile).getParentFile();
        }
        
        return null;
    }
}
