package com.excelsior.xds.ui.project.wizard;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.project.NewProjectCreator;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.ui.commons.perspectives.PerspectiveUtils;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.internal.nls.Messages;
import com.excelsior.xds.ui.perspectives.XdsPerspectiveFactory;

/**
 * Wizard for the Modula-2 project creation from scratch. 
 */
public class NewProjectFromScratchWizard extends    BasicNewResourceWizard 
                                         implements INewWizard 
{
	public static final String ID = "com.excelsior.xds.ui.project.NewProjectFromScratchWizard"; //$NON-NLS-1$
	private NewProjectFromScratchPage firstPage;
	
	public NewProjectFromScratchWizard() {
		setWindowTitle(Messages.NewProjectWizard_Title);
		setHelpAvailable(false);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(ImageUtils.getImage(ImageUtils.XDS_GREEN_IMAGE_74X66)));
	}
	
	public NewProjectFromScratchPage getFromScratchPage() {
		return firstPage;
	}
	
	@Override
	public boolean performFinish() {
	    NewProjectCreator npc = new NewProjectCreator();
		final IProject newProject = npc.createFromScratch(firstPage.getSettings(), ResourceUtils.getXdsResourcesPluginBundle(), DialogInteraction.INSTANCE);
		if (newProject == null) {
			return false;
		}
		selectAndReveal(newProject);

		String mainModule = npc.getMainModule();
		if (mainModule != null && new File(mainModule).isFile()) {
	        // Open created main module in editor:
	        final String resoureRelativePath = ResourceUtils.getRelativePath(newProject, ResourceUtils.getAbsolutePathAsInFS(mainModule));
	        if (resoureRelativePath != null) {
	            Display.getDefault().asyncExec(new Runnable() {
	                @Override
	                public void run() {
	                    IFile file = newProject.getFile(resoureRelativePath);
	                    IEditorInput editorInput = new FileEditorInput(file);
	                    try {
	                    	CoreEditorUtils.openInEditor(editorInput, true);
	                    } catch (CoreException e) {
	                    	LogHelper.logError(e);
	                    }
	                }
	            });
	        }

		    
		}
		
		try {
            PerspectiveUtils.promptAndOpenPerspective(getWorkbench(), XdsPerspectiveFactory.DEVELOPMENT_PERSPECTIVE_ID, Messages.NewProjectWizard_PerspectiveSwitch, Messages.NewProjectWizard_DoYouWantToSwitsh);
        } catch (WorkbenchException e) {
            LogHelper.logError(e);
        }
		
		return true;
	}

	@Override
	public void addPages() {
		super.addPages();
	    firstPage = new NewProjectFromScratchPage();
		addPage(firstPage);
	}
}
