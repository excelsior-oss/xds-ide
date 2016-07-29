package com.excelsior.xds.core.variables;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;

import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.sdk.Sdk;

/**
 * String variable utilities. 
 */
public final class VariableUtils {
	public static final String XDS_HOME_VAR = "xds_home";  //$NON-NLS-1$
	
    private VariableUtils() {
	}

	public static String performStringSubstitution(String line) throws CoreException {
		return performStringSubstitution(line, true);
	}
	
	public static String performStringSubstitution(String line, boolean isReportUndefinedVariables) throws CoreException {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		return manager.performStringSubstitution(line, isReportUndefinedVariables);
	}
	
	public static String performStringSubstitution(Sdk context, String line) throws CoreException {
        return performStringSubstitution(context, line, true);
    }
	
	public static String performStringSubstitution(Sdk context, String line, boolean isReportUndefinedVariables) throws CoreException {
        IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
        if (context != null) {
			line = line.replaceAll(String.format(Pattern.quote("${%s}"),
					XDS_HOME_VAR), StringUtils.replaceChars(
					context.getSdkHomePath(), '\\', '/'));
        }
        line = manager.performStringSubstitution(line, isReportUndefinedVariables);
        return line;
    }
	
    public static String performStringSubstitution(IProject context, String line) throws CoreException {
    	XdsProjectSettings settings = XdsProjectSettingsManager.getXdsProjectSettings(context);
    	return performStringSubstitution(settings.getProjectSdk(), line);
    }
}
