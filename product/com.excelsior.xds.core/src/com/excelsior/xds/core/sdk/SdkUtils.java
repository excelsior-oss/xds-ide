package com.excelsior.xds.core.sdk;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.ini4j.Wini;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.text.TextEncoding;

public final class SdkUtils {
    private static final String XDSPLUGININI_FILE_NAME = "xdsplugin.ini"; //$NON-NLS-1$
    private static final String ECLIPSE_HOME_LOCATION_PROPERTY = "eclipse.home.location"; //$NON-NLS-1$

    public static List<IProject> getProjectsWithGivenSdk(Sdk sdk) {
    	return getProjectsWithGivenSdkName(sdk.getName());
	}

    public static List<IProject> getProjectsWithGivenSdkName(String sdkName) {
		List<IProject> xdsProjectsWithGivenSdk = new ArrayList<IProject>();
    	
		Sdk defaultSdk = SdkManager.getInstance().loadSdkRegistry().getDefaultSdk();
		String defaultSdkName = defaultSdk != null? defaultSdk.getName() : null;
		for (IProject p : ProjectUtils.getXdsProjects()) {
			XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(p);
			String projectSdkName = xdsProjectSettings.getProjectSpecificSdkName();
			boolean isAdd = false;
			if (projectSdkName != null) {
				isAdd = Objects.equals(sdkName, projectSdkName);
			}
			else {
				isAdd = sdkName == null || Objects.equals(sdkName, defaultSdkName);
			}
			if (isAdd) {
				xdsProjectsWithGivenSdk.add(p);
			}
		}
		
		return xdsProjectsWithGivenSdk;
	}
	
	/**
	 * Called at startup. 
	 * If there are no any SDKs registered, it reads preinstalled sdk(s) enumerated 
	 * in <eclipse_install_path>\xdsplugin.ini
	 * 
	 * The file example:
	 * =================
	 * [SDKLIST]
	 * 
	 * xds.home=XDS_DIR
	 * xds.home=XDS_DIR2
	 * ...
	 * =================
	 * 
	 * first 'xds.home' will be set as 'default' sdk
	 *  
	 */
	public static void autoLoadSdksOnEarlyStart() {
	    SdkRegistry sdkRegistry = SdkManager.getInstance().loadSdkRegistry();
	    if (sdkRegistry.getRegisteredSDKs().isEmpty()) {
    	    String eclipseInstallDir = System.getProperty(ECLIPSE_HOME_LOCATION_PROPERTY);
            try {
        	    File f = new File(new URL(FilenameUtils.concat(eclipseInstallDir, XDSPLUGININI_FILE_NAME)).toURI());
        	    if (f.isFile()) {
                    Ini ini = new Wini();
                    Config config = ini.getConfig();
                    
                    config.setMultiSection(true);
                    config.setMultiOption(true);
                    config.setGlobalSection(true);
                    config.setComment(true);
                    ini.setFile(f);
                    ini.setComment("#:"); //$NON-NLS-1$
                    try(InputStreamReader reader = TextEncoding.getInputStreamReader(f)){
                    	ini.load(reader);
                    }
                    
                    Section sdkListSection = ini.get("SDKLIST"); //$NON-NLS-1$
                    if (sdkListSection != null) {
                        boolean is1st = true;
                        for (String sdksubpath : sdkListSection.getAll("xds.home")) { //$NON-NLS-1$
                            File xdsdir = new File(sdksubpath);
                            if (!xdsdir.isAbsolute()) {
                                xdsdir = new File(new URL(FilenameUtils.concat(eclipseInstallDir, sdksubpath)).toURI());
                            }
                            SdkIniFileReader sdkReader = new SdkIniFileReader(xdsdir.getAbsolutePath());
                            Sdk aSdk[] = sdkReader.getSdk();
                            for (Sdk sdk : aSdk) {
                                sdkRegistry.addSdk(sdk);
                                if (is1st) {
                                    sdkRegistry.setDefaultSdk(sdk.getName());
                                    is1st = false;
                                }
                            }
                        }
                    }
        	    }
	        } catch (Exception e) {
	            LogHelper.logError(e);
	        }
            if (!sdkRegistry.getRegisteredSDKs().isEmpty()) {
                SdkManager.getInstance().saveSdkRegistry(sdkRegistry);
            }
	    }
	}
	
	/**
	 * @param p
	 * @return SDK set on the given project
	 * @throws CoreException
	 */
	public static Sdk getProjectSdk(IProject p) throws CoreException {
		XdsProjectSettings xdsProjectSettings  =  XdsProjectSettingsManager.getXdsProjectSettings(p);
		return xdsProjectSettings.getProjectSdk();
	}
	
	public static boolean isInsideSdkLibraryDefinitions(Sdk sdk, IFileStore fileStore) {
		if (sdk == null || fileStore == null) {
			return false;
		}
		String sdkHomePath = sdk.getSdkHomePath();
		if (sdkHomePath != null) {
			File sdkHomeDir = new File(sdkHomePath);
			if (sdkHomeDir.isDirectory()) {
				IFileStore sdkHomeFileStore = ResourceUtils.toFileStore(sdkHomeDir);
				if (sdkHomeFileStore != null) {
					return sdkHomeFileStore.isParentOf(fileStore);
				}
			}
		}
		return false;
	}
	
	private SdkUtils(){
	}
}
