package com.excelsior.xds.ui.commons.utils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.IWizardDescriptor;

public abstract class WizardUtils {
    
    public static void openWizard(String wizardId, Shell parentShell) {
        openWizard(wizardId, parentShell, null);
    }
    
    public static void openWizard(String wizardId, Shell parentShell, ISelection selection) {
        // First see if this is a "new wizard".
        IWizardDescriptor descriptor = PlatformUI.getWorkbench().getNewWizardRegistry().findWizard(wizardId);
        // If not check if it is an "import wizard".
        if  (descriptor == null) {
          descriptor = PlatformUI.getWorkbench().getImportWizardRegistry().findWizard(wizardId);
        }
        // Or maybe an export wizard
        if  (descriptor == null) {
          descriptor = PlatformUI.getWorkbench().getExportWizardRegistry().findWizard(wizardId);
        }
        try  {
          // Then if we have a wizard, open it.
          if  (descriptor != null) {
            IWizard wizard = descriptor.createWizard();
            if (wizard instanceof IWorkbenchWizard) {
                IStructuredSelection structuredSelection = selection instanceof IStructuredSelection?  (IStructuredSelection)selection : new StructuredSelection();
                ((IWorkbenchWizard)wizard).init(PlatformUI.getWorkbench(), structuredSelection);
                WizardDialog wd = new WizardDialog(parentShell, wizard);
                wd.setTitle(wizard.getWindowTitle());
                wd.open();
            }
            else {
                Assert.isTrue(false, "Attempt to call not IWorkbenchWizard"); //$NON-NLS-1$
            }
          }
        } catch  (CoreException e) {
          e.printStackTrace();
        }
    }
}
