package com.excelsior.xds.core.sdk;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.osgi.framework.Version;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.process.ProcessUtils;
import com.excelsior.xds.core.variables.VariableUtils;


/**
 * Provides path to the XDS components (compiler, debugger etc) and environment  
 * necessary  to launch such components. Also stores tool settings (see {@link  com.excelsior.xds.core.tool.ITool}) 
 *
 * @author fsa, lion
 */
public final class Sdk {
	
    private static final char PRIMARY_FILE_EXTENSIONS_SEPARATOR_CHAR = ';';

	/** 
     * Special value for path to unsupported components 
     */
    public static final String NOT_SUPPORTED = "<not.supported>";  //$NON-NLS-1$
    
    private final static Pattern debuggerExecutableSignature = Pattern.compile(".*XdIdeInfoMagic729236\\s+\\[PipeFormat=(\\d+)[.](\\d+)[.](\\d+)\\]\\s+\\[Version=(\\d+)[.](\\d+)[.]?(\\d+)?\\]\\s*", Pattern.DOTALL); //$NON-NLS-1$

    /**
     * xShell formats of compiler output under "__XDS_SHELL__" option
     */
    public static enum XShellFormat {
        UNDEFINED ("undefined"), //$NON-NLS-1$
        BINARY    ("binary"), //$NON-NLS-1$
        TEXT      ("text"); //$NON-NLS-1$

        /** Unique xShell format name to refer in the ini-file and xml-file. */
        public final String name;  
        
        XShellFormat (String name) {
            this.name = name;
        }
    };
    
    public static enum Property {
        // property        | ini-file key       | xml-file tag   
        XDS_NAME           ("xds.name",          "Name"),                                    //$NON-NLS-1$  //$NON-NLS-2$ 
        XDS_HOME           ("xds.home",          "SdkHomePath"),    // optional for sdk.ini  //$NON-NLS-1$  //$NON-NLS-2$
        XDS_COMPILER       ("xds.compiler",      "CompilerExe",                             //$NON-NLS-1$  //$NON-NLS-2$
                            new Tag[]{Tag.CODEPAGE, Tag.COMMANDLINE}),
        XDS_DEBUGGER       ("xds.debugger",      "DebuggerExe"),                             //$NON-NLS-1$  //$NON-NLS-2$
        XDS_PROFILER       ("xds.profiler",      "ProfilerExe"),                             //$NON-NLS-1$  //$NON-NLS-2$
        XDS_SIMULATOR      ("xds.simulator",     "SimulatorExe"),                            //$NON-NLS-1$  //$NON-NLS-2$ 
        XDS_EXE_EXTENSION  ("xds.exe.extension", "ExeExtension"),      // optional. "exe" by default  //$NON-NLS-1$  //$NON-NLS-2$
        XDS_PRIM_EXTENSIONS("xds.primary.extensions",    "XdsPrimExtensions"), // ~ "mod;ob2;def"             //$NON-NLS-1$  //$NON-NLS-2$
        
        XDS_XSHELL_FORMAT  ("xds.xshell.format",         "xShellFormat"),   // optional. UNDEFINED by default  //$NON-NLS-1$  //$NON-NLS-2$

        XDS_LIB_DEFS_PATH  ("xds.library.defs",          "XdsLibraryDefs"), // optional. "$(XDSDIR)/DEF" by default  //$NON-NLS-1$  //$NON-NLS-2$
        
        XDS_FOLDER_PRJ_FILE   ("xds.folder.prj.file", "FolderPrjFile"),       // optional. Project file folder  //$NON-NLS-1$  //$NON-NLS-2$ // default subfolder name to create prj file in (for new project dialog, ~="prjDir")
        XDS_FOLDER_MAIN_MODULE("xds.folder.main.module", "FolderMainModule"), // optional. Main module folder  //$NON-NLS-1$  //$NON-NLS-2$  // default subfolder name to create main module in (for new project dialog, ~="src")
        XDS_TOB2_FILE         ("xds.template.oberon.file", "XdsOb2File"),     // <file>.ob2 - new .ob2 file template  //$NON-NLS-1$  //$NON-NLS-2$
        
