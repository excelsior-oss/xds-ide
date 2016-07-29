package com.excelsior.xds.ui.editor.commons.internal.nls;

import java.text.MessageFormat;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS 
{
    private static final String BUNDLE_NAME = "com.excelsior.xds.ui.editor.commons.internal.nls.messages"; //$NON-NLS-1$

    public static String format(String message, Object object) {
        return MessageFormat.format(message, new Object[] { object});
    }

    public static String format(String message, Object[] objects) {
        return MessageFormat.format(message, objects);
    }
    
    public static String UpdateIndentGuides;
    
    public static String IndentGuidePreferencePage_pageDesc;
    public static String IndentGuidePreferencePage_EnableIndentGuide;
    public static String IndentGuidePreferencePage_LineAttributes;
    public static String IndentGuidePreferencePage_Alpha_0_255;
	public static String IndentGuidePreferencePage_Color;
	public static String IndentGuidePreferencePage_DASH;
	public static String IndentGuidePreferencePage_DASHDOT;
	public static String IndentGuidePreferencePage_DASHDOTDOT;
	public static String IndentGuidePreferencePage_DOT;
	public static String IndentGuidePreferencePage_Shift_1_8;
	public static String IndentGuidePreferencePage_SOLID;
	public static String IndentGuidePreferencePage_Style;
	public static String IndentGuidePreferencePage_Width_1_8;
	
	public static String SyntaxColoringPreferencePage_Bold;
    public static String SyntaxColoringPreferencePage_Color;
    public static String SyntaxColoringPreferencePage_Element;

    public static String SyntaxColoringPreferencePage_Enable;
    public static String SyntaxColoringPreferencePage_Italic;
    public static String SyntaxColoringPreferencePage_LinkHrefsDescription;
    public static String SyntaxColoringPreferencePage_Preview;
    public static String SyntaxColoringPreferencePage_Strikethrough;
    public static String SyntaxColoringPreferencePage_Underline;
	
	static {
	        // initialize resource bundle
	        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

    private Messages() {
    }
}
