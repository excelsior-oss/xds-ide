package com.excelsior.xds.core.search.internal.nls;

import java.text.MessageFormat;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS 
{
	
    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages";     //$NON-NLS-1$

    public static String format(String message, Object object) {
        return MessageFormat.format(message, new Object[] { object});
    }

    public static String format(String message, Object[] objects) {
        return MessageFormat.format(message, objects);
    }

    public static String ModulaSearchInput_AllOccurencies;
    public static String ModulaSearchInput_Constants;
    public static String ModulaSearchInput_Declarations;
    public static String ModulaSearchInput_Fields;
    public static String ModulaSearchInput_Modules;
    public static String ModulaSearchInput_Names;
    public static String ModulaSearchInput_Procedures;
    public static String ModulaSearchInput_Types;
    public static String ModulaSearchInput_Usages;
    public static String ModulaSearchInput_Variables;
    public static String ModulaSearchInput_Workspace;

    static {
            // initialize resource bundle
            NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Not for instantiation
}

    
}