        // this properties are come from xds.ini, but may be overridden in sdk.ini  
        XDS_TPR_FILE       ("xds.template.prj.file",     "XdsTprFile"),      // <file>.tpr - new .prj file template  //$NON-NLS-1$  //$NON-NLS-2$
        XDS_TRD_FILE       ("xds.template.red.file",     "XdsTrdFile"),      // <file>.trd - new .red file template  //$NON-NLS-1$  //$NON-NLS-2$
        XDS_MAIN_MOD_FILE  ("xds.template.main.mod.file","XdsMainModFile"),  // <file>.mod - new main .mod file template  //$NON-NLS-1$  //$NON-NLS-2$
        XDS_TMOD_FILE      ("xds.template.mod.file",     "XdsModFile"),      // <file>.mod - new .mod file template  //$NON-NLS-1$  //$NON-NLS-2$
        XDS_TDEF_FILE      ("xds.template.def.file",     "XdsDefFile"),      // <file>.def - new .def file template  //$NON-NLS-1$  //$NON-NLS-2$
        XDS_DIRS_TO_CREATE ("xds.directories.to.create", "XdsDirsToCreate"), // ~ "SYM;OBJ;SRC"                      //$NON-NLS-1$  //$NON-NLS-2$
        XDS_UPDATE_MANIFEST("xds.update.manifiest",      "XdsUpdateManifest"); // path to update.xml  //$NON-NLS-1$  //$NON-NLS-2$
        
        /** SDK property key for external 'sdk.ini' file */
        public final String key;  
        
        /** SDK property key for internal 'sdk.xml' file */
        public final String xmlKey;
        
        /** Array of tags may be used with this property (may be empty but not NULL) */
        public final Tag[] possibleTags;
        
        /** 
         * @param key ini-file key associated with property
         * @param xmlKey xml-tag associated with property
         * @param possibleTags array of tags may be used with this property (may be empty but not NULL)
         */
        Property (String key, String xmlKey, Tag[] possibleTags) {
            this.key = key;	
            this.xmlKey = xmlKey;	
            this.possibleTags = (possibleTags==null) ? new Tag[]{} : possibleTags;
        }
        
        Property (String key, String xmlKey) {
            this(key, xmlKey, null);
        }

    }
    
    /**
     * Each property may have set of additional tags. Tags are written in sdk.ini file as 
     *     <ini_file key>.<tag_name> = <tag value>
     * For example:
     *     xds.compiler.some.options = -killme
     *     
     * Read tag (returns null when tag not exists):
     *     sdk.getTag(Sdk.Property.Property, "some.options");
     *     
     * Set tag:
     *     sdk.setTag(Sdk.Property.Property, "tag.name", "tagValue");
     * Remove tag:
     *     sdk.setTag(Sdk.Property.Property, "tag.name", null);
     * 
     */
    public static enum Tag {
        CODEPAGE   ("codepage", "Codepage"), //$NON-NLS-1$ //$NON-NLS-2$
        COMMANDLINE("commandline", "Commandline"); //$NON-NLS-1$ //$NON-NLS-2$

        /** Tag name to add to property name in 'sdk.ini' file */
        public final String tagName;
        
        /** Tag name to add to property name in internal 'sdk.xml' file */
        public final String xmlTagName;

        
        Tag(String tagName, String xmlTagName) {
            this.tagName = tagName;
            this.xmlTagName = xmlTagName;
        }
    }
    
	private static final EnumSet<Property> FILE_PRORERTIES = EnumSet.of(
			Property.XDS_COMPILER, Property.XDS_DEBUGGER);
	
	private Map<Property, String> propertyName2Value = new HashMap<>();
	
	private XShellFormat xShellFormat; // TODO : store in propertyName2Value
    private Map<String, String> tagsMap = new HashMap<String, String>();
    
	// Environment Variables container in form <Name, Value>
	private Map<String, String> environmentVariables = new HashMap<String, String>();
	
    // Settings of original XDS IDE
    private XdsIniFile transientXdsIni; 	
    
    private List<SdkTool> tools = new ArrayList<SdkTool>();

	private Boolean isDebuggerSupportsIdeIntegration;
	private Version debuggerVersion;
	private Version protocolVersion;
	
