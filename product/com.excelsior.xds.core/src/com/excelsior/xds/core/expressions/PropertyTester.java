package com.excelsior.xds.core.expressions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.excelsior.xds.core.compiler.compset.CompilationSetManager;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.project.XdsProjectType;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.utils.AdapterUtilities;
import com.excelsior.xds.core.utils.XdsFileUtils;

public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

    private static final String IS_MODULA_FILE_PROPERTY     = "isModulaFile"; //$NON-NLS-1$
    private static final String IS_OBERON_FILE_PROPERTY     = "isOberonFile"; //$NON-NLS-1$
    private static final String IS_SYM_FILE_PROPERTY        = "isSymFile";    //$NON-NLS-1$
    private static final String IS_DBGSCRIP_FILE_PROPERTY   = "isDbgScriptFile"; //$NON-NLS-1$
    private static final String IS_DBGSCRIP_SOURCE_PROPERTY = "isDbgScriptSource"; //$NON-NLS-1$
    private static final String XDS_PROJECT_TYPE_PROPERTY   = "xdsProjectType"; //$NON-NLS-1$
    private static final String IS_IN_COMPILATION_SET_PROPERTY = "isInCompilationSet"; //$NON-NLS-1$

    public PropertyTester() {
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        IResource resource = AdapterUtilities.getAdapter(receiver, IResource.class);
        
        //TODO: replace with functional method table
        
        if (IS_MODULA_FILE_PROPERTY.equals(property)) {
            String path = ResourceUtils.getAbsolutePath(resource);
            return XdsFileUtils.isModulaFile(path);
        }
        else if (IS_OBERON_FILE_PROPERTY.equals(property)) {
            String path = ResourceUtils.getAbsolutePath(resource);
            return XdsFileUtils.isOberonFile(path);
        }
        else if (IS_SYM_FILE_PROPERTY.equals(property)) {
            String path = ResourceUtils.getAbsolutePath(resource);
            return XdsFileUtils.isSymbolFile(path);
        }
        else if (IS_DBGSCRIP_SOURCE_PROPERTY.equals(property)) {
            String path = ResourceUtils.getAbsolutePath(resource);
            return XdsFileUtils.isAnyOfDbgScriptSourceFiles(path);
        }
        else if (IS_DBGSCRIP_FILE_PROPERTY.equals(property)) {
            String path = ResourceUtils.getAbsolutePath(resource);
            return XdsFileUtils.isDbgScriptFile(path);
        }
        else if (XDS_PROJECT_TYPE_PROPERTY.equals(property)) {
        	IProject project = resource.getProject();
        	if (project != null) {
        		XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(project);
        		try{
        			XdsProjectType expectedXdsProjectType = XdsProjectType.valueOf(expectedValue.toString());
        			XdsProjectType xdsProjectType = xdsProjectSettings.getProjectType();
        			return expectedXdsProjectType.equals(xdsProjectType);
        		}
        		catch (IllegalArgumentException e){
        			return false;
        		}
        	}
        }
        else if (IS_IN_COMPILATION_SET_PROPERTY.equals(property)) {
        	IFile ifile =  AdapterUtilities.getAdapter(receiver, IFile.class);
        	if (ifile != null) {
        		return CompilationSetManager.getInstance().isInCompilationSet(ifile);
        	}
        }
        
        return false;
    }
}
