package com.excelsior.xds.core.builders;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.excelsior.xds.core.sdk.Sdk;

/**
 * Instances of this class represent a XDS compiler settings.
 * A rich set of XDS compiler settings allows you to control the source language,
 * the generated code, and the internal limits and settings. We distinguish 
 * between boolean options (or just options) and equations. An option can be 
 * set ON (TRUE) or OFF (FALSE), while an equation value is a string. 
 */
public class BuildSettings {

    private final File prjFile;
    private final Sdk  sdk;
    private final File workDir;
    
    private final LookupSettings lookups = new LookupSettings();
    
    /**
     * These equations sets the extensions of the binary artifacts files produced by build.
     */
    private static final Set<String> DERIVED_RESOURCE_EXTENSION_EQUATIONS = new HashSet<>(Arrays.asList("OBJEXT", "EXEEXT",
			"LIBEXT", "SYM")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	/**
	 * Extensions discovered from compiler.
	 */
	private final Set<String> derivedResourcesExtensions = new HashSet<>();

    /**
     * XDS compiler options. An option is a pair (name,value), where value 
     * can be set ON (true) or OFF (false). 
     */
    private final Map<String, Boolean> options = new HashMap<String, Boolean>(); 
    
    /**
     * XDS compiler equations. An equation is a pair (name,value), where value
     * is in general case an arbitrary string. 
     * 
     */
    private final Map<String, String> equations = new HashMap<String, String>();
    
    

    public BuildSettings() {
        this(null, null, null);
    }
    
    public BuildSettings(Sdk sdk, File workDir) {
        this(sdk, workDir, null);
    }
    
    public BuildSettings(Sdk sdk, File workDir, File prjFile) {
        this.prjFile = prjFile;
        this.sdk = sdk;
        this.workDir = workDir;
    }
    
    public BuildSettingsKey createKey() {
    	return new BuildSettingsKey(getSdk(), getWorkDir(), getPrjFile());
    }
    
    public File getPrjFile() {
        return prjFile;
    }

    public Sdk getSdk() {
        return sdk;
    }

    public File getWorkDir() {
        return workDir;
    }

    
    public void setLookupEquations(String lookupEquations) throws IOException {
        lookups.setLookupEquations(lookupEquations, getWorkDir());
    }

    public File lookup(String name) {
        return lookups.lookup(name);
    }
    
    /**
     * Returns all lookup directories to search for files with given file name pattern.
     * 
     * @param fileName - file name to search for (something like "zz.def")
     * @return - list of all lookup directories for the given file name pattern. 
     */
    public List<File> getLookupDirs(String fileName) {
        return lookups.getLookupDirs(fileName);
    }

    public List<File> getLookupDirs() {
        return lookups.getLookupDirs();
    }
    
    /**
     * Adds the specified option to this compiler settings. If the settings
     * previously contained the option, the old value is replaced by 
     * the specified value. 
     * 
     * @param name the unique option name
     * @param enabled the initial enabled state
     * 
     * @return the previous value associated with the option, or
     *         {@code null} if there was no this option in the settings.
     */
    public Boolean addOption(String name, boolean enabled) {
        return options.put(name.trim().toUpperCase(), enabled);
    }
    
    /**
     * Returns the enabled state of the specified option,
     * or {@code false} if this settings contains no such option.
     * 
     * @param name the option name whose value is to be returned
     * 
     * @return the enabled sate of the specified option, or 
     *         {@code false} if this settings contains no such option
     */
    public boolean getOption(String name) {
        Boolean value = options.get(name.trim().toUpperCase()); 
        return (value != null) && value;
    }

    
    /**
     * Returns <tt>true</tt> if this compiler settings contains the specified option.
     * 
     * @param name the option name whose presence in this settings is to be tested
     * 
     * @return <tt>true</tt> if this settings contains the specified option
     */     
    public boolean isOptionDefined(String name) {
        return options.containsKey(name.trim().toUpperCase());
    }
    
    
    /**
     * Adds the specified option to this compiler settings if it is not already
     * present. If this settings already contains the option, the call leaves 
     * the settings unchanged and returns <tt>false</tt>.
     * 
     * @param name the unique equation name
     * @param value the initial value of equation.
     * 
     * @return the previous value associated with the equation, or
     *         <tt>null</tt> if there was no this equation in the settings.
     */
    public String addEquation(String name, String value) {
        if (value != null)
            value = value.trim();
        String equationName = name.trim().toUpperCase();
        if (DERIVED_RESOURCE_EXTENSION_EQUATIONS.contains(equationName)) {
			derivedResourcesExtensions.add(value);
		}
		return equations.put(equationName, value);
    }
    
    public Set<String> getDerivedResourcesExtensions(){
		return derivedResourcesExtensions;
	}

    /**
     * Returns the value of the specified equation,
     * or {@code false} if this settings contains no such equation.
     * 
     * @param name the equation name whose value is to be returned
     * 
     * @return the value of the specified equation, or 
     *         {@code false} if this settings contains no such equation
     */
    public String getEquation(String name) {
        return equations.get(name.trim().toUpperCase());
    }

    /**
     * Returns <tt>true</tt> if this compiler settings contains the specified equation.
     * 
     * @param name the equation name whose presence in this settings is to be tested
     * 
     * @return <tt>true</tt> if this settings contains the specified equation
     */     
    public boolean isEquationDefined(String name) {
        return equations.containsKey(name.trim().toUpperCase());
    }
    
    
    /**
     * Creates and returns a copy of this compiler settings.
     */
    @Override
    public BuildSettings clone() {
        BuildSettings newInstance = new BuildSettings(sdk, workDir, prjFile);
        newInstance.options.putAll(options);
        newInstance.equations.putAll(equations);
        newInstance.lookups.copy(lookups);
        return newInstance;
    }

    /* 
     * For the debug purposes
     */
    @Override
    public String toString() {
        return "BuildSettings [options=" + options + ", equations=" + equations //$NON-NLS-1$ //$NON-NLS-2$
                + "]"; //$NON-NLS-1$
    }
    
}
