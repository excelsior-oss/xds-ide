package com.excelsior.xds.core.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;

import com.excelsior.xds.core.resource.ResourceUtils;

/**
 * Utility class for recognizing XDS files.  
 */
public final class XdsFileUtils {
	/**
	 * Only static method are allowed - this class should have no state
	 */
	private XdsFileUtils() {
	}
    
    public static final String XDS_PROJECT_FILE_EXTENSION     = "prj"; //$NON-NLS-1$
    public static final String XDS_REDIRECTION_FILE_EXTENSION = "red"; //$NON-NLS-1$
    public static final String XDS_SYMBOL_FILE_EXTENSION      = "sym"; //$NON-NLS-1$

    public static final String MODULA_PROGRAM_MODULE_FILE_EXTENSION    = "mod"; //$NON-NLS-1$
    public static final String MODULA_DEFINITION_MODULE_FILE_EXTENSION = "def"; //$NON-NLS-1$

    public static final String OBERON_MODULE_FILE_EXTENSION            = "ob2"; //$NON-NLS-1$
    public static final String OBERON_DEFINITION_MODULE_FILE_EXTENSION = "odf"; //$NON-NLS-1$

    public static final String XDS_DBGSCRIPT_FILE_EXTENSION        = "pkt"; //$NON-NLS-1$
    public static final String XDS_DBGSCRIPT_BUNDLE_FILE_EXTENSION = "ldp"; //$NON-NLS-1$
    public static final String XDS_DBGSCRIPT_OUTPUT_FILE_EXTENSION = "res"; //$NON-NLS-1$

    public static Set<String> MODULA_FILE_EXTENSIONS = new HashSet<String>( 
            Arrays.asList(new String[] { MODULA_PROGRAM_MODULE_FILE_EXTENSION
                                       , MODULA_DEFINITION_MODULE_FILE_EXTENSION }
            ));
    
    public static Set<String> OBERON_FILE_EXTENSIONS = new HashSet<String>( 
            Arrays.asList(new String[] { OBERON_MODULE_FILE_EXTENSION
                                       , OBERON_DEFINITION_MODULE_FILE_EXTENSION }
            ));
    
    public static Set<String> XDS_DBGSCRIPT_UNIT_FILE_EXTENSIONS = new HashSet<String>( 
            Arrays.asList(new String[] { XDS_DBGSCRIPT_FILE_EXTENSION
                                       , XDS_DBGSCRIPT_BUNDLE_FILE_EXTENSION
                                       , XDS_DBGSCRIPT_OUTPUT_FILE_EXTENSION }
            ));

    public static Set<String> XDS_DBGSCRIPT_SOURCE_FILE_EXTENSIONS = new HashSet<String>( 
            Arrays.asList(new String[] { XDS_DBGSCRIPT_FILE_EXTENSION
                                       , XDS_DBGSCRIPT_BUNDLE_FILE_EXTENSION }
            ));

    public static Set<String> COMPILATION_UNIT_FILE_EXTENSIONS = new HashSet<String>( 
            Arrays.asList(new String[] { MODULA_PROGRAM_MODULE_FILE_EXTENSION
                                       , MODULA_DEFINITION_MODULE_FILE_EXTENSION
                                       , OBERON_MODULE_FILE_EXTENSION
                                       , OBERON_DEFINITION_MODULE_FILE_EXTENSION }
            ));
    
    public static Set<String> PROGRAM_MODULE_FILE_EXTENSIONS = new HashSet<String>( 
            Arrays.asList(new String[] { MODULA_PROGRAM_MODULE_FILE_EXTENSION
                                       , OBERON_MODULE_FILE_EXTENSION }
            )); 

    public static Set<String> DEFINITION_MODULE_FILE_EXTENSIONS = new HashSet<String>( 
            Arrays.asList(new String[] { MODULA_DEFINITION_MODULE_FILE_EXTENSION
                                       , OBERON_DEFINITION_MODULE_FILE_EXTENSION }
            )); 
    
    
    public static boolean isXdsFile (String fileName) {
        return isCompilationUnitFile(fileName) || isXdsProjectFile(fileName);
    }
    
    public static boolean isModulaFile(IFile f) {
    	return isModulaFile(ResourceUtils.getAbsolutePath(f));
    }
    
    public static boolean isModulaFile (String fileName) {
        String fileExt = getLowercasedExtension(fileName);
        return MODULA_FILE_EXTENSIONS.contains(fileExt);
    }

