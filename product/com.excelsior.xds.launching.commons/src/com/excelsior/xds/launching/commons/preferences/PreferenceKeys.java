package com.excelsior.xds.launching.commons.preferences;

import com.excelsior.xds.core.preferences.PreferenceCommons;
import com.excelsior.xds.core.preferences.PreferenceKey;
import com.excelsior.xds.launching.commons.internal.plugin.LaunchingCommonsPlugin;

public final class PreferenceKeys
{
	private static final String QUALIFIER = LaunchingCommonsPlugin.PLUGIN_ID;
	
	/**
	 * 'Use console debugger' default, used when new debug configuration is created. 
	 */
	public final static PreferenceKey PKEY_USE_CONSOLE_DEBUGGER_DEFAULT = new PreferenceKey(
			 QUALIFIER, "use_console_debugger", false); //$NON-NLS-1$
	
	public static void flush() {
		PreferenceCommons.flushInstanceScope(QUALIFIER);
	}
}