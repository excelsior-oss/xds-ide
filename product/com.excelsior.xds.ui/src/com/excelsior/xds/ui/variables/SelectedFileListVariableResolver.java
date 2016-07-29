package com.excelsior.xds.ui.variables;

import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

import com.excelsior.xds.core.resource.ResourceUtils;

/**
 * Variable resolver which returns the list of locations and names of 
 * the selected file.
 */
public class SelectedFileListVariableResolver  implements IDynamicVariableResolver {

    // Note: Names of variables must be synchronized with their names in plugin.xml 
    
    /** The list of the absolute file system paths of the selected files */
    public static final String LIST_SELECTED_FILE_LOC = "xds_list_selected_file_loc";  //$NON-NLS-1$
    
    /** The list of names of the selected files */
    public static final String LIST_SELECTED_FILE_NAME = "xds_list_selected_file_name";  //$NON-NLS-1$

    /** The list of names name without path and extension of the selected files */
    public static final String LIST_SELECTED_FILE_BASENAME = "xds_list_selected_file_basename";  //$NON-NLS-1$

    /** Default separator of list items */
    public static final String DEFAULT_SEPARATOR = " ";  //$NON-NLS-1$

    @Override
    public String resolveValue(IDynamicVariable variable, String argument) throws CoreException 
    {
        List<IResource> resources = VariableResolverUtils.getSelectedResources();
        if (resources != null) {
            StringBuilder sb = new StringBuilder();
            String var_name = variable.getName();
            if (StringUtils.isBlank(argument)) {
                argument = DEFAULT_SEPARATOR;
            }

            if (LIST_SELECTED_FILE_LOC.equals(var_name)) {
                for (IResource r : resources) {
                    sb.append(ResourceUtils.getAbsolutePath(r));
                    sb.append(argument);
                }
                return sb.toString();
            } 
            if (LIST_SELECTED_FILE_NAME.equals(var_name)) {
                for (IResource r : resources) {
                    sb.append(r.getName());
                    sb.append(argument);
                }
                return sb.toString();
            } 
            if (LIST_SELECTED_FILE_BASENAME.equals(var_name)) {
                for (IResource r : resources) {
                    sb.append(FilenameUtils.getBaseName(r.getName()));
                    sb.append(argument);
                }
                return sb.toString();
            } 
        }
        return null;
    }

}
