package com.excelsior.xds.ui.editor.commons.preferences;

import com.excelsior.xds.core.extensionpoint.ExtensionRegistry;
import com.excelsior.xds.ui.editor.commons.plugin.EditorCommonsPlugin;

public class SyntaxColoringPreferencesRegistry extends ExtensionRegistry<ISyntaxColoringPreferences> {
	public static SyntaxColoringPreferencesRegistry get() {
		return SyntaxColoringPreferencesRegistryHolder.instance;
	}
	
	private static class SyntaxColoringPreferencesRegistryHolder {
		static SyntaxColoringPreferencesRegistry instance = new SyntaxColoringPreferencesRegistry(EditorCommonsPlugin.PLUGIN_ID, "syntaxColoringPreferences", "class"); //$NON-NLS-1$ //$NON-NLS-2$
		static {
			instance.contributions();
		}
	}
	
	public SyntaxColoringPreferencesRegistry(String pluginId,
			String extensionPointName, String executableExtensionPropertyName) {
		super(pluginId, extensionPointName, executableExtensionPropertyName);
	}
}