    public static boolean isOberonFile (String fileName) {
        String fileExt = getLowercasedExtension(fileName);
        return OBERON_FILE_EXTENSIONS.contains(fileExt);
    }

    public static boolean isCompilationUnitFile (String fileName) {
        String fileExt = getLowercasedExtension(fileName);
        return COMPILATION_UNIT_FILE_EXTENSIONS.contains(fileExt);
    }
    
    public static boolean isCompilationUnitFile(IFile f) {
    	return isCompilationUnitFile(ResourceUtils.getAbsolutePath(f));
    }
    
    public static boolean isProgramModuleFile (String fileName) {
        String fileExt = getLowercasedExtension(fileName);
        return PROGRAM_MODULE_FILE_EXTENSIONS.contains(fileExt);
    }
    
    public static boolean isModulaProgramModuleFile (String fileName) {
        String fileExt = getLowercasedExtension(fileName);
        return MODULA_PROGRAM_MODULE_FILE_EXTENSION.equals(fileExt);
    }
    
    public static boolean isModulaDefinitionModuleFile (String fileName) {
        String fileExt = getLowercasedExtension(fileName);
        return MODULA_DEFINITION_MODULE_FILE_EXTENSION.equals(fileExt);
    }

    public static boolean isDefinitionModuleFile (String fileName) {
    	String fileExt = getLowercasedExtension(fileName);
    	return DEFINITION_MODULE_FILE_EXTENSIONS.contains(fileExt);
    }
    
    public static boolean isOberonModuleFile (String fileName) {
        String fileExt = getLowercasedExtension(fileName);
        return OBERON_MODULE_FILE_EXTENSION.equals(fileExt);
    }
    
    public static boolean isOberonDefinitionModuleFile (String fileName) {
        String fileExt = getLowercasedExtension(fileName);
        return OBERON_DEFINITION_MODULE_FILE_EXTENSION.equals(fileExt);
    }
    
    public static boolean isDefinitionModuleOrOberonModuleFile (String fileName) {
        return isDefinitionModuleFile(fileName) || isOberonModuleFile(fileName);
    }
    
    public static boolean isXdsProjectFile (String fileName) {
        String fileExt = getLowercasedExtension(fileName);
        return XDS_PROJECT_FILE_EXTENSION.equals(fileExt);
    }
    
    public static boolean isAnyOfDbgScriptSourceFiles (IFile f) {
    	return isAnyOfDbgScriptSourceFiles(ResourceUtils.getAbsolutePath(f));
    }

    /**
     * test for *.PKT or .LDP files
     */
    public static boolean isAnyOfDbgScriptSourceFiles (String fileName) {
        String fileExt = getLowercasedExtension(fileName);
        return XDS_DBGSCRIPT_SOURCE_FILE_EXTENSIONS.contains(fileExt);
    }
    
    /**
     * test for *.PKT files
     */
    public static boolean isDbgScriptFile (IFile f) {
       return isDbgScriptFile(ResourceUtils.getAbsolutePath(f));
    }

    /**
     * test for *.PKT files
     */
    public static boolean isDbgScriptFile (String fileName) {
        String fileExt = getLowercasedExtension(fileName);
        return XDS_DBGSCRIPT_FILE_EXTENSION.equals(fileExt);
    }
    
    /**
     * test for *.PKT files
     */
    public static boolean isDbgScriptBundleFile (IFile f) {
    	return isDbgScriptBundleFile(ResourceUtils.getAbsolutePath(f));
    }
    
    /**
     * test for *.LDP files
    */
    public static boolean isDbgScriptBundleFile (String fileName) {
        String fileExt = getLowercasedExtension(fileName);
        return XDS_DBGSCRIPT_BUNDLE_FILE_EXTENSION.equals(fileExt);
    }
    
    public static boolean isSymbolFile (String fileName) {
        String fileExt = getLowercasedExtension(fileName);
        return XDS_SYMBOL_FILE_EXTENSION.equals(fileExt);
    }

    public static String getLowercasedExtension (String fileName) {
        return StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
    }
    
    public static String getProgramModuleFileName(String moduleName) {
    	return moduleName + "." + MODULA_PROGRAM_MODULE_FILE_EXTENSION;
    }
    
    public static String getDefinitionModuleFileName(String moduleName) {
    	return moduleName + "." + MODULA_DEFINITION_MODULE_FILE_EXTENSION;
    }
}
