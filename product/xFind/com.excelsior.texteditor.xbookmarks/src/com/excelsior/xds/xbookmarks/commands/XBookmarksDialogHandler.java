package com.excelsior.xds.xbookmarks.commands;

import java.text.MessageFormat;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.excelsior.xds.xbookmarks.XBookmarksDialog;
import com.excelsior.xds.xbookmarks.XBookmarksPlugin;
import com.excelsior.xds.xbookmarks.XBookmarksUtils;
import com.excelsior.xds.xbookmarks.internal.nls.Messages;


/**
 * Abstract command handler to process bookmarks dialogs commands. 
 */
public abstract class XBookmarksDialogHandler extends XBookmarkHandler {

    
    /**
     * Removes all bookmarks
     */
    protected void removeAllBookmarks() {
        IResource scope = XBookmarksUtils.getWorkspaceScope();
        if (scope == null) {
            return;
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
    }
    
    
    protected XBookmarksDialog.Model.Row getDescription (IMarker marker) {
        if (marker!= null && marker.exists()) {
            int markerNumber = marker.getAttribute(XBookmarksPlugin.BOOKMARK_NUMBER_ATTR, -1);
            if (markerNumber >=0 && markerNumber <= 9) {
                int    line       = MarkerUtilities.getLineNumber(marker);
                String resName    = marker.getResource().getName();
                
                String markerName = MarkerUtilities.getMessage(marker);
                String prefix     = mkBookmarkNamePrefix(markerNumber);
                if (markerName.startsWith(prefix)) {
                    markerName = markerName.substring(prefix.length());
                    if (markerName.startsWith(": ")) { //$NON-NLS-1$
                        markerName = markerName.substring(2);
                    }
                    markerName = markerName.trim();
                    if (markerName.isEmpty()) {
                        markerName = prefix;
                    }
                }
                return new XBookmarksDialog.Model.Row(new String[]{ "" + markerNumber + "."  //$NON-NLS-1$ //$NON-NLS-2$
                                                                  , resName + ":" + line     //$NON-NLS-1$ 
                                                                  , markerName}).setData(markerNumber);  
            }
        }
        return null;
    }
    
}
