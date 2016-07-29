package com.excelsior.xds.core.sdk;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.ini4j.Wini;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.sdk.Sdk.Property;
import com.excelsior.xds.core.text.TextEncoding;

/**
 * Reads external description of XDS development system from the "sdk.ini" file.
 * 
 * Example of composite "sdk.ini" file:
 *     # import description of Native XDS-x86 Win32 Edition
 *     !import = sdk-x86.ini
 *     # import description of XDS-C Win32 Edition
 *     !import = sdk-c.ini * 
 * 
 * Example of simple sdk.ini (for example "sdk-x86.ini") file: 
 *     # name of Development system
 *     xds.name = XDS-x86 2.60
 *     # Relative path to compiler
 *     xds.compiler = bin\xc.exe
 *     # Relative path to compiler
 *     xds.debugger = bin\xd.exe
 *
 * @author fsa, lion
 */
public class SdkIniFileReader {
	
    private static final String MAIN_INI_FILE_NAME = "sdk.ini";  //$NON-NLS-1$

    private static final String ENVIRONMENT_SECTION_NAME = "Environment";  //$NON-NLS-1$
    private static final String TOOL_SECTION_NAME        = "Tool";         //$NON-NLS-1$
    
    private static final String ENVVAR_XDSDIR = "XDSDIR";  //$NON-NLS-1$

//	private static final int MAX_TOOL_NUMBER = 1000;
	private ArrayList<Sdk> aSdk;
	private StringBuilder sbErrLog;
	
	// lines started with this char(s) are comments
	private static final String COMMENT_LINE_CHARS = "#";  //$NON-NLS-1$     
	
	// key for import file property
	private static final String IMPORT_PROPERTY    = "!import";  //$NON-NLS-1$
	
	
	/**
	 * Read sdk.ini file (or file tree with includes) from the given location. 
	 * @param xdsHomePath - folder to search sdk.ini file
	 */
	public SdkIniFileReader(String xdsHomePath) {
		aSdk        = new ArrayList<Sdk>();
		sbErrLog    = new StringBuilder();
		
		try {
		    Ini ini = loadFile(xdsHomePath, MAIN_INI_FILE_NAME, true);
		    if (ini != null) {
	            Section global = ini.get(Config.DEFAULT_GLOBAL_SECTION_NAME);
	            List<String> imports = global.getAll(IMPORT_PROPERTY);
	            if ((imports == null) || imports.isEmpty()) {
	                processIniFile(xdsHomePath, MAIN_INI_FILE_NAME);
	            } else {
	                for (String import_file_name: imports) {
	                    processIniFile(xdsHomePath, import_file_name);
	                }
	            }
		    }
        } catch (Exception e) {
            LogHelper.logError(e);
            aSdk.clear();
            setError(e.getMessage());
        }
		
	}
	
	/**
	 * Returns the list of the SDKs which are defined in sdk.ini file.
     * The list may be empty if sdk.ini file does not exist or its format is invalid.
     * In the last case, use getError() to get error message.
	 *  
	 * @return array of the available SDKs.
	 */
	public Sdk[] getSdk() {
		return aSdk.toArray(new Sdk[]{});
	}
	
	/**
	 * Returns error message for invalid sdk.ini file.
	 *  
	 * @return error message or null if there was no errors.
	 */
	public String getError() {
		return sbErrLog.toString();
	}

	/**
    * @param basePath  the base path to attach to, if relative file name is used
    * @param fileName  the name of the file to be loaded 
    * @param enableMultiOption  the state of multi-option configuration flag  
    */
	private Ini loadFile ( String basePath, String fileName
	                     , boolean enableMultiOption ) throws Exception 
	{
	    String full_name = FilenameUtils.concat(basePath, fileName);
	    File file = new File(full_name); 
        if (file.exists() && file.isFile()) {
            Ini ini = new Wini();
            Config config = ini.getConfig();
            
            config.setMultiSection(true);
            config.setMultiOption(enableMultiOption);
            config.setGlobalSection(true);
            config.setComment(true);

            ini.setFile(file);
            ini.setComment(COMMENT_LINE_CHARS);
            try(InputStreamReader inputStreamReader = TextEncoding.getInputStreamReader(file)){
            	ini.load(inputStreamReader);
            }
            return ini;
        }
	    return null;
	}
	
    private void processIniFile(String basePath, String fileName) throws Exception {
        Ini ini = loadFile(basePath, fileName, false);
        String homePath = ini.getFile().getParentFile().getCanonicalPath();

        Sdk sdk = SdkManager.createSdk();
        sdk.setPropertyInternal(Property.XDS_HOME, homePath);

        for (Sdk.Property property: Sdk.Property.values()) {
            String val = ini.get(Config.DEFAULT_GLOBAL_SECTION_NAME, property.key); // '?' for the default (global) section
            if (val != null) {
                switch (property) {
                case XDS_NAME:
                    sdk.setPropertyInternal(Property.XDS_NAME, val);
                    break;
                case XDS_HOME:
                    sdk.setPropertyInternal(Property.XDS_HOME, makePath(homePath, val));
                    break;
                case XDS_EXE_EXTENSION:
                    sdk.setPropertyInternal(Property.XDS_EXE_EXTENSION, val);
                    break;
                case XDS_PRIM_EXTENSIONS:
                    sdk.setPropertyInternal(Property.XDS_PRIM_EXTENSIONS, val);
                    break;
                default:
                    if ( Sdk.Property.XDS_XSHELL_FORMAT.equals(property) 
                      || Sdk.Property.XDS_DIRS_TO_CREATE.equals(property)
                      || Sdk.Property.XDS_FOLDER_PRJ_FILE.equals(property)
                      || Sdk.Property.XDS_FOLDER_MAIN_MODULE.equals(property)) 
                    {
                        sdk.setPropertyInternal(property, val);
                    } else {
                        sdk.setPropertyInternal(property, makePathNS(homePath, val));
                    }
                }
            }
                
            for (Sdk.Tag tag : property.possibleTags) {
                String tval = ini.get(Config.DEFAULT_GLOBAL_SECTION_NAME, property.key + "." + tag.tagName); //$NON-NLS-1$
                if (tval != null) {
                    sdk.setTag(property, tag, tval);
                }
            }
        }
        
        adjustSettings(ini.getFile(), sdk);
        
        // Get additional settings from xds.ini etc.:
        addSettingsFromLocation(sdk);

        processEnvironmentSection(sdk, ini);
        processToolSections(sdk, ini);
        
        aSdk.add(sdk);        
    }


