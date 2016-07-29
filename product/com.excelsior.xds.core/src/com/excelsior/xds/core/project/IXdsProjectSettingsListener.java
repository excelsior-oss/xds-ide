package com.excelsior.xds.core.project;

import org.eclipse.core.resources.IProject;

import com.excelsior.xds.core.sdk.Sdk;

/**
 * Listener of the changes in Project Settings see {@link XdsProjectSettings}
 * 
 * @author lsa80
 */
public interface IXdsProjectSettingsListener {
	void projectSdkChanged(IProject project, Sdk oldSdk, Sdk currentSdk);
}
