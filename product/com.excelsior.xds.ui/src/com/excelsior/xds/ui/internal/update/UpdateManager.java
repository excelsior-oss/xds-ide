package com.excelsior.xds.ui.internal.update;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkManager;
import com.excelsior.xds.core.sdk.SdkRegistry;
import com.excelsior.xds.core.updates.descriptor.DescriptorParser;
import com.excelsior.xds.core.updates.descriptor.UpdateDirDescriptor;
import com.excelsior.xds.core.updates.dropins.XdsPluginUpdater;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.internal.nls.Messages;
import com.excelsior.xds.ui.internal.update.handlers.XdsUpdateHandler;

public class UpdateManager {
	private static final String XDS_UPDATE_CONTEXT = "com.excelsior.xds.update.context"; //$NON-NLS-1$
	private static IContextActivation activateContext;

	public static void invokeStandardUpdateUI() {
		Command command = WorkbenchUtils.getCommandService().getCommand(XdsUpdateHandler.STANDARD_SDK_UPDATE_COMMAND_ID);
		final Event trigger = new Event();
		
		ExecutionEvent executionEvent = ((IHandlerService)WorkbenchUtils.getService(IHandlerService.class)).createExecutionEvent(command, trigger);
		try {
			deactivateXdsUpdateContext(); // so standard handler will be used for "org.eclipse.equinox.p2.ui.sdk.update"
			command.setEnabled(null);
			command.executeWithChecks(executionEvent);
		} catch (ExecutionException e) {
			LogHelper.logError(e);
		} catch (NotDefinedException e) {
			LogHelper.logError(e);
		} catch (NotEnabledException e) {
			LogHelper.logError(e);
		} catch (NotHandledException e) {
			LogHelper.logError(e);
		}
		finally{
			activateXdsUpdateContext(); // invoke XDS overriding for the "org.eclipse.equinox.p2.ui.sdk.update"
		}
	}
	
	/**
	 * Override standard 'Check for Updates' handler. 
	 */
	public static void activateXdsUpdateContext() {
	   IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
 	   activateContext = contextService.activateContext( XDS_UPDATE_CONTEXT );
	}
	
	/**
	 * Cancel overriding of standard 'Check for Updates' handler. 
	 */
	public static void deactivateXdsUpdateContext() {
		if (activateContext != null) {
			IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
			contextService.deactivateContext(activateContext);
			activateContext = null;
		}
	}
	
	/**
	 * @param isReportNoUpdates - if true - 'No new updates found message box will be shown'
	 */
	public static void runUpdateHelpPluginRoutines(final boolean isReportNoUpdates) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				List<UpdateDirDescriptor> updateSources = getUpdateSources();
				boolean isNewUpdatesPending = false;
				for (UpdateDirDescriptor desc : updateSources) {
					if (XdsPluginUpdater.areNewPluginsPending(desc)) {
						isNewUpdatesPending = true;
						break;
					}
				}
				
				if (isNewUpdatesPending) {
					UpdateAvailableDialog dlg = new UpdateAvailableDialog(WorkbenchUtils.getWorkbenchWindowShell());
					if (dlg.open() == UpdateAvailableDialog.OK) {
						runUpdateHelpPluginRoutines(updateSources);
					}
				}
				else if (isReportNoUpdates) {
					SWTFactory.ShowMessageBox(null, Messages.UpdateManager_XdsUpdateManager, Messages.UpdateManager_NoNewUpdatesFound, SWT.OK);
				}
			}
		});
	}
	
	public static void refreshSourcesAndRunUpdateHelpPluginRoutines() {
		List<UpdateDirDescriptor> updateSources = getUpdateSources();
		runUpdateHelpPluginRoutines(updateSources);
	}
	
	private static void runUpdateHelpPluginRoutines(
			List<UpdateDirDescriptor> updateSources) {
		for (UpdateDirDescriptor desc : updateSources) {
			if (XdsPluginUpdater.areNewPluginsPending(desc)) {
				try {
					XdsPluginUpdater.doUpdate(desc);
				} catch (IOException e) {
					LogHelper.logError(e);
				}
			}
		}
		
		restartIDE();
	}
	
	private static void restartIDE() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				PlatformUI.getWorkbench().restart();
			}
		});
	}
	
	private static List<UpdateDirDescriptor> getUpdateSources() {
		List<UpdateDirDescriptor> updateSources = new ArrayList<UpdateDirDescriptor>();
		UpdateDirDescriptor desc = DescriptorParser.parse();
		if (desc != null) {
			updateSources.add(desc);
		}
		SdkRegistry sdkRegistry = SdkManager.getInstance().loadSdkRegistry();
		List<Sdk> registeredSDKs = sdkRegistry.getRegisteredSDKs();
		for (Sdk sdk : registeredSDKs) {
			String updateDescriptorPath = sdk.getUpdateDescriptorPath();
			if (updateDescriptorPath == null) continue;
			try {
				desc = DescriptorParser.parsePendingUpdates(new File(updateDescriptorPath));
			} catch (WorkbenchException e) {
				LogHelper.logError(e);
			} catch (IOException e) {
				LogHelper.logError(e);
			}
			if (desc != null) {
				updateSources.add(desc);
			}
		}
		
		return updateSources;
	}
}
