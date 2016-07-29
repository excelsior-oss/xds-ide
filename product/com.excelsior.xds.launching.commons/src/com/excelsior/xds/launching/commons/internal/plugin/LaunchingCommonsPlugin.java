package com.excelsior.xds.launching.commons.internal.plugin;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.excelsior.xds.launching.commons.preferences.PreferenceKeys;

public class LaunchingCommonsPlugin implements BundleActivator {
	public static final String PLUGIN_ID = "com.excelsior.xds.launching.commons"; //$NON-NLS-1$

	private static BundleContext context;

	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		LaunchingCommonsPlugin.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		LaunchingCommonsPlugin.context = null;
		PreferenceKeys.flush();
	}

}
