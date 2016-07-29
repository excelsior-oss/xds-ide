package com.excelsior.xds.xbookmarks.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;

import com.excelsior.xds.xbookmarks.internal.nls.Messages;

/**
 * A map of the externalizable name of the xBookmark parameter value.
 * This class is only used to display parameters to to the user. 
 */
public class xBookmarkParameter implements IParameterValues {

    /*
     * @see org.eclipse.core.commands.IParameterValues#getParameterValues()
     */
    public Map<String, String> getParameterValues() {
        Map<String, String> map = new HashMap<String, String>(10);
        for (int i = 0; i <= 9; i++) {
            map.put(Messages.BookmarkAction_ParameterName + i, "" + i); //$NON-NLS-1$
        }
        return map;
    }
    
}
