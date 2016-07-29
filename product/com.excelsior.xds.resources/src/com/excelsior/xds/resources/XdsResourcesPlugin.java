package com.excelsior.xds.resources;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class XdsResourcesPlugin extends AbstractUIPlugin implements
		BundleActivator {
	private static BundleContext context;
	private static Plugin plugin;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		XdsResourcesPlugin.context = bundleContext;
		plugin = this;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
		XdsResourcesPlugin.context = null;
	}

	public static Plugin getDefault() {
		return XdsResourcesPlugin.plugin;
	}
}
