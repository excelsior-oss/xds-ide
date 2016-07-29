package com.excelsior.xds.core.project;

import java.io.File;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.excelsior.xds.core.XdsCorePlugin;
import com.excelsior.xds.core.exceptions.ExceptionHelper;
import com.excelsior.xds.core.internal.nls.Messages;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.preferences.PreferenceKey;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkManager;
import com.excelsior.xds.core.sdk.SdkRegistry;
import com.excelsior.xds.core.variables.VariableUtils;

/**
 * Read/write access for the XDS IProject settings.
 * <br> 
 * @author lsa80
 */
public final class XdsProjectSettings extends AbstractProjectSettings
{
	private final XdsProjectSettingsManager manager;

	/**
	 * If true means that SDK was changed but this edit is unflushed.
	 */
	private boolean isSdkChanged = false;
	private Sdk previousSdk;
	
    
    private static final String XDS_QUALIFIER         = XdsCorePlugin.PLUGIN_ID;
    private static final String TAG_PROJECT_TYPE_NAME = "ProjectType";    //$NON-NLS-1$
    private static final String TAG_PROJECT_SDK_NAME  = "ProjectSdk";     //$NON-NLS-1$
    private static final String TAG_PROJECT_APPLICATION_EXE = "ProjectApplicationExe"; //$NON-NLS-1$
    private static final String TAG_MAIN_MODULE       = "MainModule";     //$NON-NLS-1$
    private static final String TAG_XDS_PRJ_FILE      = "XdsPrjFile";     //$NON-NLS-1$
    private static final String TAG_XDS_WORKING_DIR   = "XdsCompileDir";  //$NON-NLS-1$

    XdsProjectSettings(IProject project, XdsProjectSettingsManager manager) {
    	super(project, XDS_QUALIFIER);
        this.manager = manager;
    }
    
