package com.excelsior.xds.ui.module.wizard;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

public class NewOb2ModuleWizard extends BasicNewResourceWizard implements INewWizard {
    public static final String ID = "com.excelsior.xds.ui.project.NewOb2ModuleWizard"; //$NON-NLS-1$
    
    private static final String CRLF = System.getProperty("line.separator"); //$NON-NLS-1$

    
    private NewOb2ModulePage firstPage;
    
    public NewOb2ModuleWizard() {
        super();
        setWindowTitle(Messages.NewOb2ModuleWizard_Title);
    }

    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        selection = currentSelection;
        setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(ImageUtils.getImage(ImageUtils.XDS_GREEN_IMAGE_74X66)));
    }
    
    @Override
    public boolean performFinish() {
        try {
            IProject project = firstPage.getProject();
            
            Map<String, String> vars = new HashMap<String, String>();
            if (firstPage.isMainModule()) {
                vars.put("eclipse.ide.oberonmainpragma", "<* +MAIN *>" + CRLF + CRLF); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                vars.put("eclipse.ide.oberonmainpragma", ""); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
          	File fOb2 = SourceFileCreator.createSourceFile(project, SourceFileTemplate.OBERON, firstPage.getModuleName(), 
          	                                               firstPage.getSourceFolder(), vars);
            
            ProjectUtils.refreshLocalSync(project);

            if (fOb2 != null) {
                openInEditor(project, fOb2);
            }
        } catch (IOException e) {
            LogHelper.logError(e);
            SWTFactory.OkMessageBox(null, Messages.NewOb2ModuleWizard_CreateNewModuleError, e.getMessage());
        } catch (CoreException e) {
            LogHelper.logError(e);
            SWTFactory.OkMessageBox(null, Messages.NewOb2ModuleWizard_CreateNewModuleError, e.getMessage());
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
        firstPage = new NewOb2ModulePage();
        addPage(firstPage);
    }
}
