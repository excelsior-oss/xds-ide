package com.excelsior.xds.core.templates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;

/**
 * Methods for getting template file contents and creating new sources files from templates
 * @author lsa80
 */
public class SourceFileCreator {
	private static final String RESOURCE_PLUGIN_FOLDER_LOCATION = "resources/newfiles/"; //$NON-NLS-1$

	private SourceFileCreator() {}
	
	public static File createSourceFile(IProject project, SourceFileTemplate templateType, String moduleName, String relativeProjectPath) throws IOException, CoreException {
	    return createSourceFile(project, templateType, moduleName, relativeProjectPath, new HashMap<String, String>());
    }

    public static File createSourceFile(IProject project, SourceFileTemplate templateType, String moduleName, 
                       String relativeProjectPath, Map<String, String> vars) throws IOException, CoreException {

        vars.put("eclipse.ide.modulename", moduleName); // - used for main module generation  //$NON-NLS-1$ 
        
        XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(project);
        Sdk sdk = xdsProjectSettings.getProjectSdk();
        String template = getTemplateContent(sdk, templateType, vars);
        
        IContainer c = (IContainer)project.getParent().findMember(new Path(relativeProjectPath));
        File       f = new File(FilenameUtils.concat(ResourceUtils.getAbsolutePath(c), moduleName + templateType.extension));
        FileWriter w = new FileWriter(f);
        try{
            w.write(template);
        }
        finally{
            w.close();
        }
        return f;
    }

	public static String getTemplateContent(Sdk sdk ,
			SourceFileTemplate templateType, Map<String, String> vars)
			throws FileNotFoundException, IOException {
    	
    	InputStream resourceStream = null; 
    	try{
    		if (sdk != null) {
        		String path =  sdk.getPropertyValue(templateType.sdkProperty);
        		if (StringUtils.isNotBlank(path) && new File(path).exists()) {
        			resourceStream = new FileInputStream(path);
        		}
        	}
        	
        	if (resourceStream == null) {
        		String templatePath = RESOURCE_PLUGIN_FOLDER_LOCATION + templateType.fileName;  //$NON-NLS-1$ 
                resourceStream = ResourceUtils.getPluginResourceAsStream(ResourceUtils.getXdsResourcesPluginBundle(), templatePath);
        	}
        	
            String template = IOUtils.toString(resourceStream);
            StrSubstitutor sub = new StrSubstitutor(vars);
            template = sub.replace(template);
    		return template;
    	}
    	finally{
    		if (resourceStream != null) {
    			resourceStream.close();
    		}
    	}
	}
}
