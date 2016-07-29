package com.excelsior.xds.launching;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class LaunchingPlugin implements BundleActivator {
	public static final String PLUGIN_ID = "com.excelsior.xds.launching"; //$NON-NLS-1$
	private static BundleContext context;

	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		LaunchingPlugin.context = bundleContext;
	};

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		LaunchingPlugin.context = null;
	}
}
