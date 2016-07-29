package com.excelsior.xds.launching.commons.internal.nls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS 
{
	private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages";     //$NON-NLS-1$
	
	public static String LaunchDelegate_ExeName;
	public static String LaunchDelegate_ExeNameNotSet;
	public static String LaunchDelegate_ExeOrPktName;
	public static String LaunchDelegate_FileIsNotExecutable;
	public static String LaunchDelegate_InvalidExe;
	public static String LaunchDelegate_InvalidSimulatorFileName;
	public static String LaunchDelegate_ProcessFinishedWithCode;
	public static String LaunchDelegate_SimulatorExe;
	public static String LaunchDelegate_Launching;
    public static String LaunchDelegate_DebuggerLocation;
	public static String LaunchDelegate_ProfilerLocation;
	public static String ConsoleType_XDS_App;
	
	public static String AbstractLaunchDelegate_CantDetermineWorkDir;
    public static String AbstractLaunchDelegate_InvaludWorkDir;
    public static String AbstractLaunchDelegate_NotSpecified;
    public static String AbstractLaunchDelegate_ProblemDescriptionAndAskToEdit;
    public static String AbstractLaunchDelegate_ProblemOccured;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
