package com.excelsior.xds.ui.variables;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.variables.VariableUtils;

/**
 * Variable resolver which returns values of the project SDK properties.
 */
public class SdkVariableResolver implements IDynamicVariableResolver {

    // Note: names of variables must be synchronized with their names in plugin.xml 

    /** The absolute file system path of the project SDK root. */
    private static final String XDS_HOME_VAR = VariableUtils.XDS_HOME_VAR;  //$NON-NLS-1$
    
    @Override
    public String resolveValue(IDynamicVariable variable, String argument) throws CoreException 
    {
        IResource resource = VariableResolverUtils.getFirstSelectedResource();
        if (resource != null) {
            XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(resource.getProject());
            Sdk sdk = xdsProjectSettings.getProjectSdk();

            if (sdk != null) {
                if (XDS_HOME_VAR.equals(variable.getName())) {
                    return sdk.getSdkHomePath();
                }
            }
        }
        return null;
    }
    
}
