package com.excelsior.xds.launching.debugger.ce.internal.nls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS 
{
	private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages";     //$NON-NLS-1$

	public static String LaunchDelegate_CantLaunchTwice;
    public static String LaunchDelegate_CheckingLaunchCfg;
    public static String LaunchDelegate_SdkNotConfigured;
    public static String LaunchDelegate_IncorrectProject;
    public static String LaunchDelegate_Launching;
    public static String LaunchDelegate_ProcessFinishedWithCode;
    public static String LaunchDelegate_DebuggerLocation;
	public static String LaunchDelegate_ProfilerLocation;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
