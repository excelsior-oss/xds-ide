package com.excelsior.xds.ide.startup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.internal.ide.ChooseWorkspaceData;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.ide.internal.ini.EclipseIniWriter;

@SuppressWarnings("restriction")
public class StartupHook implements IStartup {

	@Override
	public void earlyStartup() {
		try {
			// on first launch - remove '-data' key/value pair from the xds-ide.ini
			EclipseIniWriter eclipseIniWriter = new EclipseIniWriter("-data", null, false); //$NON-NLS-1$
			eclipseIniWriter.write();
		} catch (Throwable e) {
			LogHelper.logError("xds-ide.ini modification failed : reseting of -data param", e); //$NON-NLS-1$
		}
		
		Job job = new Job("Setting Current Workspace To Be Used On Next Launch") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				while (Platform.getInstanceLocation() == null
						|| !Platform.getInstanceLocation().isSet()) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				}
				
				boolean isWorkspaceSet = false;
				while(!isWorkspaceSet) {
					try {
						ResourceUtils.getWorkspaceRootPath();
						isWorkspaceSet = true;
					} catch (IllegalStateException e) {
						System.out.println("Wainting for the workspace initialization...");
					}
				}
				
				makeCurrentWorkspaceToUsedOnNextLaunch();
				
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	private static void makeCurrentWorkspaceToUsedOnNextLaunch() {
		ChooseWorkspaceData data = new ChooseWorkspaceData(""); //$NON-NLS-1$
		data.readPersistedData();
		
		String[] recentWorkspaces = data.getRecentWorkspaces();
		String workspaceRootPath = ResourceUtils.getWorkspaceRootPath();
		boolean isFound = false;
		for (String workspace : recentWorkspaces) {
			if (workspace == null) continue;
			workspace = (new File(workspace)).getAbsolutePath();
			if (workspace.equals(workspaceRootPath)) {
				isFound = true;
				break;
			}
		}
		if (!isFound) {
			List<String> recentWorkspacesList = new ArrayList<String>(Arrays.asList(recentWorkspaces));
			recentWorkspacesList.add(0, workspaceRootPath);
			data.setRecentWorkspaces(recentWorkspacesList.toArray(new String[0]));
			data.writePersistedData();
		}
	}

}
