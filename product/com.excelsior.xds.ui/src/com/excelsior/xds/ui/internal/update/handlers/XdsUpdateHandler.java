package com.excelsior.xds.ui.internal.update.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

import com.excelsior.xds.ui.internal.update.UpdateManager;

public class XdsUpdateHandler extends AbstractHandler implements IHandler {
	public static final String STANDARD_SDK_UPDATE_COMMAND_ID = "org.eclipse.equinox.p2.ui.sdk.update"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
// KIDE-55. Temporary disabled due to unacceptable UI.  	    
//		UpdateManager.runUpdateHelpPluginRoutines(true);
//		XdsResourcesUpdater.getInstance().updateResources();
		UpdateManager.invokeStandardUpdateUI();
		return null;
	}
}
