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
public class PrjFileVariableResolver implements IDynamicVariableResolver {
    
    // Note: Names of variables must be synchronized with their names in plugin.xml 

    /** The absolute file system path of the XDS project file (*.prj) of selected project */
    public static final String PRJFILE_LOC = "xds_prjfile_loc";  //$NON-NLS-1$
    
    /** The absolute file system path of the parent directory of the selected XDS project file (*.prj) */
    public static final String PRJFILE_BASELOC = "xds_prjfile_baseloc";  //$NON-NLS-1$
    
    /** The name of the XDS project file (*.prj) of the selected project */
    public static final String PRJFILE_NAME = "xds_prjfile_name";  //$NON-NLS-1$

    /** The name without path and extension of the XDS project file (*.prj) of the selected project */
    public static final String PRJFILE_BASENAME = "xds_prjfile_basename";  //$NON-NLS-1$
    
    /** The file name extension of the XDS project file (*.prj) of the selected project. */
    public static final String PRJFILE_EXT = "xds_prjfile_ext";  //$NON-NLS-1$

    
    @Override
    public String resolveValue(IDynamicVariable variable, String argument) throws CoreException 
    {
        IFile file = VariableResolverUtils.getPrjFile();
        if (file != null) {
            String var_name = variable.getName();
            if (PRJFILE_LOC.equals(var_name)) {
                return ResourceUtils.getAbsolutePath(file);
            } 
            if (PRJFILE_BASELOC.equals(var_name)) {
                return ResourceUtils.getAbsolutePath(file.getParent());
            } 
            if (PRJFILE_NAME.equals(var_name)) {
                return file.getName();
            } 
            if (PRJFILE_BASENAME.equals(var_name)) {
                return FilenameUtils.getBaseName(file.getName());
            } 
            if (PRJFILE_EXT.equals(var_name)) {
                return FilenameUtils.getExtension(file.getName());
            }
        }
        return null;
    }

}
