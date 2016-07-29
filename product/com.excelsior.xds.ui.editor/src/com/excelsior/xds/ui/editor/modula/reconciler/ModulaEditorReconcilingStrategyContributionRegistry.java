package com.excelsior.xds.ui.editor.modula.reconciler;

import com.excelsior.xds.core.extensionpoint.ExtensionRegistry;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.modula.reconciling.IReconcilingStrategyProvider;

public class ModulaEditorReconcilingStrategyContributionRegistry extends
		ExtensionRegistry<IReconcilingStrategyProvider> {

	public static ModulaEditorReconcilingStrategyContributionRegistry get() {
		return ModulaEditorReconcilingStrategyContributionRegistryHolder.instance;
	}

	private static class ModulaEditorReconcilingStrategyContributionRegistryHolder {
		static ModulaEditorReconcilingStrategyContributionRegistry instance = new ModulaEditorReconcilingStrategyContributionRegistry(XdsEditorsPlugin.PLUGIN_ID, "modulaEditorReconcilingStrategyContribution", "class");  //$NON-NLS-1$ //$NON-NLS-2$
		static {
			instance.contributions();
		}
	}
	
	protected ModulaEditorReconcilingStrategyContributionRegistry(String pluginId, String extensionPointName,
			String executableExtensionPropertyName) {
		super(pluginId, extensionPointName, executableExtensionPropertyName);
	}
}
