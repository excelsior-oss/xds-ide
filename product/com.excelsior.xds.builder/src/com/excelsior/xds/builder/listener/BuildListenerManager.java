package com.excelsior.xds.builder.listener;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import com.excelsior.xds.builder.BuilderPlugin;
import com.excelsior.xds.core.builders.XdsBuildResult;
import com.excelsior.xds.core.log.LogHelper;

public class BuildListenerManager {
	private List<IBuilderListener> buildListeners = new ArrayList<IBuilderListener>();
	private boolean isInitialized = false;

	public void notifyBuildStarted(IProject p) {
		initializeBuildListeners();
		for (IBuilderListener listener : buildListeners) {
			listener.onBuildStarted(p);
		}
	}
	
	public void notifyBuildFinished(IProject p, XdsBuildResult buildRes) {
		initializeBuildListeners();
		for (IBuilderListener listener : buildListeners) {
			listener.onBuildFinished(p, buildRes);
		}
	}
	
	public void addListener(IBuilderListener buildListener) {
	    buildListeners.add(buildListener);
	}

	private void initializeBuildListeners() {
	    if (!isInitialized) {
	        IExtension[] extensions = Platform.getExtensionRegistry()
	                .getExtensionPoint(BuilderPlugin.PLUGIN_ID, "listener") //$NON-NLS-1$
	                .getExtensions();
	        for (int i = 0; i < extensions.length; i++) {
	            IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
	            for (int j = 0; j < configElements.length; j++) {
	                parse(configElements[j], buildListeners);
	            }
	        }
	        isInitialized = true;
	    }
	}

	private static void parse(IConfigurationElement configurationElement,
			List<IBuilderListener> buildListeners) {
		try {
			IBuilderListener listener = (IBuilderListener)configurationElement.createExecutableExtension("class"); //$NON-NLS-1$
			buildListeners.add(listener);
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
	}
	
	public static BuildListenerManager getInstance(){
		return BuildListenerManagerHolder.INSTANCE;
	}
	
	private static class BuildListenerManagerHolder{
		static BuildListenerManager INSTANCE = new BuildListenerManager();
	}
}