	private final SdkManager manager;
	
    /**
     * Create new SDK to edit it
     */
	Sdk(SdkManager manager, String name, String sdkHomePath) {
	    this(manager);
	    setPropertyInternal(Property.XDS_NAME, name);
	    setPropertyInternal(Property.XDS_HOME, sdkHomePath);
	}

    /**
     * Create empty invalid SDK (to read into it from sdk.ini)
     */
	Sdk(SdkManager manager) {
		this.manager = manager;
		setPropertyInternal(Property.XDS_NAME, EMPTY);
		setPropertyInternal(Property.XDS_HOME, EMPTY);
		setPropertyInternal(Property.XDS_COMPILER, EMPTY);
		setPropertyInternal(Property.XDS_DEBUGGER, EMPTY);
		setPropertyInternal(Property.XDS_PROFILER, NOT_SUPPORTED);
		setPropertyInternal(Property.XDS_SIMULATOR, NOT_SUPPORTED);
		setPropertyInternal(Property.XDS_FOLDER_PRJ_FILE, EMPTY);
		setPropertyInternal(Property.XDS_FOLDER_MAIN_MODULE, EMPTY);
		setPropertyInternal(Property.XDS_TOB2_FILE, NOT_SUPPORTED);
		setPropertyInternal(Property.XDS_TPR_FILE, NOT_SUPPORTED);
		setPropertyInternal(Property.XDS_TRD_FILE, NOT_SUPPORTED);
		setPropertyInternal(Property.XDS_MAIN_MOD_FILE, NOT_SUPPORTED);
		setPropertyInternal(Property.XDS_TDEF_FILE, NOT_SUPPORTED);
		setPropertyInternal(Property.XDS_TMOD_FILE, NOT_SUPPORTED);
		setPropertyInternal(Property.XDS_LIB_DEFS_PATH, NOT_SUPPORTED);
		setPropertyInternal(Property.XDS_UPDATE_MANIFEST, NOT_SUPPORTED);
		setPropertyInternal(Property.XDS_EXE_EXTENSION, "exe"); //$NON-NLS-1$
		setPropertyInternal(Property.XDS_PRIM_EXTENSIONS, getDefaultPrimaryFileExtensions());
		setPropertyInternal(Property.XDS_DIRS_TO_CREATE, EMPTY);
        this.xShellFormat = XShellFormat.UNDEFINED;
	}
	
	public boolean isDefault() {
		return ObjectUtils.equals(manager.loadSdkRegistry().getDefaultSdkName(), getName());
	}
	
	private SdkChangeEvent sdkChangeEvent;  
	
	public void beginEdit(){
		if (sdkChangeEvent == null){
			sdkChangeEvent = new SdkChangeEvent(this);
		}
		else {
			Assert.isTrue(false, "Already edited"); //$NON-NLS-1$
		}
	}
	
	public void cancelEdit(){
		sdkChangeEvent = null;
	}

	public void endEdit(){
		if (sdkChangeEvent != null) {
			manager.notifySdkListenersOnChanged(sdkChangeEvent);
			sdkChangeEvent = null;
		}
		else {
			Assert.isTrue(false, "Not being edited");
		}
	}
	
	public boolean isBeingEdited() {
		return sdkChangeEvent != null;
	}
	
	private void recordModification(Property property, String oldValue, String newValue) {
		sdkChangeEvent.setPropertyChanged(property, oldValue, newValue);
	}

	private static String getDefaultPrimaryFileExtensions() {
		return "mod;def;ob2;prj;pkt;ldp"; //$NON-NLS-1$
	}
	
	public static String[] getDefaultPrimaryFileExtensionsAsArray() {
		return StringUtils.split(getDefaultPrimaryFileExtensions(), PRIMARY_FILE_EXTENSIONS_SEPARATOR_CHAR);
	}
	
	public String getName() {
		return getPropertyValue(Property.XDS_NAME);
	}

	public void setName(String name) {
		setPropertyValue(Property.XDS_NAME, name);
	}

	public String getSdkHomePath() {
		return getPropertyValue(Property.XDS_HOME);
	}
	
	public void setSdkHomePath(String sdkHomePath) {
		setPropertyValue(Property.XDS_HOME, sdkHomePath);
	}
	
