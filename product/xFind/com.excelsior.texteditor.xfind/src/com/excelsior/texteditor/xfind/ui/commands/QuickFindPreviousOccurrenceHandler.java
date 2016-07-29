package com.excelsior.texteditor.xfind.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.excelsior.texteditor.xfind.ui.QuickXFind;
import com.excelsior.texteditor.xfind.ui.XFindPanelManager;

public class QuickFindPreviousOccurrenceHandler extends AbstractHandler 
{
    /**
     * {@inheritDoc}
     */
    @Override // IHandler
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IEditorPart part = window.getActivePage().getActiveEditor();
		if (part != null && !XFindPanelManager.isXFindPanelOpened(part)) {
            QuickXFind.findPrevious(part);
        }
        return null;
    }

}
