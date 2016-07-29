package com.excelsior.xds.core.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;

import com.excelsior.xds.core.XdsCorePlugin;
import com.excelsior.xds.core.utils.GraphicUtils;

/**
 * Preference keys of the com.excelsior.xds.core plugins
 * @author lsa80
 */
public final class PreferenceKeys
{
    private static final String XDS_QUALIFIER = XdsCorePlugin.PLUGIN_ID;

    //--------------------------------------------------------------------------
    // Keys for Spelling Checker:
    //--------------------------------------------------------------------------

    public final static PreferenceKey PKEY_SPELLING_IGNORE_DIGITS = new PreferenceKey(
            XDS_QUALIFIER, PreferenceConstants.SPELLING_IGNORE_DIGITS, true);

    public final static PreferenceKey PKEY_SPELLING_IGNORE_MIXED = new PreferenceKey(XDS_QUALIFIER,
            PreferenceConstants.SPELLING_IGNORE_MIXED, true);

    public final static PreferenceKey PKEY_SPELLING_IGNORE_SENTENCE = new PreferenceKey(
            XDS_QUALIFIER, PreferenceConstants.SPELLING_IGNORE_SENTENCE, true);

    public final static PreferenceKey PKEY_SPELLING_IGNORE_UPPER = new PreferenceKey(XDS_QUALIFIER,
            PreferenceConstants.SPELLING_IGNORE_UPPER, true);

    public final static PreferenceKey PKEY_SPELLING_IGNORE_URLS = new PreferenceKey(XDS_QUALIFIER,
            PreferenceConstants.SPELLING_IGNORE_URLS, true);

    public final static PreferenceKey PKEY_SPELLING_IGNORE_NON_LETTERS = new PreferenceKey(
            XDS_QUALIFIER, PreferenceConstants.SPELLING_IGNORE_NON_LETTERS, true);

    public final static PreferenceKey PKEY_SPELLING_IGNORE_SINGLE_LETTERS = new PreferenceKey(
            XDS_QUALIFIER, PreferenceConstants.SPELLING_IGNORE_SINGLE_LETTERS, true);

    public final static PreferenceKey PKEY_SPELLING_IGNORE_MODULA_STRINGS = new PreferenceKey(
            XDS_QUALIFIER, PreferenceConstants.SPELLING_IGNORE_MODULA_STRINGS, true);

    public final static PreferenceKey PKEY_SPELLING_LOCALE = new PreferenceKey(XDS_QUALIFIER,
            PreferenceConstants.SPELLING_LOCALE, PreferenceConstants.PREF_VALUE_NO_LOCALE);

    public final static PreferenceKey PKEY_SPELLING_USER_DICTIONARY = new PreferenceKey(
            XDS_QUALIFIER, PreferenceConstants.SPELLING_USER_DICTIONARY, ""); //$NON-NLS-1$

    public final static PreferenceKey PKEY_SPELLING_USER_DICTIONARY_ENCODING = new PreferenceKey(
            XDS_QUALIFIER, PreferenceConstants.SPELLING_USER_DICTIONARY_ENCODING, ""); //$NON-NLS-1$

    public final static PreferenceKey PKEY_SPELLING_PROBLEMS_THRESHOLD = new PreferenceKey(
            XDS_QUALIFIER, PreferenceConstants.SPELLING_PROBLEMS_THRESHOLD, 100);

    public final static PreferenceKey PKEY_SPELLING_PROPOSAL_THRESHOLD = new PreferenceKey(
            XDS_QUALIFIER, PreferenceConstants.SPELLING_PROPOSAL_THRESHOLD, 20);
    
    
    //--------------------------------------------------------------------------
    // Keys for Editor Preference:
    //--------------------------------------------------------------------------

    /**
     * A named preference that controls enable/disable highlight inactive code.
     * Code which is disabled by conditional compilation pragmas. 
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     */
    public final static PreferenceKey PKEY_HIGHLIGHT_INACTIVE_CODE = new PreferenceKey(
           XDS_QUALIFIER, "xds_editor.highlight_inactive_code", true);
    
    /**
     * Preference to show symbol occurrences highlighting
     */
    public final static PreferenceKey PKEY_HIGHLIGHT_OCCURENCES = new PreferenceKey(
            XDS_QUALIFIER, "xds_editor.highlight_occurrences", true);

    /**
     * Preference to show symbol language constructions highlighting
     */
    public final static PreferenceKey PKEY_HIGHLIGHT_CONSTRUCTIONS = new PreferenceKey(
            XDS_QUALIFIER, "xds_editor.highlight_constructions", true);
    
    /**
	 * Preference for the color of the active code (executable source code),
	 * shown in the Disassembly view and as a background for the line numbers at
	 * the line number column of the Modula editor.
	 */
    public final static PreferenceKey PKEY_EXECUTABLE_SOURCE_CODE_COLOR = new PreferenceKey(
			XDS_QUALIFIER, "debugger__active_runtime_code_color",
			StringConverter.asString(GraphicUtils.lighter(new RGB(0xE6, 0xFD, 0xE6), 0.0f)));

	/**
	 * Show line number column in the Modula editor during the Debug session, even if General/Editors/Text editors/ setting prohibit it.
	 */
	public final static PreferenceKey OVERRIDE_SHOW_LINE_NUMBER_COLUMN = new PreferenceKey(
			XDS_QUALIFIER, "modula_editor_override_show_line_number_column__during_debug", true); //$NON-NLS-1$
    
    public static void addChangeListener(IPreferenceChangeListener listener){
    	PreferenceKey.addChangeListener(XdsCorePlugin.PLUGIN_ID, listener);
    }
    
    public static void removeChangeListener(IPreferenceChangeListener listener){
    	PreferenceKey.removeChangeListener(XdsCorePlugin.PLUGIN_ID, listener);
    }
}