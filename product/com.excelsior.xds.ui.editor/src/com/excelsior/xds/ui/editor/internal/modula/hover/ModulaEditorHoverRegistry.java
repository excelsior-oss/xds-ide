package com.excelsior.xds.ui.editor.internal.modula.hover;

import org.eclipse.jface.text.ITextHover;

import com.excelsior.xds.core.extensionpoint.ExtensionRegistry;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;

public class ModulaEditorHoverRegistry extends ExtensionRegistry<ITextHover> {
	public static ModulaEditorHoverRegistry get() {
		return ModulaEditorHoverRegistryHolder.instance;
	}

	private static class ModulaEditorHoverRegistryHolder {
		static ModulaEditorHoverRegistry instance = new ModulaEditorHoverRegistry(XdsEditorsPlugin.PLUGIN_ID, "modulaEditorHover", "class");  //$NON-NLS-1$ //$NON-NLS-2$
		static {
			instance.contributions();
		}
	}
	
	private ModulaEditorHoverRegistry(String pluginId,
			String extensionPointName, String executableExtensionPropertyName) {
		super(pluginId, extensionPointName, executableExtensionPropertyName);
	}
}