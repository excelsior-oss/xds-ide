package com.excelsior.xds.xbookmarks.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * Command handler to jump from anywhere in Workspace to the xBookmark location
 */
public class GotoXBookmarkHandler extends XBookmarkHandler {

    /*
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute (ExecutionEvent event) throws ExecutionException {
        String markerNumber = event.getParameter("com.excelsior.xds.xbookmarks.commands.xBookmarkNumber"); //$NON-NLS-1$
        gotoBookmark(Integer.parseInt(markerNumber));
        return null;
    }
    
}