    private String makePathNS (String basePath, String path) throws Exception {
        if (Sdk.NOT_SUPPORTED.equals(path)) {
            return path;
        } 
        return makePath(basePath, path); 
    }
    
    private String makePath (String basePath, String path) throws Exception {
        return FilenameUtils.concat(basePath, path);
    }
    
    
    private void adjustSettings(File f, Sdk sdk) throws Exception {
        if (StringUtils.isBlank(sdk.getName())) {
            throw new Exception(f.getAbsolutePath() + " -- Error: invalid SDK definition file (must define sdk name at least)"); //$NON-NLS-1$
        }
        if (sdk.getEnvironmentVariableRaw(ENVVAR_XDSDIR) == null) {
            sdk.putEnvironmentVariable(ENVVAR_XDSDIR, sdk.getSdkHomePath());
        }
    }

    
	private void processEnvironmentSection(Sdk sdk, Ini ini) {
        List<Section> environments = ini.getAll(ENVIRONMENT_SECTION_NAME);
        if (environments != null) {
            for (Section envSection : environments) {
                Set<Entry<String, String>> entrySet = envSection.entrySet();
                for (Entry<String, String> entry : entrySet) {
                    sdk.putEnvironmentVariable(entry.getKey(), entry.getValue());
                }
            }
        }
	}

	private void processToolSections(Sdk sdk, Ini ini) {
	    List<Section> tools = ini.getAll(TOOL_SECTION_NAME);
	    if (tools != null) {
	        for (Section toolSection : tools) {
                SdkTool tool = parseTool(sdk, toolSection);
                if (tool.isValid()) {
                    sdk.addTool(tool);
                } else {
                    LogHelper.logError(tool.getErrorMessage());
                }
	        }
	    }
	}

	private SdkTool parseTool(Sdk sdk, Section toolSection) {
        SdkTool tool;
	    if (toolSection.containsKey("isSeparator")) { //$NON-NLS-1$
	        tool = new SdkTool(); // new SdkTool() makes separator, not a tool
	    } else {
            tool = new SdkTool(sdk);
	    }
		for (SdkTool.Property property : SdkTool.Property.values()) {
		    String value = toolSection.get(property.key);
		    if (value != null) {
		        tool.setPropertyValue(property, value);
		    }
		}
		return tool;
	}
	

	private void setError(String s) {
		sbErrLog.append(s).append("\n"); //$NON-NLS-1$
	}

	
	/**
	 * Sdk may have not initialized some fields: templates, directories to create etc.
	 * Even if sdk.ini is not initialized it or Sdk was created without sdk.ini - some
	 * this data may be collected from its home path, xds.ini and compiler mane if any.
	 */
	public static void addSettingsFromLocation(Sdk sdk) {
        XdsIniFile ini = sdk.getXdsIni();
        String s;
        if (sdk.getDirsToCreate().isEmpty()) {
            s = ini.getDirectoriesToCreate();
            if (s != null) {
                sdk.setDirsToCreate(s.trim());
            }
        }
        File f;
        // -- .tpr if it is still empty:
        if (sdk.getTprFile().isEmpty()) {
            s = ini.getProjectTemplateFile();
            if (s != null) {
                f = new File(s.trim());
                if (f.isFile()) {
                    sdk.setTprFile(f.getAbsolutePath());
                }
            } 
        }
        // -- .trd if it is still empty:
        if (sdk.getTrdFile().isEmpty()) {
            s = ini.getRedirectionTemplateFile();
            if (s != null) {
                f = new File(s.trim());
                if (f.isFile()) {
                    sdk.setTrdFile(f.getAbsolutePath());
                }
            }
        }
        
        // .tpr and .trd files may be constructed from compiler name
        s = FilenameUtils.removeExtension(sdk.getCompilerExecutablePath());
        if (!StringUtils.isEmpty(s)) {
            // -- .tpr if it is still empty:
            if (sdk.getTprFile().isEmpty()) {
                f = new File(s + ".tpr"); //$NON-NLS-1$
                if (f.isFile()) {
                    sdk.setTprFile(f.getAbsolutePath());
                }
            }
            // -- .trd if it is still empty:
            if (sdk.getTrdFile().isEmpty()) {
                f = new File(s + ".trd"); //$NON-NLS-1$
                if (f.isFile()) {
                    sdk.setTrdFile(f.getAbsolutePath());
                }
            }
            
        }
	}
	
}
