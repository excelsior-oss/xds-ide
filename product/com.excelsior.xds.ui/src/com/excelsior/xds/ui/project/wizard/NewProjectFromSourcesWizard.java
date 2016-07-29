package com.excelsior.xds.ui.project.wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.project.NewProjectCreator;
import com.excelsior.xds.ui.commons.perspectives.PerspectiveUtils;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.internal.nls.Messages;
import com.excelsior.xds.ui.perspectives.XdsPerspectiveFactory;

/**
 * Wizard for the Modula-2 project creation from existing sources. 
 */
public class NewProjectFromSourcesWizard extends    BasicNewResourceWizard 
                                         implements INewWizard 
{
    public static final String ID = "com.excelsior.xds.ui.project.NewProjectFromSourcesWizard"; //$NON-NLS-1$
	NewProjectFromSourcesPage firstPage;
	
	public NewProjectFromSourcesWizard() {
		setWindowTitle(Messages.NewProjectWizard_Title);
		setHelpAvailable(false);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(ImageUtils.getImage(ImageUtils.XDS_GREEN_IMAGE_74X66)));
	}

	@Override
	public boolean performFinish() {
		IProject newProject = NewProjectCreator.createFromSources(firstPage.getSettings());
		if (newProject == null) {
			return false;
		}
		selectAndReveal(newProject);
		
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
		firstPage = new NewProjectFromSourcesPage();
		addPage(firstPage);
	}
}
