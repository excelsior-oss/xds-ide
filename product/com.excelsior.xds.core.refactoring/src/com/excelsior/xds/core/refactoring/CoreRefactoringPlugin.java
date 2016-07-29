package com.excelsior.xds.core.refactoring;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class CoreRefactoringPlugin implements BundleActivator {
	
	public static final String PLUGIN_ID = "com.excelsior.xds.core.refactoring";

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		CoreRefactoringPlugin.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		CoreRefactoringPlugin.context = null;
	}

}
