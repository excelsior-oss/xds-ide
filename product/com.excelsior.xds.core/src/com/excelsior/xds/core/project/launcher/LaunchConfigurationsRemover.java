package com.excelsior.xds.core.project.launcher;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;

import com.excelsior.xds.core.log.LogHelper;

public final class LaunchConfigurationsRemover {
	
	/**
	 * Removes all launch configurations associated with the given project
	 * (used when project is removed)
	 * 
	 * @param ip - the project
	 */
	public static void removeAll(IProject ip) {
		DebugPlugin plugin = DebugPlugin.getDefault();
		if (plugin == null) {
			return;
		}
		
		ArrayList<ILaunchConfiguration> configsToRemove = new ArrayList<ILaunchConfiguration>();
		try {
			ILaunchManager lm = plugin.getLaunchManager();
			ILaunchConfigurationType configType = lm.getLaunchConfigurationType(ILaunchConfigConst.ID_MODULA_APPLICATION);
			ILaunchConfiguration[] configs = plugin.getLaunchManager().
					getLaunchConfigurations(configType);
			for (ILaunchConfiguration config : configs) {
				if (config.getAttribute(ILaunchConfigConst.ATTR_PROJECT_NAME, "").equals(ip.getName())) { //$NON-NLS-1$
						configsToRemove.add(config);
				}
			}
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
		
		for (ILaunchConfiguration cfg : configsToRemove) {
			try {
				cfg.delete();
			} catch (CoreException e) {
				LogHelper.logError(e);
			}
		}
	}

}
