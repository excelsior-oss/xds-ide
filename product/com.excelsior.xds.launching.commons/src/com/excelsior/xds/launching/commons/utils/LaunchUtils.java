package com.excelsior.xds.launching.commons.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

import com.excelsior.xds.core.project.launcher.ILaunchConfigConst;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkUtils;
import com.excelsior.xds.launching.commons.preferences.PreferenceKeys;

public final class LaunchUtils {
	/**
	 * Determines whether this launch configuration can result in the {@link ILaunchManager#DEBUG_MODE} launch with IDE integration
	 * @param p
	 * @param configuration
	 * @return
	 * @throws CoreException
	 * @see ILaunchManager#DEBUG_MODE
	 */
	public static boolean canLaunchInIdeDebugMode(IProject p, ILaunchConfiguration configuration) throws CoreException {
		Sdk sdk = SdkUtils.getProjectSdk(p);
		boolean isUseIdeDebugger = !configuration.getAttribute(ILaunchConfigConst.ATTR_USE_CONSOLE_DEBUGGER, PreferenceKeys.PKEY_USE_CONSOLE_DEBUGGER_DEFAULT.getStoredBoolean());
		return isUseIdeDebugger && sdk.isDebuggerSupportsIdeIntegration();
    }
	
	private LaunchUtils(){
	}
}