	public String getCompilerExecutablePath() {
		return getPropertyValue(Property.XDS_COMPILER);
	}
	
	void setCompilerExecutablePath (String compilerExecutablePath) {
		setPropertyValue(Property.XDS_COMPILER, compilerExecutablePath);
	}
	
	public String getDebuggerExecutablePath() {
		return getPropertyValue(Property.XDS_DEBUGGER);
	}
	
    void setDebuggerExecutablePath (String debuggerExecutablePath) {
    	setPropertyValue(Property.XDS_DEBUGGER, debuggerExecutablePath);
        isDebuggerSupportsIdeIntegration = null;
    }
    
    public String getProfilerExecutablePath() {
        return getPropertyValue(Property.XDS_PROFILER);
    }
    
    void setProfilerExecutablePath (String profilerExecutablePath) {
    	setPropertyValue(Property.XDS_PROFILER, profilerExecutablePath);
    }
    
    public String getSimulatorExecutablePath() {
    	return getPropertyValue(Property.XDS_SIMULATOR);
    }
    
    void setSimulatorExecutablePath(String simulatorExecutablePath) {
    	setPropertyValue(Property.XDS_SIMULATOR, simulatorExecutablePath);
    }

    public String getExecutableFileExtensions() {
    	return getPropertyValue(Property.XDS_EXE_EXTENSION);
	}

	void setExecutableFileExtensions(String exeExtension) {
		setPropertyValue(Property.XDS_EXE_EXTENSION, exeExtension);
	}
	
    public String getPrimaryFileExtensions() {
    	return getPropertyValue(Property.XDS_PRIM_EXTENSIONS);
    }
    
    void setPrimaryFileExtensions(String primExtensions) {
    	setPropertyValue(Property.XDS_PRIM_EXTENSIONS, primExtensions);
    }
    
    public String[] getPrimaryFileExtensionsAsArray() {
    	return StringUtils.split(getPrimaryFileExtensions().toLowerCase(), PRIMARY_FILE_EXTENSIONS_SEPARATOR_CHAR);
    }
    
    public String getTprFile() {
    	return getPropertyValue(Property.XDS_TPR_FILE);
    }
    
    void setTprFile(String fileLocation) {    
    	setPropertyValue(Property.XDS_TPR_FILE, fileLocation);
    }
    
    public String getTrdFile() {    
    	return getPropertyValue(Property.XDS_TRD_FILE);
    }
    
    void setTrdFile(String fileLocation) { 
    	setPropertyValue(Property.XDS_TRD_FILE, fileLocation);
    }
    
    public String getMainModFile() {
    	return getPropertyValue(Property.XDS_MAIN_MOD_FILE);
	}

	void setMainModFile(String mainModFile) {
		setPropertyValue(Property.XDS_MAIN_MOD_FILE, mainModFile);
	}

	public String getModFile() {
		return getPropertyValue(Property.XDS_TMOD_FILE);
	}

	void setModFile(String modFile) {
		setPropertyValue(Property.XDS_TMOD_FILE, modFile);
	}

	public String getDefFile() {
		return getPropertyValue(Property.XDS_TDEF_FILE);
	}

	void setDefFile(String defFile) {
		setPropertyValue(Property.XDS_TDEF_FILE, defFile);
	}

	public String getDirsToCreate() {    
		return getPropertyValue(Property.XDS_DIRS_TO_CREATE);
    }
    
    void setDirsToCreate(String s) {
    	setPropertyValue(Property.XDS_DIRS_TO_CREATE, s);
    }

    public String getFolderMainModule() {    
    	return getPropertyValue(Property.XDS_FOLDER_MAIN_MODULE);
    }

    public String getFolderPrjFile() { 
    	return getPropertyValue(Property.XDS_FOLDER_PRJ_FILE);
    }

    public String getLibraryDefinitionsPath() {    
    	return getPropertyValue(Property.XDS_LIB_DEFS_PATH);
    }
    
    public void setLibraryDefinitionsPath(String s) {    
    	setPropertyValue(Property.XDS_LIB_DEFS_PATH, s);
    }
    
