package com.excelsior.xds.ui.module.wizard;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.templates.SourceFileCreator;
import com.excelsior.xds.core.templates.SourceFileTemplate;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.internal.nls.Messages;

public class NewModuleWizard extends BasicNewResourceWizard implements INewWizard {
    public static final String ID = "com.excelsior.xds.ui.project.NewModuleWizard"; //$NON-NLS-1$
    
    private NewModulePage firstPage;
    
    public NewModuleWizard() {
        super();
        setWindowTitle(Messages.NewModuleWizard_Title);
    }

    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        selection = currentSelection;
        setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(ImageUtils.getImage(ImageUtils.XDS_GREEN_IMAGE_74X66)));
    }
    
    @Override
    public boolean performFinish() {
        try {
            IProject project = firstPage.getProject();
            
            File fMod = null, fDef=null;
            
            if (firstPage.isMainModule()) {
            	fMod = SourceFileCreator.createSourceFile(project, SourceFileTemplate.MAIN_MODULE, firstPage.getModuleName(), firstPage.getImplementationModuleSourceFolder());
            } else {
                if (firstPage.isUseDefinitionModule()) {
                	fMod = SourceFileCreator.createSourceFile(project, SourceFileTemplate.DEFITION, firstPage.getModuleName(), firstPage.getDefinitionModuleSourceFolder());
                }
                
                if (firstPage.isUseImplementationModule()) {
                	fDef = SourceFileCreator.createSourceFile(project, SourceFileTemplate.IMPLEMENTATION, firstPage.getModuleName(), firstPage.getImplementationModuleSourceFolder());
                }
            }
            
            ProjectUtils.refreshLocalSync(project);

            if (fDef != null) {
                openInEditor(project, fDef);
            }
            if (fMod != null) {
                openInEditor(project, fMod);
            }
        } catch (IOException e) {
            LogHelper.logError(e);
            SWTFactory.OkMessageBox(null, Messages.NewModuleWizard_CreateNewModuleError, e.getMessage());
        } catch (CoreException e) {
            LogHelper.logError(e);
            SWTFactory.OkMessageBox(null, Messages.NewModuleWizard_CreateNewModuleError, e.getMessage());
        }
        
        return true;
    }
    
    private void openInEditor(final IProject project, File f) {
		if (f.isFile()) {
	        // Open created main module in editor:
	        final String resoureRelativePath = ResourceUtils.getRelativePath(project, f.getAbsolutePath());
	        if (resoureRelativePath != null) {
	            Display.getDefault().asyncExec(new Runnable() {
	                @Override
	                public void run() {
	                    IFile file = project.getFile(resoureRelativePath);
	                    IEditorInput editorInput = new FileEditorInput(file);
	                    try {
	                    	CoreEditorUtils.openInEditor(editorInput, true);
	                    } catch (CoreException e) {
	                    }
	                }
	            });
	        }
	    }
    }
    
    @Override
    public void addPages() {
        super.addPages();
        firstPage = new NewModulePage();
        addPage(firstPage);
    }
}
