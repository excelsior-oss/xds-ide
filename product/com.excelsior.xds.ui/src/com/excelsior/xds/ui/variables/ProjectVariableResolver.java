package com.excelsior.xds.ui.variables;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

import com.excelsior.xds.core.resource.ResourceUtils;

/**
 * Variable resolver which returns location and name of the selected project.
 */
public class ProjectVariableResolver implements IDynamicVariableResolver {

    // Note: names of variables must be synchronized with their names in plugin.xml 

    /** The absolute file system path of the selected project */
    public static final String PROJECT_LOC = "xds_project_loc";  //$NON-NLS-1$

    /** The absolute file system path of the parent directory of the selected project */
    public static final String PROJECT_BASELOC = "xds_project_baseloc";  //$NON-NLS-1$

    /** The name of the selected project */
    public static final String PROJECT_NAME = "xds_project_name";  //$NON-NLS-1$

    
    @Override
    public String resolveValue(IDynamicVariable variable, String argument) throws CoreException 
    {
        IResource resource = VariableResolverUtils.getFirstSelectedResource();
        if (resource != null) {
            IProject project = resource.getProject();
            String var_name = variable.getName();

            if (PROJECT_LOC.equals(var_name)) {
                return ResourceUtils.getAbsolutePath(project);
            } 
            if (PROJECT_BASELOC.equals(var_name)) {
                return ResourceUtils.getAbsolutePath(project.getParent());
            } 
            if (PROJECT_NAME.equals(var_name)) {
                return project.getName();
            }
        }
        return null;
    }

}
