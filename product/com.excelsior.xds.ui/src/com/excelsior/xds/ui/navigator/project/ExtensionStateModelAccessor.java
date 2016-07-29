package com.excelsior.xds.ui.navigator.project;

import org.eclipse.ui.navigator.IExtensionStateModel;

/**
 * Utility class to simplify IExtensionStateModel access
 */
class ExtensionStateModelAccessor implements IExtensionStateConstants{
	private IExtensionStateModel extensionStateModel;

	ExtensionStateModelAccessor(IExtensionStateModel extensionStateModel) {
		this.extensionStateModel = extensionStateModel;
	}
	
	boolean isShowResources() {
		return extensionStateModel.getBooleanProperty(IS_SHOW_RESOURCES);
	}
	
	void setShowResources(boolean isShow) {
		extensionStateModel.setBooleanProperty(IS_SHOW_RESOURCES, isShow);
	}
}