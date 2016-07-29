package com.excelsior.xds.core.help;

/**
 * Help context ids for the XDS Modula-2 IDE.
 * <p>
 * This interface contains constants only; it is not intended to be implemented.
 * </p>
 */
public interface IXdsHelpContextIds {

    public static final String XDS_HELP_PLUGIN = "com.excelsior.xds.help"; //$NON-NLS-1$

    //-- Dialogs ---------------------------------------------------------------
    public static final String SAVE_AND_BUILD_DIALOG = XDS_HELP_PLUGIN
            + ".save_and_build_dialog"; //$NON-NLS-1$
    
    // New module:
    public static final String NEW_MODULE_DLG = XDS_HELP_PLUGIN
            + ".new_module_dlg"; //$NON-NLS-1$
    
    
    // Edit SDK:
    public static final String EDIT_SDK_DLG = XDS_HELP_PLUGIN
            + ".edit_sdk_dlg"; //$NON-NLS-1$
    
    // Edit SDK Tool:
    public static final String EDIT_SDK_TOOL_DLG = XDS_HELP_PLUGIN
            + ".edit_sdk_tool_dlg"; //$NON-NLS-1$
    
    // New project from scratch
    public static final String NEW_PROJECT_FROM_SCRATCH_DLG = XDS_HELP_PLUGIN
    + ".new_project_from_scratch_dlg"; //$NON-NLS-1$
    
    
    // New project from sources
    public static final String NEW_PROJECT_FROM_SOURCES_DLG = XDS_HELP_PLUGIN
    + ".new_project_from_sources_dlg"; //$NON-NLS-1$
    
    
    //-- Launch configuration editor:
    // Help IDs for launch configuration editor are specified in plugin.xml:
    //
    // helpContextId="com.excelsior.xds.help.launch_configuration_editor"
    // helpContextId="com.excelsior.xds.help.launch_configuration_editor_pkt"
    
    
    //-- Preference Pages ------------------------------------------------------
    public static final String MODULA2_PREFERENCE_PAGE = XDS_HELP_PLUGIN
            + ".preferences_modula2"; //$NON-NLS-1$
    
    public static final String MODULA2_CONSOLE_PREFERENCE_PAGE = XDS_HELP_PLUGIN
            + ".preferences_modula2_console"; //$NON-NLS-1$

    public static final String MODULA2_EDITOR_PREFERENCE_PAGE = XDS_HELP_PLUGIN
            + ".preferences_modula2_editor"; //$NON-NLS-1$
    
    public static final String MODULA2_SYNTAX_COLORING_PREFERENCE_PAGE = XDS_HELP_PLUGIN
            + ".preferences_modula2_syntax_coloring"; //$NON-NLS-1$

    public static final String MODULA2_TEMPLATES_PREFERENCE_PAGE = XDS_HELP_PLUGIN
            + ".preferences_modula2_templates"; //$NON-NLS-1$
    
    
    // Edit SDKs preferences:
    public static final String MODULA2_SDKS_PREFERENCE_PAGE = XDS_HELP_PLUGIN
            + ".preferences_modula2_sdks"; //$NON-NLS-1$
    
    // Formatter
    public static final String MODULA2_FORMATTER_PREFERENCE_PAGE = XDS_HELP_PLUGIN
            + ".preferences_modula2_codestyle_formatter";
    
    // xFind panel
    // NOTE: xFind is separate package and it hardcodes this constant 
    // public static final String XFIND_PREFERENCE_PAGE = XDS_HELP_PLUGIN
    //         + ".preferences_xfind_panel";
    
    //-- Properties Pages ------------------------------------------------------
    public static final String MODULA2_PROPERTY_PAGE = XDS_HELP_PLUGIN
            + ".properties_modula2"; //$NON-NLS-1$
    

    //-- Views -----------------------------------------------------------------
    public static final String PROJECT_EXPLORER_VIEW = XDS_HELP_PLUGIN
            + ".ui_view_ProjectExplorer"; //$NON-NLS-1$

    
    
    
    public static final String MODULA2_GENERAL_SPELLING_PREFERENCE_BLOCK = MODULA2_PROPERTY_PAGE; //XXX
}
