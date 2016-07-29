package com.excelsior.xds.core;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class XdsCorePlugin extends AbstractUIPlugin implements BundleActivator {

    /**
     * The plug-in identifier of the XDS Modula-2 core support
     */
	public static final String PLUGIN_ID = "com.excelsior.xds.core"; //$NON-NLS-1$

    /**
     * IContentType id for XDS File
     */
    public final static String CONTENT_TYPE_XDS_FILE = "com.excelsior.xds.contenttype.xdsFile"; //$NON-NLS-1$

    /**
     * IContentType id for XDS Source Unit
     */
    public final static String CONTENT_TYPE_XDS_SOURCE = "com.excelsior.xds.contenttype.xdsSource"; //$NON-NLS-1$
	
	private static BundleContext context;
	private static AbstractUIPlugin plugin;

	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		XdsCorePlugin.context = bundleContext;
		plugin = this;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		XdsCorePlugin.context = null;
	}

	public static AbstractUIPlugin getDefault() {
		return plugin;
	}
}