	public String getUpdateDescriptorPath() {
		return getPropertyValue(Property.XDS_UPDATE_MANIFEST);
	}

	void setUpdateDescriptorPath(String updateDescriptorPath) {
		setPropertyValue(Property.XDS_UPDATE_MANIFEST, updateDescriptorPath);
	}

	public String getPropertyValue (Property property) {
		if (Property.XDS_XSHELL_FORMAT == property) {
			return xShellFormat.name;
		}
		else {
			return propertyName2Value.get(property);
		}
	}
	
	public boolean isDebuggerSupportsIdeIntegration() {
		checkDebuggerVersion();
		return isDebuggerSupportsIdeIntegration;
	}
	
	public Version getProtocolVersion() {
		checkDebuggerVersion();
		return protocolVersion;
	}
	
	public Version getDebuggerVersion() {
		checkDebuggerVersion();
		return debuggerVersion;
	}

	private void checkDebuggerVersion() {
		if (isDebuggerSupportsIdeIntegration == null) {
			try {
				String stdOut = ProcessUtils.launchProcessAndCaptureStdout(new String[]{getDebuggerExecutablePath(), "-F"}, new File("."), new String[0]); //$NON-NLS-1$ //$NON-NLS-2$
				Matcher matcher = debuggerExecutableSignature.matcher(stdOut);
				isDebuggerSupportsIdeIntegration = matcher.matches();
				
				if (isDebuggerSupportsIdeIntegration) {
					protocolVersion = version(matcher, 0);
					debuggerVersion = version(matcher, 3);
				}
			} catch (CoreException e) {
				LogHelper.logError(e);
				isDebuggerSupportsIdeIntegration = false;
			}
			
		}
	}
	
	private static Version version(Matcher matcher, int from) {
		int major = getGroup(matcher, from + 1);
		int minor = getGroup(matcher, from + 2);
		int micro = getGroup(matcher, from + 3);
		
		return new Version(major, minor, micro);
	}
	
	private static int getGroup(Matcher m, int g) {
		String strGroup = m.group(g);
		if (strGroup == null) {
			return 0;
		}
		return Integer.parseInt(strGroup);
	}
	
	public void setPropertyValue (Property property, String val) {
		String oldVal = getPropertyValue(property);
		setPropertyInternal(property, val);
		
		switch (property) {
		case XDS_HOME:
	        invalidateTransientFields();
			break;
		case XDS_COMPILER:
	        invalidateTransientFields();
			break;
		default:
		}
		
		boolean isEdited = isBeingEdited();
		if (isEdited) {
			recordModification(property, oldVal, val);
		}
	}

	void setPropertyInternal(Property property, String val) {
		if  (property == Property.XDS_XSHELL_FORMAT) {
			// TODO : change to Enum.parse
			for (XShellFormat format: XShellFormat.values()) {
				if (format.name.equals(val)) {
					xShellFormat = format;
					break;
				}
			}
		}
		else {
			propertyName2Value.put(property, val);
		}
	}
	
	String getTag(Property prop, Tag tag) {
	    String tagName = prop.key + "." + tag.tagName; //$NON-NLS-1$
	    return tagsMap.get(tagName);
	}

    void setTag(Property prop, Tag tag, String val) {
        String tagName = prop.key + "." + tag.tagName; //$NON-NLS-1$
        if (val == null) {
            tagsMap.remove(tagName);
        } else {
            tagsMap.put(tagName, val);
        }
    }

    public XShellFormat getXShellFormat() {
        if (xShellFormat == XShellFormat.UNDEFINED) {
            xShellFormat = XShellFormatTracker.test(this);
        }
        return xShellFormat; 
    }

    /**
     * @return environment variables, ${} variables in the values are not opened 
     */
    public Map<String, String> getEnvironmentVariablesRaw() {
    	return environmentVariables;
    }

    /**
     * @return environment variables, ${} variables in the values are opened 
     */
    public Map<String, String> getEnvironmentVariables() {
        HashMap<String, String> res = new HashMap<String, String>();
        for (String name : environmentVariables.keySet()) {
            String val = environmentVariables.get(name);
            try {
                if (val == null) val = StringUtils.EMPTY;
                val = VariableUtils.performStringSubstitution(this, val);
            } catch (CoreException e) {
                LogHelper.logError(e);
            }
            res.put(name,  val);
        }
        return res;
    }

