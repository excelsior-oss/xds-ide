package com.excelsior.xds.core.ide.internal.nls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS 
{
	private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages";     //$NON-NLS-1$
    public static String IdeCore_ApplySDKToProjectJob;
    public static String SaveEditorsDialog_Title;
    public static String SaveEditorsDialog_Message;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
