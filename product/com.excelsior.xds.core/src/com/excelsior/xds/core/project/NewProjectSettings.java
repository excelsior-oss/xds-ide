package com.excelsior.xds.core.project;

import org.apache.commons.lang.StringUtils;

import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;

/**
 * Container of settings to create a new project.
 */
public final class NewProjectSettings {
    
	// common part of settings:
	private String projectName;
	private String projectRoot;  
	private Sdk    projectSdk;  // null means default SDK

	private String mainModule;  // .mod/.ob2 file name or null   
	
	private String projectFile; // .prj file or null 

	// create from scratch part of settings:
	private String  tprFile;            // templete file or null to don't use it
	private boolean createRedirectionFile;
	private boolean createDirs;
	
	/**
	 * 
	 * @param projectName unique name for new project
	 * @param projectRoot path to project root folder 
	 * @param projectSdk  project specific SDK or null for default SDK
	 */
	public NewProjectSettings (String projectName, String projectRoot, Sdk projectSdk) {
		this.projectName = projectName;
		this.projectRoot = !StringUtils.isBlank(projectRoot) ? ResourceUtils.getAbsolutePathAsInFS(projectRoot) : projectRoot;
		this.projectSdk  = projectSdk;
    }

	public String getProjectName() {
		return projectName;
	}

	public String getProjectRoot() {
		return projectRoot;
	}

	public Sdk getProjectSdk() {
		return projectSdk;
	}


	/**
	 * @param mainModule existent .mod or .ob2 file name
	 */
	public void setMainModule(String mainModule) {
		this.mainModule = mainModule;
	}

	public String getMainModule() {
		return mainModule;
	}

	/**
	 * @param projectFile .prj file or null 
	 */
	public void setXdsProjectFile(String projectFile) {
		this.projectFile = projectFile;
	}
	
	public String getXdsProjectFile() {
		return projectFile;
	}


	// -------------------------------------------------------------------------
	// create from scratch part of settings
	// -------------------------------------------------------------------------
	public void setTemplateFile(String tprFile) {
		this.tprFile = tprFile;
	}

	public String getTemplateFile() {
		return tprFile;
	}

	
	public void setCreateRedFile(boolean create) {
		createRedirectionFile = create;
	}

	public boolean isCreateRedFile() {
		return createRedirectionFile;
	}


	public void setCreateDirs(boolean create) {
		createDirs = create;
	}

	public boolean isCreateDirs() {
		return createDirs;
	}
	
}
