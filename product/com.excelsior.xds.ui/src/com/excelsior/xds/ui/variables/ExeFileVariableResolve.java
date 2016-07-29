package com.excelsior.xds.ui.variables;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

import com.excelsior.xds.core.resource.ResourceUtils;

/**
 * Variable resolver which returns location, name and extension of
 * the user program executable file of the selected project.
 */
public class ExeFileVariableResolve implements IDynamicVariableResolver {

    // Note: Names of variables must be synchronized with their names in plugin.xml 

    /** The absolute file system path of the user program executable file of the selected project */
    public static final String EXEFILE_LOC = "xds_exefile_loc";  //$NON-NLS-1$

    /** The absolute file system path of the parent directory of the user program executable file of the selected project */
    public static final String EXEFILE_BASELOC = "xds_exefile_baseloc";  //$NON-NLS-1$

    /** The name of the user program executable file of the selected project */
    public static final String EXEFILE_NAME = "xds_exefile_name";  //$NON-NLS-1$
    
    /** The name without path and extension of  the user program executable file of the selected project */
    public static final String EXEFILE_BASENAME = "xds_exefile_basename";  //$NON-NLS-1$

    /** The file name extension of the user program executable file of the selected project */
    public static final String EXEFILE_EXT = "xds_exefile_ext";  //$NON-NLS-1$
    
    
    @Override
    public String resolveValue(IDynamicVariable variable, String argument) throws CoreException 
    {
        IFile file = VariableResolverUtils.getApplicationExecutable();
        if (file != null) {
            String var_name = variable.getName();
            if (EXEFILE_LOC.equals(var_name)) {
                return ResourceUtils.getAbsolutePath(file);
            }
            if (EXEFILE_BASELOC.equals(var_name)) {
                return ResourceUtils.getAbsolutePath(file.getParent());
            }
            if (EXEFILE_NAME.equals(var_name)) {
                return file.getName();
            } 
            if (EXEFILE_BASENAME.equals(var_name)) {
                return FilenameUtils.getBaseName(file.getName());
            } 
            if (EXEFILE_EXT.equals(var_name)) {
                return FilenameUtils.getExtension(file.getName());
            }
        }
        return null;
    }

}
