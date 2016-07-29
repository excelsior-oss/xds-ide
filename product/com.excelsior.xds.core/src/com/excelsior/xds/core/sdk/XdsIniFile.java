package com.excelsior.xds.core.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.text.TextEncoding;

/**
 * Provides access to settings of original XDS IDE which are stored 
 * in the "xds.ini" file.  
 */
public class XdsIniFile {

    // variables used in 'xds.ini' file
    private final String xdsdir;
    private final String xdsname;
    
    // xds.ini file contents in form <SectionName, <Key, Value>>
    private final HashMap<String, HashMap<String, String>> content;
    
    XdsIniFile (Sdk sdk) {
        content = new HashMap<String, HashMap<String, String>>();

        String compiler = sdk.getCompilerExecutablePath();
        
        String dir = null;
        if (compiler != null) {
        	dir = (new File(compiler)).getParent();
        }
        if (dir == null) {
        	dir = FilenameUtils.concat(sdk.getSdkHomePath(), "bin"); //$NON-NLS-1$
        }
        xdsdir = dir;
        
        xdsname = (compiler == null) 
                ? ""  //$NON-NLS-1$
                : FilenameUtils.getBaseName(compiler);
        readXdsIniFile(FilenameUtils.getFullPath(compiler));
    }

    /**
     * @return value like "$(xdsdir)\$(xdsname).tpr", "xc.tpr" or null
     */
    public String getProjectTemplateFile () {
        return getValue("new-project", "template"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @return value like "src\$(projname).mod" or null
     */
    public String getMainModuleName () {
        return getValue("new-project", "default-main-module"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @return value like "$(xdsdir)\$(xdsname).trd", "xc.trd" or null
     */
    public String getRedirectionTemplateFile () {
        return getValue("new-project", "redfile"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @return string like "DEF;SRC;OBJ;SYM" or null
     */
    public String getDirectoriesToCreate() {
        return getValue("new-project", "directories"); //$NON-NLS-1$ //$NON-NLS-2$
    }
 
    /**
     * Note: it is case insensitive 
     */
    private String getValue(String sectionName, String key) {
        HashMap<String, String> section = content.get(sectionName.toLowerCase());
        if (section == null)
            return null;
        
        String value = section.get(key.toLowerCase());
        return value == null ? null
                             : replaceVariables(value); 
    }
    
    private String replaceVariables (String str) {
        str = str.replace("$(xdsdir)",  xdsdir);   // ~= "c:\xds\bin" //$NON-NLS-1$
        str = str.replace("$(xdsname)", xdsname);  // ~= "xc" //$NON-NLS-1$
        return str;
    }
    
    private void readXdsIniFile (String location) {
        if (location == null) 
            return;
        File file = new File(FilenameUtils.concat(location, "xds.ini")); //$NON-NLS-1$
        if (!file.isFile())
            return;

        // parse and cache xds.ini file:
        try {
        	StringBuilder sb = new StringBuilder();
        	TextEncoding.readFileAndCodepage(file, sb, null);

        	BufferedReader br = new BufferedReader(new StringReader(sb.toString()));
        	HashMap<String, String> section = null;
        	String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // [section name]
                if (line.length()>2 && line.charAt(0)=='[' && line.charAt(line.length()-1)==']') {
                    line = line.substring(1,line.length()-1).trim().toLowerCase();
                    if (line.length()>0) {
                        section = content.get(line); // section name
                        if (section == null) {
                            section = new HashMap<String, String>();
                            content.put(line,  section);
                        }
                    } else {
                        section = null;
                    }
                } else if (section != null && line.contains("=")) { //$NON-NLS-1$
                    char c0 = Character.toLowerCase(line.charAt(0));
                    if (c0 >= 'a' && c0 <= 'z') {
                        int pos = line.indexOf('=');
                        String key = line.substring(0,pos).trim().toLowerCase();
                        String val = line.substring(pos+1).trim();
                        section.put(key, val);
                    }
                }
            }
            br.close();
        } catch (IOException e) {
        	LogHelper.logError(e);
        }
    }
}
