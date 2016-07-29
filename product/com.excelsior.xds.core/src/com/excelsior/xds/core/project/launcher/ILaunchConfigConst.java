package com.excelsior.xds.core.project.launcher;
/**
 * Constant definitions for Modula-2 launch configurations.
 * <p>
 * Constant definitions only
 * </p>
 */
public interface ILaunchConfigConst {

    /**
     * Identifier for the Modula-2 Application launch configuration type
     * (value <code>"com.excelsior.xds.ui.launching.ModulaApplication"</code>).
     */
    public static final String ID_MODULA_APPLICATION     = "com.excelsior.xds.ui.launching.ModulaApplication"; //$NON-NLS-1$
    public static final String ID_PKT_SCRIPT             = "com.excelsior.xds.ui.launching.PktScript";         //$NON-NLS-1$

    // XDS Modula-2 Application launcher:
    public static final String ATTR_EXECUTABLE_PATH = ID_MODULA_APPLICATION + ".ATTR_EXECUTABLE_PATH";  //$NON-NLS-1$
    
    // XDS Debug Script launcher:
    public static final String ATTR_DBG_USE_PKT  = ID_MODULA_APPLICATION + ".ATTR_DBG_USE_PKT";    //$NON-NLS-1$ // use .pkt or .ldp 
    public static final String ATTR_DBG_PKT_PATH = ID_MODULA_APPLICATION + ".ATTR_DBG_PKT_PATH";   //$NON-NLS-1$
    public static final String ATTR_DBG_LDP_PATH = ID_MODULA_APPLICATION + ".ATTR_DBG_LDP_PATH";   //$NON-NLS-1$

    // Both launchers:
    public static final String ATTR_DEBUGGER_ARGUMENTS = ID_MODULA_APPLICATION + ".ATTR_DEBUGGER_ARGUMENTS";    //$NON-NLS-1$
	
	public static final String ATTR_PROJECT_NAME = ID_MODULA_APPLICATION + ".ATTR_PROJECT_NAME";    //$NON-NLS-1$
	
	public static final String ATTR_PROGRAM_ARGUMENTS = ID_MODULA_APPLICATION + ".ATTR_PROGRAM_ARGUMENTS";  //$NON-NLS-1$
	
	public static final String ATTR_SIMULATOR_ARGUMENTS = ID_MODULA_APPLICATION + ".ATTR_SIMULATOR_ARGUMENTS";  //$NON-NLS-1$
	
	public static final String ATTR_WORKING_DIRECTORY = ID_MODULA_APPLICATION + ".ATTR_WORKING_DIRECTORY";   //$NON-NLS-1$

	public static final String ATTR_USE_CONSOLE_DEBUGGER = ID_MODULA_APPLICATION + ".ATTR_USE_CONSOLE_DEBUGGER";   //$NON-NLS-1$
}
