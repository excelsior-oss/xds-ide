package com.excelsior.xds.core.search;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class SearchCorePlugin extends AbstractUIPlugin {
	
	public static final String PLUGIN_ID = "com.excelsior.xds.core.search";

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	private static SearchCorePlugin plugin;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		plugin = this;
		SearchCorePlugin.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		SearchCorePlugin.context = null;
		plugin = null;
	}

	public static SearchCorePlugin getDefault() {
		return plugin;
	}
}
