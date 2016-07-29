package com.excelsior.xds.ui.editor.internal.preferences;

import com.excelsior.xds.ui.editor.XdsEditorsPlugin;

/**
 * Preference constants used in the XDS editors preference store.
 * 
 * @author lion
 */
public interface IXdsEditorsPreferenceIds {

    public static final String PREFIX = XdsEditorsPlugin.PLUGIN_ID + ".";   //$NON-NLS-1$

    /** 
     * A named preference that controls enable/disable highlight matching brackets. 
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     */
    public final static String PREF_HIGHLIGHT_MATCHING_BRACKETS = PREFIX + "HighlightMatchingBrackets";  //$NON-NLS-1$
    
    /** 
     * A named preference that holds a color of matched brackets. 
     * <p>
     * Value is of type <code>RGB</code>.
     * </p>
     */
    public final static String PREF_MATCHED_BRACKETS_COLOR = PREFIX + "MatchedBracketsColor";  //$NON-NLS-1$ 
    
    /** 
     * A named preference that holds a color of unmatched brackets. 
     * <p>
     * Value is of type <code>RGB</code>.
     * </p>
     */
    public final static String PREF_UNMATCHED_BRACKETS_COLOR = PREFIX + "UnmatchedBracketsColor";  //$NON-NLS-1$ 

}
