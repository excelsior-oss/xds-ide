package com.excelsior.xds.xbookmarks.commands;

import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.xbookmarks.XBookmarksUtils;
import com.excelsior.xds.xbookmarks.internal.nls.Messages;

/**
 * Command handler to remove all xBookmarks in Workspace.
 */
public class RemoveAllXBookmarksHandler extends XBookmarkHandler {

    /*
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute (ExecutionEvent event) throws ExecutionException {
        IResource scope = XBookmarksUtils.getWorkspaceScope();
        if (scope == null) {
            return null;
        }
        
        for (int markerNumber = 0; markerNumber <= 9; ++markerNumber) {
            IMarker marker = XBookmarksUtils.fetchBookmark(scope, markerNumber);
            if (marker != null) {
                try {
                    marker.delete();
                }
                catch (CoreException e) {
                    XBookmarksUtils.logError(e);
                    statusLine.showErrMessage(MessageFormat.format(Messages.BookmarkAction_CantDeleteBM, markerNumber));
                    break;
                }
            }
        }
        XBookmarksState xBbookmarkStateService = (XBookmarksState)WorkbenchUtils.getSourceProvider(XBookmarksState.EXIST_STATE);
        if (xBbookmarkStateService != null) {
        	xBbookmarkStateService.fireAllBookmarkRemoved();
        }
        return null;
    }
    
}
