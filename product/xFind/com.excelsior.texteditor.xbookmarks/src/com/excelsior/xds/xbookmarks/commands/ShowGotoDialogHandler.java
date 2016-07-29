package com.excelsior.xds.xbookmarks.commands;

import java.util.Comparator;
import java.util.HashSet;

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
import com.excelsior.xds.xbookmarks.XBookmarksPlugin;
import com.excelsior.xds.xbookmarks.XBookmarksUtils;
import com.excelsior.xds.xbookmarks.internal.nls.Messages;

/**
 * Command handler to show all xBookmarks in Workspace and jump to one of them
 */
public class ShowGotoDialogHandler extends XBookmarksDialogHandler {

    /*
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute (ExecutionEvent event) throws ExecutionException {
        XBookmarksDialog.Model model = new XBookmarksDialog.Model(3);
        final HashSet<Integer> existentNums = new HashSet<Integer>(); 
        try {
            IMarker[] rawMarkers = XBookmarksUtils.getWorkspaceScope().findMarkers(XBookmarksPlugin.BOOKMARK_MARKER_ID, true, IResource.DEPTH_INFINITE);
            for (int i = 0; i < rawMarkers.length; i++) {
                IMarker marker = rawMarkers[i];
                XBookmarksDialog.Model.Row row = getDescription(marker);
                if (row != null) {
                    model.addRow(row);
                    existentNums.add(row.getData());
                }
            }
            model.SortRows(new Comparator<XBookmarksDialog.Model.Row>() {
                public int compare(XBookmarksDialog.Model.Row r0, XBookmarksDialog.Model.Row r1) {
                    return (r0.getData() - r1.getData());
                    
                }
            });
        } catch (CoreException e) {}
        if (model.getRows().size() == 0) {
            XBookmarksDialog.Model.Row row = new XBookmarksDialog.Model.Row(new String[]{"", "", Messages.BookmarkAction_NoBM}).setEnabled(false).setData(-1); //$NON-NLS-1$ //$NON-NLS-2$
            model.addRow(row);
        }
        
        Listener lst = new Listener() {
            @Override
            public void handleEvent(Event event) {
                XBookmarksDialog.Model.Row r = (XBookmarksDialog.Model.Row)event.data;
                if (r.getData() >= 0) {
                    gotoBookmark(r.getData());
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
                Messages.BookmarkAction_GotoBM, 
                Messages.BookmarkAction_PressCtrlNumToGo); 
        md.open();
        md.addListener (SWT.KeyDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                char ch = event.character;
                if (event.stateMask == SWT.CTRL && existentNums.contains(ch - '0')) {
                    md.close();
                    gotoBookmark(ch-'0');
                }
            }
            
        });
        return null;
    }

}
