package com.excelsior.xds.ui.variables;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

import com.excelsior.xds.core.resource.ResourceUtils;

/**
 * Variable resolver which returns location, name and extension of 
 * the XDS project file (*.prj) of selected project.
 */
public class MainModuleVariableResolver implements IDynamicVariableResolver {

    // Note: Names of variables must be synchronized with their names in plugin.xml 

    /** The absolute file system path of the main module of the selected project */
    public static final String MAINMODULE_LOC = "xds_mainmodule_loc";  //$NON-NLS-1$
    
    /** The absolute file system path of the parent directory of the main module of the selected project */
    public static final String MAINMODULE_BASELOC = "xds_mainmodule_baseloc";  //$NON-NLS-1$

    /** The name of the main module of the selected project */
    public static final String MAINMODULE_NAME = "xds_mainmodule_name";  //$NON-NLS-1$

    /** The name without path and extension of  the main module of the selected project */
    public static final String MAINMODULE_BASENAME = "xds_mainmodule_basename";  //$NON-NLS-1$

    /** The file name extension of the main module of the selected project */
    public static final String MAINMODULE_EXT = "xds_mainmodule_ext";  //$NON-NLS-1$

    
    @Override
    public String resolveValue(IDynamicVariable variable, String argument) throws CoreException 
    {
        IFile file = VariableResolverUtils.getMainModule();
        if (file != null) {
            String var_name = variable.getName();
            if (MAINMODULE_LOC.equals(var_name)) {
                return ResourceUtils.getAbsolutePath(file);
            } 
            if (MAINMODULE_BASELOC.equals(var_name)) {
                return ResourceUtils.getAbsolutePath(file.getParent());
            } 
            if (MAINMODULE_NAME.equals(var_name)) {
                return file.getName();
            } 
            if (MAINMODULE_BASENAME.equals(var_name)) {
                return FilenameUtils.getBaseName(file.getName());
            } 
            if (MAINMODULE_EXT.equals(var_name)) {
                return FilenameUtils.getExtension(file.getName());
            }
        }
        return null;
    }

}
