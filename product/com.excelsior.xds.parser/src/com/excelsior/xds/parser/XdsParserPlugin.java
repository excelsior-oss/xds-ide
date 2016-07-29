package com.excelsior.xds.parser;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class XdsParserPlugin extends Plugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "com.excelsior.xds.parser"; //$NON-NLS-1$
    
    // The shared instance
    private static XdsParserPlugin plugin;

    /**
     * The constructor
     */
    public XdsParserPlugin() {
    }
    
    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static XdsParserPlugin getDefault() {
        return plugin;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

}
