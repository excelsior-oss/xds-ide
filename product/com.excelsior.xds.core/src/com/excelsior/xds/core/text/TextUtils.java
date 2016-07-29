package com.excelsior.xds.core.text;

import java.util.regex.Pattern;

public final class TextUtils {
	public final static String NEW_LINE = System.getProperty("line.separator"); //$NON-NLS-1$
	
	private static final Pattern PATTERN_BACK_SLASH = Pattern.compile("\\\\"); //$NON-NLS-1$

	private static final Pattern PATTERN_QUESTION = Pattern.compile("\\?"); //$NON-NLS-1$

	private static final Pattern PATTERN_STAR = Pattern.compile("\\*"); //$NON-NLS-1$

	private static final Pattern PATTERN_LBRACKET = Pattern.compile("\\("); //$NON-NLS-1$

	private static final Pattern PATTERN_RBRACKET = Pattern.compile("\\)"); //$NON-NLS-1$

	/*
	 * Converts user string to regular expres '*' and '?' to regEx variables.
	 * 
	 */
	private static String asRegEx(String pattern, boolean group) {
		// Replace \ with \\, * with .* and ? with .
		// Quote remaining characters
		String result1 = PATTERN_BACK_SLASH.matcher(pattern).replaceAll("\\\\E\\\\\\\\\\\\Q"); //$NON-NLS-1$
		String result2 = PATTERN_STAR.matcher(result1).replaceAll("\\\\E.*\\\\Q"); //$NON-NLS-1$
		String result3 = PATTERN_QUESTION.matcher(result2).replaceAll("\\\\E.\\\\Q"); //$NON-NLS-1$
		if (group) {
			result3 = PATTERN_LBRACKET.matcher(result3).replaceAll("\\\\E(\\\\Q"); //$NON-NLS-1$
			result3 = PATTERN_RBRACKET.matcher(result3).replaceAll("\\\\E)\\\\Q"); //$NON-NLS-1$
		}
		return "\\Q" + result3 + "\\E"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Creates a regular expression pattern from the pattern string (which is
	 * our old 'StringMatcher' format).
	 * 
	 * @param pattern
	 *            The search pattern
	 * @param isCaseSensitive
	 *            Set to <code>true</code> to create a case insensitve pattern
	 * @return The created pattern
	 */
	public static Pattern createPattern(String pattern, boolean isCaseSensitive) {
		if (isCaseSensitive)
			return Pattern.compile(asRegEx(pattern, false));
		return Pattern.compile(asRegEx(pattern, false), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	}
	
	/**
	 * Returns double-quoted input text if input text contains spaces
	 * @param text
	 * @return
	 */
	public static String enquoteIfHasSpace(String text) {
    	if (Pattern.compile("\\s+").matcher(text).find()) { //$NON-NLS-1$
    		return String.format("\"%s\"", text); //$NON-NLS-1$
    	}
    	else {
    		return text;
    	}
    }
	
	/**
	 * Only static methods are allowed
	 */
	private TextUtils(){
	}
}
