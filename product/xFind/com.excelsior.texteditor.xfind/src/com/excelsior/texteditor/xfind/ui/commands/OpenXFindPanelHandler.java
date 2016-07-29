package com.excelsior.texteditor.xfind.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

import com.excelsior.texteditor.xfind.internal.LogHelper;
import com.excelsior.texteditor.xfind.ui.XFindPanel;
import com.excelsior.texteditor.xfind.ui.XFindPanelManager;

public class OpenXFindPanelHandler extends AbstractHandler
{
    /**
     * {@inheritDoc}
     */
    @Override // IHandler
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IEditorPart part = window.getActivePage().getActiveEditor();

        XFindPanel panel = XFindPanelManager.getXFindPanel(part, true);
        if (panel != null) {
            panel.showPanel();
        }
        else {
            // failed to create XFindPanel, execute standard command "Find and Replace".   
            IHandlerService handlerService = (IHandlerService)window.getService(IHandlerService.class);
            try {
                handlerService.executeCommand(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE, null);
            } catch (Exception ex) {
                LogHelper.logError("Command " + IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE + " not found");   //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        return null;
    }

}
