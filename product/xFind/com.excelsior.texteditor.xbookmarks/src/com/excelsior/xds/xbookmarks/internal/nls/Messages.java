package com.excelsior.xds.xbookmarks.internal.nls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS 
{
    private static final String BUNDLE_NAME = "com.excelsior.xds.xbookmarks.internal.nls.messages"; //$NON-NLS-1$
    
    public static String BookmarkAction_BM;
    public static String BookmarkAction_BM_HbzWhere;
    public static String BookmarkAction_BM_NotFound;
    public static String BookmarkAction_CantDeleteBM;
    public static String BookmarkAction_CantGoBM;
    public static String BookmarkAction_CantGoBM_NoActivePage;
    public static String BookmarkAction_CantSetBM;
    public static String BookmarkAction_CantSetBM_NoProjectDocument;
    public static String BookmarkAction_DeletedBM;
    public static String BookmarkAction_GotoBM;
    public static String BookmarkAction_MovedBM;
    public static String BookmarkAction_NoBM;
    public static String BookmarkAction_OnBM;
    public static String BookmarkAction_PressCtrlNumToGo;
    public static String BookmarkAction_PressShiftNumToToggle;
    public static String BookmarkAction_SetBM;
    public static String BookmarkAction_ToggleBM;
    public static String BookmarkAction_ParameterName;
    public static String XPreferencePage_LinkTextWithHref;
    public static String XPreferencePage_ShowBMNumbers;
    public static String XPreferencePage_ShowNumbersInTextMode;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
