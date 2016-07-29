package com.excelsior.xds.ui.commons.utils;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;

public abstract class DialogUtils {
    public static IFolder chooseProjectFolder(Shell shell, IProject container) {
        FilteredResourcesSelectionDialog dlg = new FilteredResourcesSelectionDialog(shell, false, container, IResource.FOLDER & (~IResource.HIDDEN));
        dlg.setInitialPattern("**"); //$NON-NLS-1$
        if (dlg.open() == FilteredResourcesSelectionDialog.OK ) {
            return (IFolder)(dlg.getResult()[0]);
        }
        
        return null;
    }
    
    public static boolean openTwoChoiceDialog(Shell shell, String title, String message, String[] alternatives) {
        MessageDialog dialog = new TwoChoiceDialog(shell, title, null, message,
                MessageDialog.QUESTION, alternatives, 0);
        return dialog.open() == 0;
    }
    
    static class TwoChoiceDialog extends MessageDialog {
        public TwoChoiceDialog(Shell parentShell, String dialogTitle,
                Image dialogTitleImage, String dialogMessage,
                int dialogImageType, String[] dialogButtonLabels,
                int defaultIndex) {
            super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
                    dialogImageType, dialogButtonLabels, defaultIndex);
            int style = SWT.NONE;
            style &= SWT.SHEET;
            setShellStyle(getShellStyle() | style);
        }
    }
}