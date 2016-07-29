package com.excelsior.xds.core.compiler.driver;

import java.util.List;

import org.eclipse.core.resources.IFile;

import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectType;
import com.excelsior.xds.core.resource.ResourceUtils;

/*
 * Either main module or XDS prj file
 */
public class CompilationTarget{
    private XdsProjectType xdsProjectType;
    private IFile compilationTargetFile;
    
    public CompilationTarget(XdsProjectSettings xdsProjectSettings) {
        this.xdsProjectType = xdsProjectSettings.getProjectType();
        switch (xdsProjectType) {
        case MAIN_MODULE:
            this.compilationTargetFile = ProjectUtils.getMainModuleFile(xdsProjectSettings);
            break;
        case PROJECT_FILE: 
            this.compilationTargetFile = ProjectUtils.getPrjFile(xdsProjectSettings);
            break;
        default:
            throw new IllegalArgumentException("Unexpected xdsProjectType"); //$NON-NLS-1$
        }
    }
    
    public boolean isValid(){
        return compilationTargetFile != null;
    }
    
    public List<String> getCompilationSetFiles(CompileDriver compileDriver){
        switch (xdsProjectType) {
        case MAIN_MODULE:
            return compileDriver.getModuleList(ResourceUtils.getAbsolutePath(compilationTargetFile));
        case PROJECT_FILE:
            List<String> compilationSetList = compileDriver.getProjectModuleList(ResourceUtils.getAbsolutePath(compilationTargetFile));
            compilationSetList.add(ResourceUtils.getAbsolutePath(compilationTargetFile));
            return compilationSetList;
        default:
            throw new IllegalArgumentException("Unexpected xdsProjectType"); //$NON-NLS-1$
        }
    }
    
    public XdsProjectType getXdsProjectType() {
        return xdsProjectType;
    }

    public IFile getCompilationTargetFile() {
        return compilationTargetFile;
    }
}
