package com.excelsior.xds.ui.variables;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

import com.excelsior.xds.core.resource.ResourceUtils;

/**
 * Variable resolver which returns values location, name and extension of 
 * the selected file.
 */
public class SelectedFileVariableResolver implements IDynamicVariableResolver {

    // Note: Names of variables must be synchronized with their names in plugin.xml 

    /** The absolute file system path of the selected file */
    public static final String SELECTED_FILE_LOC = "xds_selected_file_loc";  //$NON-NLS-1$

    /** The absolute file system path of the parent directory of the selected file */
    public static final String SELECTED_FILE_BASELOC = "xds_selected_file_baseloc";  //$NON-NLS-1$

    /** The name of the selected file */
    public static final String SELECTED_FILE_NAME = "xds_selected_file_name";  //$NON-NLS-1$
    
    /** The name without path and extension of  the selected file */
    public static final String SELECTED_FILE_BASENAME = "xds_selected_file_basename";  //$NON-NLS-1$
    
    /** The file name extension of the selected file */
    public static final String SELECTED_FILE_EXT = "xds_selected_file_ext";  //$NON-NLS-1$
    
    
    @Override
    public String resolveValue(IDynamicVariable variable, String argument) throws CoreException 
    {
        IFile file = VariableResolverUtils.getSelectedFile();
        if (file != null) {
            String var_name = variable.getName();
            if (SELECTED_FILE_LOC.equals(var_name)) {
                return ResourceUtils.getAbsolutePath(file);
            } 
            if (SELECTED_FILE_BASELOC.equals(var_name)) {
                return ResourceUtils.getAbsolutePath(file.getParent());
            } 
            if (SELECTED_FILE_NAME.equals(var_name)) {
                return file.getName();
            } 
            if (SELECTED_FILE_BASENAME.equals(var_name)) {
                return FilenameUtils.getBaseName(file.getName());
            } 
            if (SELECTED_FILE_EXT.equals(var_name)) {
                return FilenameUtils.getExtension(file.getName());
            }
        }
        return null;
    }

}