    public void setEnvironmentVariables (Map<String, String> envVars) {
        environmentVariables.clear();
        environmentVariables.putAll(envVars);
        invalidateTransientFields();
    }

    /**
     * @param name variable name
     * @return variable value with not opened ${} variables
     */
    public String getEnvironmentVariableRaw (String name) {
    	return environmentVariables.get(name);
    }

    public void putEnvironmentVariable (String name, String value) {
    	environmentVariables.put(name, value);
        invalidateTransientFields();
    }
    
    public void removeEnvironmentVariable (String name) {
        environmentVariables.remove(name);
        invalidateTransientFields();
    }
    
    public List<SdkTool> getTools() {
		return Collections.unmodifiableList(tools);
	}
    
    public void addTool(SdkTool tool) {
    	tools.add(tool);
    }
    
    public void removeTool(SdkTool tool) {
    	tools.remove(tool);
    }

    public void removeAllTools() {
        tools.clear();
    }

    public boolean moveTool(int idx, boolean up) {
        if (up ? (idx-- > 0) : (idx < tools.size() - 1)) {
            SdkTool t = tools.remove(idx);
            tools.add(idx+1,  t);
            return true;
        }
        return false;
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		String sdkHomePath = getSdkHomePath();
		result = prime * result
			   + ((sdkHomePath == null) ? 0 : sdkHomePath.hashCode());

		for (Property property: FILE_PRORERTIES) {
			String path = this.getPropertyValue(property);
			result = prime * result
			       + ((path == null) ? 0 : path.hashCode());
		}
		
		String name = getName();
		result = prime * result 
		       + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Sdk other = (Sdk) obj;
		for (Property property: Property.values()) {
			String thisValue  = this.getPropertyValue(property);
			String otherValue = other.getPropertyValue(property);
			if (thisValue == null) {
				if (otherValue != null)
					return false;
			} else if (!thisValue.equals(otherValue))
				return false;
		}
				
		return true;
	}
	
	public boolean isValid() {
		// NOTE: the same validation criteria should be used in 
		// com.excelsior.xds.ui.preferences.EditSdkDialog
		// except NOT_SUPPORTED fields
		String name = getName();
		if (name == null || name.length() < 1) {
			return false;
		}

		String sdkHomePath = getSdkHomePath();
		// Folder paths must be available:
		for (String path : new String[] {sdkHomePath}){
			if (path == null){
				return false;
			}
			File folder = new File(path);
			if (!folder.exists() || !folder.isDirectory()) {
				return false;
			}
		}

		// File paths must exist or NOT_SUPPORTED
		for (Property property: FILE_PRORERTIES) {
			String path = this.getPropertyValue(property);
			if (!NOT_SUPPORTED.equals(path)) {
				if (path == null){
					return false;
				}
				File file = new File(path);
				if (!file.exists() || !file.isFile()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public Sdk clone() {
		Sdk newSdk = SdkManager.createSdk();
		newSdk.copyFrom(this);
		return newSdk;
	}
	
	public void copyFrom(Sdk from) {
		for (Property property: Property.values()) {
			setPropertyValue(property, from.getPropertyValue(property));
		}
		setEnvironmentVariables(from.getEnvironmentVariablesRaw());
		this.tools = new ArrayList<SdkTool>(from.getTools());
	}

	
	public XdsIniFile getXdsIni () {
	    if (transientXdsIni == null) {
	        transientXdsIni = new XdsIniFile(this);
	    }
	    return transientXdsIni;
	}
	
	private void invalidateTransientFields () {
	    transientXdsIni = null;
	}
	
	/**
	 * Checks if the given value from SDK is not "not supported" and is not empty
	 * @param value
	 * @return
	 */
	public static boolean isSet(String value) {
	    return (!StringUtils.isBlank(value) && !NOT_SUPPORTED.equals(value));
	}
	
    /**
     * Checks if the given value from SDK is not "not supported" and is name of existent file
     * @param value
     * @return
     */
    public static boolean isFile(String value) {
        return isSet(value) && ((new File(value))).exists();
    }
}