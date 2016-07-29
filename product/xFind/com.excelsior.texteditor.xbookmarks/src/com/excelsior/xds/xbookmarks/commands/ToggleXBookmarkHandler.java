package com.excelsior.xds.xbookmarks.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * Command handler to toggle between activate/deactivate the xBookmark on the current line.
 */
public class ToggleXBookmarkHandler extends XBookmarkHandler {

    /*
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        String markerNumber = event.getParameter("com.excelsior.xds.xbookmarks.commands.xBookmarkNumber"); //$NON-NLS-1$
        toggleBookmark(Integer.parseInt(markerNumber));
        return null;
    }
    
}