    public XdsProjectType getProjectType() {
        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_PROJECT_TYPE_NAME);
        String projectTypeStr = preferenceKey.getStoredValue(projectScope, null);
        if (projectTypeStr == null) {
        	return null;
        }
        return XdsProjectType.valueOf(projectTypeStr);
    }
    
    public void setProjectType(XdsProjectType projectType) {
        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_PROJECT_TYPE_NAME);
        preferenceKey.setStoredValue(projectScope, projectType.toString(), null);
    }
    
    /**
     *  Returns SDK which is assigned to the project or 
     *  <tt>null</tt> when project uses Default SDK
     *  
     * @return project SDK or <tt>null</tt> if project uses Default SDK
     */
    public Sdk getProjectSdk() {
        String sdkName = getProjectSpecificSdkName();
        SdkRegistry sdkRegistry = SdkManager.getInstance().loadSdkRegistry();

        Sdk sdk;
        if (sdkName == null) {
        	sdk = sdkRegistry.getDefaultSdk();
        } else {
        	sdk = sdkRegistry.findSdk(sdkName);
        }
        return sdk;
    }

    /**
     * Assigns given SDK to the project. The <tt>null</tt> value of the parameter 
     * is used to assign the default SDK.  
     * @param sdk SDK to be assigned to the project or 
     *            <tt>null</tt> to assign default SDK.
     */
    public void setProjectSdk (Sdk sdk) {
    	if (!isSdkChanged){
    		previousSdk = getProjectSdk();
    		isSdkChanged = true;
    	}
    	
    	String sdkName = (sdk == null || sdk.isDefault()) 
    			        ? null : sdk.getName();
    	PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_PROJECT_SDK_NAME);
        preferenceKey.setStoredValue(projectScope, sdkName, null);
    }
    
    // SDK name (it may be removed SDK) or null when default SDK should be used
    public String getProjectSpecificSdkName() {
        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_PROJECT_SDK_NAME);
        return preferenceKey.getStoredValue(projectScope, null);
    }
    
    /**
     * @return XDS compiler working directory as it is specified in Project properties
     *         dialog (with ${} variables etc) or null when 'default' directory is selected
     */
    public String getXdsWorkingDirString() {
        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_XDS_WORKING_DIR);
        String dir = preferenceKey.getStoredValue(projectScope, null);
        if (dir != null && dir.trim().isEmpty()) {
            dir = null;
        }
        return dir;
    }
    
    /**
     * @return the directory should be used as working directory to run XDS compiler 
     *         in (may be unexistent) or null when error
     * @throws CoreException 
     */
    public File getXdsWorkingDir() throws CoreException {
        File compileDir = null;
        String path = getXdsWorkingDirString();
        String error = null;
        if (path != null) {
            try {
                path = VariableUtils.performStringSubstitution(getProject(), path);
                IPath iPath = new Path(path);

                if (iPath.isAbsolute()) {
                    File dir = new File(iPath.toOSString());
                    if (dir.isDirectory()) {
                        compileDir = dir;
                    } else {
                        // This may be a workspace relative path returned by a variable.
                        // However variable paths start with a slash and thus are thought to
                        // be absolute
                        IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(iPath);
                        if (res instanceof IContainer && res.exists()) {
                            compileDir = res.getLocation().toFile();
                        } else {
                            compileDir = dir; // unexistent
                        }
                    }
                } else {
                    IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(iPath);
                    if (res instanceof IContainer && res.exists()) {
                        compileDir = res.getLocation() != null? res.getLocation().toFile() : null;
                    } else {
                        compileDir = new File(path); // unexistent
                    }
                }

                if (compileDir == null) {
                	error = Messages.XdsProjectSettings_CantDetermineWorkDir;
                } else if (compileDir.isFile()) {
                	error = Messages.XdsProjectSettings_InvalidWorkDir + compileDir.getAbsolutePath() + Messages.XdsProjectSettings_InvalidWorkDir2; 
                }
            } catch (CoreException e) {
            	error = Messages.XdsProjectSettings_WrongWorkDir + ": " + e.getMessage(); //$NON-NLS-1$
                compileDir = null;
            }
        } else { // path == null
        	compileDir = getDefaultXdsWorkingDir();
        }
        if (error != null) {
        	ExceptionHelper.throwCoreException(XdsCorePlugin.PLUGIN_ID, error);
        }
        return compileDir;
    }
    
    /**
     * @return default directory to execute XDS compiler in: root directory of the project
     */
    public File getDefaultXdsWorkingDir() {
        return ResourceUtils.getAbsoluteFile(getProject());
    }


    // dir == null or dir == "" => use default path
    public void setXdsWorkingDir(String dir) {
        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_XDS_WORKING_DIR);
        preferenceKey.setStoredValue(projectScope, dir, null);
    }

    /**
     * Relative path to main module
     */
    public String getMainModule() {
        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_MAIN_MODULE);
        return preferenceKey.getStoredValue(projectScope, null);
    }

    /**
     * Relative path to main module
     */
    public void setMainModule(String mainModule) {
        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_MAIN_MODULE);
        preferenceKey.setStoredValue(projectScope, mainModule, null);
    }

    /**
     * Get executable name
     * 
     * @return full file name or null for autodetect
     */
    public String getApplicationExecutable() {
        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_PROJECT_APPLICATION_EXE);
        String s = preferenceKey.getStoredValue(projectScope, null);
        if (s != null) {
            try {
				s = StringUtils.trim(VariableUtils.performStringSubstitution(getProject(), s));
			} catch (CoreException e) {
				LogHelper.logError(e);
			}
        }
        return s;
    }

    /**
     * Set executable name
     * 
     * @param file - full file name (may be with {vars}) or null for autodetect 
     */
    public void setApplicationExecutable(String file) {
        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_PROJECT_APPLICATION_EXE);
        preferenceKey.setStoredValue(projectScope, file, null);
    }

    public String getXdsProjectFile() {
        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_XDS_PRJ_FILE);
        return preferenceKey.getStoredValue(projectScope, null);
    }

    public void setXdsProjectFile(String xdsPrjFile) {
        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_XDS_PRJ_FILE);
        preferenceKey.setStoredValue(projectScope, xdsPrjFile, null);
    }
    
    public IProject getProject() {
        return project;
    }
    
    public String getCompilationRootName() {
        String compilationRoot = null;
        if (XdsProjectType.PROJECT_FILE.equals(getProjectType())) {
            compilationRoot = getXdsProjectFile();
        }
        else if (XdsProjectType.MAIN_MODULE.equals(getProjectType())) {
            compilationRoot = getMainModule();
        }
        
        if (compilationRoot == null) {
            compilationRoot = StringUtils.EMPTY;
        }
        else  {
            compilationRoot = FilenameUtils.getName(compilationRoot);
        }
        return compilationRoot;
    }

	/**
     * Can raise {@link XdsProjectSettingsManager#notifyProjectSdkChanged(XdsProjectSettings, Sdk, Sdk)} event
     * @see AbstractProjectSettings#flush()
	 */
	@Override
	public void flush() {
		super.flush();
		if (isSdkChanged){
			if (!Objects.equals(previousSdk, getProjectSdk())) {
				manager.notifyProjectSdkChanged(this, previousSdk, getProjectSdk());
			}
			isSdkChanged = false;
		}
	}
}
