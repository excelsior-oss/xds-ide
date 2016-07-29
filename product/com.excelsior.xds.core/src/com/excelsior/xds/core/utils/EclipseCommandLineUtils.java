package com.excelsior.xds.core.utils;

import org.eclipse.core.runtime.Platform;

public abstract class EclipseCommandLineUtils {
	/**
	 * Checks whether specific application option was set. Options are set via Program arguments field of launch configuration, 
	 * or, in production system - via xds-ide.ini. In xds-ide.ini program arguments are those that are specified before -vmargs option. 
	 * Options specified after -vmargs are passed to the JVM.   
	 * 
	 * @param optionName - full option name, say "-perspective" or "--org.eclipse.equinox.p2.reconciler.dropins.directory".
	 * @return
	 */
	public static boolean isApplicationArgSet(String optionName) {
		String[] args = Platform.getApplicationArgs();
		for (int i = 0; i < args.length; i++) {
			if (optionName.equalsIgnoreCase(args[i])) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}
}
