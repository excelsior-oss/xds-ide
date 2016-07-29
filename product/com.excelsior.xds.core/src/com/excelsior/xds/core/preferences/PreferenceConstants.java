package com.excelsior.xds.core.preferences;


public final class PreferenceConstants {
    private PreferenceConstants() {
    	super();
	}

	/**
     * A named preference that controls whether words containing digits should
     * be skipped during spell checking.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     *
     * @since 3.0
     */
    public final static String SPELLING_IGNORE_DIGITS= "xds_spelling_ignore_digits"; //$NON-NLS-1$

    /**
     * A named preference that controls whether mixed case words should be
     * skipped during spell checking.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     *
     * @since 3.0
     */
    public final static String SPELLING_IGNORE_MIXED= "xds_spelling_ignore_mixed"; //$NON-NLS-1$

    /**
     * A named preference that controls whether sentence capitalization should
     * be ignored during spell checking.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     *
     * @since 3.0
     */
    public final static String SPELLING_IGNORE_SENTENCE= "xds_spelling_ignore_sentence"; //$NON-NLS-1$

    /**
     * A named preference that controls whether upper case words should be
     * skipped during spell checking.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     *
     * @since 3.0
     */
    public final static String SPELLING_IGNORE_UPPER= "xds_spelling_ignore_upper"; //$NON-NLS-1$

    /**
     * A named preference that controls whether URLs should be ignored during
     * spell checking.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     *
     * @since 3.0
     */
    public final static String SPELLING_IGNORE_URLS= "xds_spelling_ignore_urls"; //$NON-NLS-1$

    /**
     * A named preference that controls whether non-letters at word boundaries
     * should be ignored during spell checking.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     *
     * @since 3.3
     */
    public final static String SPELLING_IGNORE_NON_LETTERS= "xds_spelling_ignore_non_letters"; //$NON-NLS-1$

    /**
     * A named preference that controls whether single letters
     * should be ignored during spell checking.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     *
     * @since 3.3
     */
    public final static String SPELLING_IGNORE_SINGLE_LETTERS= "xds_spelling_ignore_single_letters"; //$NON-NLS-1$

    /**
     * A named preference that controls whether Java strings
     * should be ignored during spell checking.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     *
     * @since 3.4
     */
    public static final String SPELLING_IGNORE_MODULA_STRINGS= "xds_spelling_ignore_modula_strings"; //$NON-NLS-1$;


    /**
     * A named preference that controls the locale used for spell checking.
     * <p>
     * Value is of type <code>String</code>.
     * </p>
     *
     * @since 3.0
     */
    public final static String SPELLING_LOCALE= "xds_spelling_locale"; //$NON-NLS-1$

    /**
     * A named preference that specifies the workspace user dictionary.
     * <p>
     * Value is of type <code>Integer</code>.
     * </p>
     *
     * @since 3.0
     */
    public final static String SPELLING_USER_DICTIONARY= "xds_spelling_user_dictionary"; //$NON-NLS-1$

    /**
     * A named preference that specifies encoding of the workspace user dictionary.
     * <p>
     * Value is of type <code>String</code>.
     * </p>
     *
     * @since 3.3
     */
    public final static String SPELLING_USER_DICTIONARY_ENCODING= "xds_spelling_user_dictionary_encoding"; //$NON-NLS-1$

    /**
     * A named preference that controls the maximum number of problems reported during spell checking.
     * <p>
     * Value is of type <code>Integer</code>.
     * </p>
     *
     * @since 3.4
     */
    public final static String SPELLING_PROBLEMS_THRESHOLD= "xds_spelling_problems_threshold"; //$NON-NLS-1$

    /**
     * A named preference that controls the number of proposals offered during
     * spell checking.
     * <p>
     * Value is of type <code>Integer</code>.
     * </p>
     *
     * @since 3.0
     */
    public final static String SPELLING_PROPOSAL_THRESHOLD= "xds_spelling_proposal_threshold"; //$NON-NLS-1$

    
    public static final String PREF_VALUE_NO_LOCALE = ""; //$NON-NLS-1$
}
