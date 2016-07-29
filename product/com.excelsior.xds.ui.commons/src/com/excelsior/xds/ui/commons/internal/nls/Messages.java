package com.excelsior.xds.ui.commons.internal.nls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS 
{
    private static final String BUNDLE_NAME = "com.excelsior.xds.ui.commons.internal.nls.messages"; //$NON-NLS-1$
    
    public static String LocationSelector_ChooseADir;
    public static String LocationSelector_ChooseLocationRelToWspace;
    public static String LocationSelector_DirNotFound;
    public static String LocationSelector_DirSelection;
    public static String LocationSelector_ErrorOccured;
    public static String LocationSelector_FileNotFound;
    public static String LocationSelector_FileSystem;
    public static String LocationSelector_Location;
    public static String LocationSelector_OpenDir;
    public static String LocationSelector_OpenLocation;
    public static String LocationSelector_SelectAFile;
    public static String LocationSelector_SelectedFile;
    public static String LocationSelector_SelectFile;
    public static String LocationSelector_UseDefLocation;
    public static String LocationSelector_Variables;
    public static String LocationSelector_Workspace;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
