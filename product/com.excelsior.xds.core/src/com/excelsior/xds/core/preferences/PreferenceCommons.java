package com.excelsior.xds.core.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

import com.excelsior.xds.core.log.LogHelper;

/**
 * Common code for working with the preferences.
 * @author lsa80
 */
public final class PreferenceCommons {
	/**
	 * @param qualifier
	 * @see org.osgi.service.prefs.Preferences#flush
	 */
	public static void flushInstanceScope(String qualifier) {
		flush(InstanceScope.INSTANCE, qualifier);
	}
	
	/**
	 * TODO : implement better architecture for storing preferences.
	 * 
	 * @param scope
	 * @param qualifier
	 * @see org.osgi.service.prefs.Preferences#flush
	 */
	public static void flush(IScopeContext scope, String qualifier) {
		try {
            IEclipsePreferences node = scope.getNode(qualifier);
            node.flush();
        } catch (BackingStoreException e) {
            LogHelper.logError(e);
        }
	}
	
	/**
	 * Static methods only.
	 */
	private PreferenceCommons(){
	}
}
