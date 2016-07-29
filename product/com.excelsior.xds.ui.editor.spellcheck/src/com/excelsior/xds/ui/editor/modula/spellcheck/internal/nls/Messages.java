package com.excelsior.xds.ui.editor.modula.spellcheck.internal.nls;

import java.text.MessageFormat;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS 
{
    private static final String BUNDLE_NAME = "com.excelsior.xds.ui.editor.modula.spellcheck.internal.nls.messages"; //$NON-NLS-1$

    public static String format(String message, Object object) {
        return MessageFormat.format(message, new Object[] { object});
    }

    public static String format(String message, Object[] objects) {
        return MessageFormat.format(message, objects);
    }
    
    public static String Spelling_add_askToConfigure_ignoreMessage;
    public static String Spelling_add_askToConfigure_question;
    public static String Spelling_add_askToConfigure_title;
    public static String Spelling_add_info;
    public static String Spelling_addWordProposal;
    public static String Spelling_case_label;
    public static String Spelling_correct_label;
    public static String Spelling_disable_info;
    public static String Spelling_disable_label;
    public static String Spelling_error_case_label;
    public static String Spelling_error_label;
    public static String Spelling_ignore_info;
    public static String Spelling_ignore_label; 
    
    public static String SpellingPreferenceBlock_BadDictFile;
    public static String SpellingPreferenceBlock_Browse;
    public static String SpellingPreferenceBlock_Dictionaries;
    public static String SpellingPreferenceBlock_Dictionary;
    public static String SpellingPreferenceBlock_Encoding;
    public static String SpellingPreferenceBlock_IgnoreCapitalization;
    public static String SpellingPreferenceBlock_IgnoreM2Strings;
    public static String SpellingPreferenceBlock_IgnoreMixedCaseWords;
    public static String SpellingPreferenceBlock_IgnoreNonLettersBoundaries;
    public static String SpellingPreferenceBlock_IgnoreSingleLetters;
    public static String SpellingPreferenceBlock_IgnoreUpperCaseWords;
    public static String SpellingPreferenceBlock_IgnoreURLs;
    public static String SpellingPreferenceBlock_IgnoreWordsWithDigits;
    public static String SpellingPreferenceBlock_InvalidInt;
    public static String SpellingPreferenceBlock_MaxProblems;
    public static String SpellingPreferenceBlock_MaxProposals;
    public static String SpellingPreferenceBlock_none;
    public static String SpellingPreferenceBlock_NumberRequired;
    public static String SpellingPreferenceBlock_Options;
    public static String SpellingPreferenceBlock_PlatformDictionary;
    public static String SpellingPreferenceBlock_RWAccessRequired;
    public static String SpellingPreferenceBlock_SelectUserDictionary;
    public static String SpellingPreferenceBlock_UserDefDictionary;
    public static String SpellingPreferenceBlock_UserDictDesc;
    public static String SpellingPreferenceBlock_Variables;
    public static String SpellingPreferenceBlock_Advanced;

    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    private Messages() {
    }
}
