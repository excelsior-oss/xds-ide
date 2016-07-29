package com.excelsior.xds.xbookmarks.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.xbookmarks.XBookmarksDialog;
import com.excelsior.xds.xbookmarks.XBookmarksUtils;
import com.excelsior.xds.xbookmarks.internal.nls.Messages;

/**
 * Command handler to show all xBookmarks in Workspace and toggle one of them on the current line.
 */
public class ShowToggleDialogHandler extends XBookmarksDialogHandler {
    
    /*
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute (ExecutionEvent event) throws ExecutionException {
        final XBookmarksDialog.Model model = new XBookmarksDialog.Model(3);
        for (int i = 0; i <= 9; ++i) {
            IMarker marker = XBookmarksUtils.fetchBookmark(XBookmarksUtils.getWorkspaceScope(), i);
            XBookmarksDialog.Model.Row row = getDescription(marker);
            if (row == null) {
               row = new XBookmarksDialog.Model.Row(new String[]{""+i+".", "", ""}).setData(i); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
            model.addRow(row);
        }
        
        Listener lst = new Listener() {
            @Override
            public void handleEvent(Event event) {
                XBookmarksDialog.Model.Row r = (XBookmarksDialog.Model.Row)event.data;
                if (r.getData() >= 0) {
                    toggleBookmark(r.getData());
                }
            }
        };

        final XBookmarksDialog md = new XBookmarksDialog(model,
                lst,
                WorkbenchUtils.getActivePartShell(),
                false, 
                false,
                true, 
                false,
                Messages.BookmarkAction_ToggleBM, 
                Messages.BookmarkAction_PressShiftNumToToggle); 
        md.open();
        md.addListener (SWT.KeyDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                final int VK_0 = 48;
                int num = event.keyCode - VK_0;
                if (event.stateMask == (SWT.CTRL|SWT.SHIFT) && num >=0 && num <= 9) {
                    md.close();
                    toggleBookmark(num);
                } else if (event.character == SWT.DEL) {
                    XBookmarksDialog.Model.Row row = md.getSelection();
                    if (row != null) {
                        IResource scope = XBookmarksUtils.getWorkspaceScope();
                        if (scope != null) {
                            IMarker marker = XBookmarksUtils.fetchBookmark(scope, row.getData());
                            if (marker != null) {
                                try {
                                    marker.delete();
                                    row.setField(1, ""); //$NON-NLS-1$
                                    row.setField(2, ""); //$NON-NLS-1$
                                    md.update();
                                    XBookmarksState xBbookmarkStateService = (XBookmarksState)WorkbenchUtils.getSourceProvider(XBookmarksState.EXIST_STATE);
                                    if (xBbookmarkStateService != null) {
                                    	xBbookmarkStateService.fireBookmarkRemoved(1);
                                    }
                                }
                                catch (CoreException e) {}
                            }
                        }
                        
                    }
                    
                }
            }
            
        });
        return null;
    }

}
