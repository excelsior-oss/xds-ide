package com.excelsior.xds.core.project;

import static com.excelsior.xds.core.utils.Lambdas.toStream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;

import com.excelsior.xds.core.sdk.Sdk;

public final class XdsProjectSettingsManager {
	private final Map<IProject, Set<IXdsProjectSettingsListener>> project2Listeners = new HashMap<IProject, Set<IXdsProjectSettingsListener>>();
    
	private XdsProjectSettings createXdsProjectSettings(IProject project) {
		return new XdsProjectSettings(project, this);
	}
	
	public static XdsProjectSettings getXdsProjectSettings(IProject project) {
		return getInstance().createXdsProjectSettings(project);
	}
	
	/**
	 * see {@link IXdsProjectSettingsListener}
	 * 
	 * @param project - project to listen to. If null - listen to every project
	 * @param l
	 */
	public static void addListener(IProject project, IXdsProjectSettingsListener l) {
		getInstance().doAddListener(project, l);
	}
	
	/**
	 * Installs global listener (all projects)
	 * @param l
	 */
	public static void addListener(IXdsProjectSettingsListener l) {
		addListener(null, l);
	}
	
	/**
	 * see {@link IXdsProjectSettingsListener}
	 * @param project
	 * @param l
	 */
	public static void removeListener(IProject project, IXdsProjectSettingsListener l) {
		getInstance().doRemoveListener(project, l);
	}
	
	private synchronized void doRemoveListener(IProject project, IXdsProjectSettingsListener l) {
		Set<IXdsProjectSettingsListener> listeners = project2Listeners.get(project);
		if (listeners != null) {
			listeners.remove(l);
		}
	}

	private synchronized void doAddListener(IProject project, IXdsProjectSettingsListener l) {
		Set<IXdsProjectSettingsListener> listeners = project2Listeners.get(project);
		if (listeners == null) {
			listeners = new HashSet<IXdsProjectSettingsListener>();
			project2Listeners.put(project, listeners);
		}
		listeners.add(l);
	}
	
	synchronized void notifyProjectSdkChanged(XdsProjectSettings settings, Sdk oldSdk, Sdk currentSdk) {
		IProject project = settings.getProject();

		Stream.concat(toStream(project2Listeners.get(project)),
				toStream(project2Listeners.get(null))).forEach(
				l -> l.projectSdkChanged(project, oldSdk, currentSdk));
	}

	private static class XdsProjectManagerHolder{
		static XdsProjectSettingsManager INSTANCE = new XdsProjectSettingsManager();
	}
	
	public static XdsProjectSettingsManager getInstance(){
		return XdsProjectManagerHolder.INSTANCE;
	}
}
