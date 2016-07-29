package com.excelsior.xds.builder.internal.nls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS 
{
    private static final String BUNDLE_NAME = "com.excelsior.xds.builder.internal.nls.messages"; //$NON-NLS-1$

    public static String CompileDriver_CompilerExitCode;
    public static String CompileDriver_CompilerWasTerminated;
    
    public static String Modula2SourceProjectNature_BuildingSources;
    
    public static String XdsSourceBuilder_BadXcExePath;
    public static String XdsSourceBuilder_BuildComplete;
    public static String XdsSourceBuilder_BuildFailed;
    public static String XdsSourceBuilder_BuildPrj;
    public static String XdsSourceBuilder_BuildTerminated;
    public static String XdsSourceBuilder_CompilerError;

    public static String XdsSourceBuilder_CompilerWorkDir;
    public static String XdsSourceBuilder_InvalidWorkDir;
    public static String XdsSourceBuilder_ModListComplete;
    public static String XdsSourceBuilder_NoMainModule;
    public static String XdsSourceBuilder_NoPrjFile;
    public static String XdsSourceBuilder_PleaseSpecifySdk;
    public static String XdsSourceBuilder_PrjBuildStarted;
    public static String XdsSourceBuilder_PrjGettingModuleList;
    public static String XdsSourceBuilder_PrjMakeInvoked;
    public static String XdsSourceBuilder_ProjectSummary;
    public static String XdsSourceBuilder_ConsoleName;
    public static String XdsSourceBuilder_ConsoleOfProject;
    
    public static String TodoMarkerBuilder_JobName;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
