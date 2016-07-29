package com.excelsior.xds.core.extensionpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import com.excelsior.xds.core.log.LogHelper;

/**
 * Simple registry for extension point contributions.
 * @author lsa80
 * @param <T> extension class
 */
public abstract class ExtensionRegistry<T> {
	private final String pluginId;
	private final String extensionPointName;
	private final String executableExtensionPropertyName;
	private volatile List<T> contributions;
	
	/**
	 * @param pluginId plugin id of the plugin defining extension point
	 * @param extensionPointName 
	 * @param executableExtensionPropertyName name of the property specifying java class of the executable extension
	 */
	protected ExtensionRegistry(String pluginId, String extensionPointName,
			String executableExtensionPropertyName) {
		this.pluginId = pluginId;
		this.extensionPointName = extensionPointName;
		this.executableExtensionPropertyName = executableExtensionPropertyName;
	}
	
	/**
	 * Instantiates contributions and caches them to internal variable
	 * First access to this method should be serialized (synchronized). Usual practice - call this method in the static{} section of the singleton holder class to load all extensions. For examples, see classes derived from the ExtensionRegistry.
	 * @return list of the contributed extension points.
	 */
	public List<T> contributions() {
		if (contributions == null) {
			contributions = new CopyOnWriteArrayList<T>(); // use CopyOnWriteArrayList because multithreaded read access is possible
			init(contributions);
		}
		return contributions;
	}

	private void init(List<T> allContributions) {
		IExtension[] extensions = Platform.getExtensionRegistry()
				.getExtensionPoint(pluginId, extensionPointName)
				.getExtensions();
		List<T> contributions = new ArrayList<>();
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
			for (int j = 0; j < configElements.length; j++) {
				parse(configElements[j], contributions);
			}
		}
		allContributions.addAll(contributions);
	}

	private void parse(IConfigurationElement configurationElement, List<T> contributions) {
		try {
			@SuppressWarnings("unchecked")
			T contribution = (T)configurationElement.createExecutableExtension(executableExtensionPropertyName);
			contributions.add(contribution);
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
	}
}
