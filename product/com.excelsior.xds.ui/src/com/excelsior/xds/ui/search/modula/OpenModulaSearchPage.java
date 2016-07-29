package com.excelsior.xds.ui.search.modula;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;

public class OpenModulaSearchPage implements IWorkbenchWindowActionDelegate
{
    private IWorkbenchWindow fWindow;

    @Override
    public void init(IWorkbenchWindow window) {
        fWindow = window;
    }
    
    @Override
    public void dispose() {
        fWindow = null;
    }

    @Override
    public void run(IAction action) {
        if (fWindow == null || fWindow.getActivePage() == null) {
            beep();
            LogHelper.logError("Could not open the search dialog - for some reason the window handle was null"); //$NON-NLS-1$
            return;
        }
        NewSearchUI.openSearchDialog(fWindow, ModulaSearchPage.PAGE_ID);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing since the action isn't selection dependent.
    }
    
    protected void beep() {
        Shell shell= WorkbenchUtils.getWorkbenchWindowShell();
        if (shell != null && shell.getDisplay() != null)
            shell.getDisplay().beep();
    }
}
