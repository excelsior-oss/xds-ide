package com.excelsior.texteditor.xfind.internal.nls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "com.excelsior.texteditor.xfind.internal.nls.messages"; //$NON-NLS-1$

    public static String XFindPanel_Search_label;

    public static String XFindPanel_ShowHistory_tooltip;
    public static String XFindPanel_FindPrevious_tooltip;
    public static String XFindPanel_FindNext_tooltip;
    public static String XFindPanel_ShowSettings_tooltip;
    public static String XFindPanel_Close_tooltip;

    public static String XFindPanel_Settings_MatchCase_label;
    public static String XFindPanel_Settings_WholeWord_label;
    public static String XFindPanel_Settings_RegExpr_label;
    public static String XFindPanel_Settings_ClearHistory_label;

    public static String XFindPanel_Settings_Incremental;
    public static String XFindPanel_Settings_ShowPreferences_label;

    public static String XFindPanel_Status_NotFound;
    
    public static String QuickFind_Status_FirstOccurence;
    public static String QuickFind_Status_LastOccurence;

    public static String XFindPreferencePage_Bottom;

    public static String XFindPreferencePage_Top;

    public static String XFindPreferencePage_xFindPlacement;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
    
}
