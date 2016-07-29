package com.excelsior.xds.launching.debugger.ce.internal.plugin;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class LaunchDebuggerCePlugin implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		LaunchDebuggerCePlugin.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		LaunchDebuggerCePlugin.context = null;
	}

}
