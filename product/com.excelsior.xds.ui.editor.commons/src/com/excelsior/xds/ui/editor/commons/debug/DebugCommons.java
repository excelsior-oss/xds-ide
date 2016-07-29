package com.excelsior.xds.ui.editor.commons.debug;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunch;

import com.excelsior.xds.core.utils.launch.LaunchConfigurationUtils;

public final class DebugCommons {
	/**
	 * Are any debugging sessions running for this {@link IProject}
	 * @param iProject
	 * @return
	 */
	public static boolean isProjectInDebug(IProject iProject) {
		ILaunch launch = LaunchConfigurationUtils.getLaunch(iProject);
		return launch != null && launch.getDebugTarget() != null && !launch.getDebugTarget().isTerminated();
	}

	private DebugCommons(){
	}
}
