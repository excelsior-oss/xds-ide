package com.excelsior.xds.builder;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.excelsior.xds.builder.internal.resource.EncodingUpdater;

public class BuilderPlugin implements BundleActivator {

	public static final String PLUGIN_ID = "com.excelsior.xds.builder"; //$NON-NLS-1$
	
	private static BundleContext context;

	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		BuilderPlugin.context = bundleContext;
		EncodingUpdater.install();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		EncodingUpdater.uninstall();
		BuilderPlugin.context = null;
	}
}