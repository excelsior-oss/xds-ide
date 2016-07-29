package com.excelsior.xds.launching.nls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS 
{
	private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages";     //$NON-NLS-1$

	public static String LaunchDelegate_CheckingLaunchCfg;
	public static String LaunchDelegate_IncorrectProject;
	public static String LaunchDelegate_SdkNotConfigured;
	
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
