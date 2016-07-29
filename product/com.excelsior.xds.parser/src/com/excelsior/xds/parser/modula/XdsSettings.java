package com.excelsior.xds.parser.modula;

import java.util.Stack;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.builders.DefaultBuildSettingsHolder;
import com.excelsior.xds.core.sdk.XdsOptions;

/**
 * Configuration layer of XDS development system.
 */
public final class XdsSettings 
{
    private BuildSettings settings;
    private Stack<BuildSettings> stack;

    private boolean oberon;
    private final boolean odfSource;

    private boolean isoPragma;
    private boolean oberonNumExt;
    private boolean gotoExtension;
    private boolean enhancedDereference;

    public final XdsSourceType sourceType;
    
    private boolean isBuildAst = true;

    public XdsSettings(BuildSettings settings, XdsSourceType sourceType) 
    {
        odfSource = (sourceType == XdsSourceType.OdfFile);
        this.sourceType = sourceType;
        this.oberon = (sourceType == XdsSourceType.Oberon) || odfSource;
        this.settings = (settings == null) 
                      ? DefaultBuildSettingsHolder.DefaultBuildSettings.clone() 
                      : settings.clone();
        updateHeaderOptions();
    }
    
    public BuildSettings getSettings() {
		return settings;
	}

	public boolean isBuildAst() {
		return isBuildAst;
	}

    public void setBuildAst(boolean isBuildAst) {
		this.isBuildAst = isBuildAst;
	}

	public void updateHeaderOptions() {
        if (odfSource) {
            settings.addOption(XdsOptions.M2EXTENSIONS, true);
            settings.addOption(XdsOptions.M2ADDTYPES, true);
            settings.addOption(XdsOptions.O2ISOPRAGMA, true);
            settings.addOption(XdsOptions.O2EXTENSIONS, true);
            settings.addOption(XdsOptions.O2ADDKWD, true);
        }

        isoPragma = !oberon || getOption(XdsOptions.O2ISOPRAGMA);
        oberonNumExt = getOption(XdsOptions.O2NUMEXT);
        gotoExtension = topSpeedExtensions() || getOption(XdsOptions.M2GOTO);
        enhancedDereference = topSpeedExtensions() || m2Extensions();
    }

    public void popSettings() {
        if ((stack != null) && !stack.isEmpty()) {
            settings = stack.pop();
        }
    }

    public void pushSettings() {
        if (stack == null) {
            stack = new Stack<BuildSettings>();
        }
        stack.push(settings);
        settings = settings.clone();
    }

    /**
     * Adds the specified option to this compiler settings. If the settings
     * previously contained the option, the old value is replaced by the
     * specified value.
     * 
     * @param name
     *            the unique option name
     * @param enabled
     *            the initial enabled state
     * 
     * @return the previous value associated with the option, or {@code null} if
     *         there was no this option in the settings.
     */
    public Boolean addOption(String name, boolean enabled) {
        return settings.addOption(name, enabled);
    }

    /**
     * Returns <tt>true</tt> if this compiler settings contains the specified
     * option.
     * 
     * @param name
     *            the option name whose presence in this settings is to be
     *            tested
     * 
     * @return <tt>true</tt> if this settings contains the specified option
     */
    public boolean isOptionDefined(String name) {
        return settings.isOptionDefined(name) || isMessageControlOption(name);
    }

    /**
     * Returns the enabled state of the specified option, or {@code false} if
     * this settings contains no such option.
     * 
     * @param name
     *            the option name whose value is to be returned
     * 
     * @return the enabled sate of the specified option, or {@code false} if
     *         this settings contains no such option
     */
    public boolean getOption(String name) {
        return settings.getOption(name);
    }

    /**
     * Adds the specified option to this compiler settings if it is not already
     * present. If this settings already contains the option, the call leaves
     * the settings unchanged and returns <tt>false</tt>.
     * 
     * @param name
     *            the unique equation name
     * @param value
     *            the initial value of equation.
     * 
     * @return the previous value associated with the equation, or <tt>null</tt>
     *         if there was no this equation in the settings.
     */
    public String addEquation(String name, String value) {
        return settings.addEquation(name, value);
    }

    /**
     * Returns the value of the specified equation, or {@code false} if this
     * settings contains no such equation.
     * 
     * @param name
     *            the equation name whose value is to be returned
     * 
     * @return the value of the specified equation, or {@code false} if this
     *         settings contains no such equation
     */
    public String getEquation(String name) {
        return settings.getEquation(name);
    }

    /**
     * Returns <tt>true</tt> if this compiler settings contains the specified
     * equation.
     * 
     * @param name
     *            the equation name whose presence in this settings is to be
     *            tested
     * 
     * @return <tt>true</tt> if this settings contains the specified equation
     */
    public boolean isEquationDefined(String name) {
        return settings.isEquationDefined(name);
    }

    /** XDS language extensions */
    public boolean xdsExtensions() {
        return (oberon && getOption(XdsOptions.O2EXTENSIONS))
            || getOption(XdsOptions.M2EXTENSIONS);
    }

    /** TopSpeed Modula-2 extensions */
    public boolean topSpeedExtensions() {
        return !oberon && getOption(XdsOptions.TOPSPEED);
    }

    /** TopSpeed Modula-2 extensions */
    public boolean m2Extensions() {
        return !oberon && getOption(XdsOptions.M2EXTENSIONS);
    }

    /**
     * Sets language mode of the module.
     * 
     * @param language
     *            the module language.
     */
    public final void setLanguage(XdsLanguage language) {
        oberon = (XdsLanguage.Oberon2 == language);
    }

    /**
     * Returns <code>true</code> if the Oberon-2 language is enabled.
     * 
     * @return the Oberon-2 language enabled state
     */
    public final boolean isOberon() {
        return oberon;
    }

    /**
     * Returns <code>true</code> if the Oberon-2 definition source file is
     * enabled.
     * 
     * @return the Oberon-2 definition source file enabled state.
     */
    public final boolean isOdfSource() {
        return odfSource;
    }

    /**
     * Returns <code>true</code> if the Oberon-2 scientific extensions are
     * enabled.
     * 
     * @return the Oberon-2 scientific extensions enabled state.
     */
    public final boolean isOberonScientificExtensions() {
        return oberonNumExt;
    }

    /**
     * Returns <code>true</code> if the ISO Modula-2 pragmas in Oberon are
     * enabled.
     * 
     * @return the ISO Modula-2 pragmas in Oberon enabled state.
     */
    public final boolean isIsoPragma() {
        return isoPragma;
    }

    /**
     * Returns <code>true</code> if the support of GOTO statement is enabled.
     * 
     * @return the support of GOTO statement enabled state.
     */
    public final boolean isGotoExtension() {
        return gotoExtension;
    }

    public final boolean isEnhancedDereference() {
        return enhancedDereference;
    }


    private static final String WOFF = "WOFF";  //$NON-NLS-1$
    private static final String WON  = "WON";   //$NON-NLS-1$
    private static final String WERR = "WERR";  //$NON-NLS-1$
    
    private boolean isMessageControlOption(String name) {
        name = name.trim().toUpperCase();
        String option = null;
        if (name.startsWith(WOFF)) {
            option = WOFF;
        }
        else if (name.startsWith(WERR)) {
            option = WERR;
        }
        else if (name.startsWith(WON)) {
            option = WON;
        }
        else {
            return false;
        }

        String messageId = name.substring(option.length());
        for (char ch: messageId.toCharArray()) {
            if (!Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }
    
}
