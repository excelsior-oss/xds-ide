package com.excelsior.xds.xbookmarks.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

import com.excelsior.xds.xbookmarks.XBookmarksUtils;

public class XBookmarksState extends AbstractSourceProvider {

    public final static String EXIST_STATE = "com.excelsior.xds.xbookmarks.exist"; //$NON-NLS-1$
    public final static String TRUE  = "TRUE"; //$NON-NLS-1$
    public final static String FALSE = "FALSE"; //$NON-NLS-1$
    
    private int markerCount = 0;
    
    @Override
    public String[] getProvidedSourceNames() {
        return new String[] {EXIST_STATE};
    }

    @Override
    public Map<String,String> getCurrentState() {
        Map<String,String> map = new HashMap<String,String>(1);
        markerCount  = XBookmarksUtils.getActivatedBookmarkNumbers().size();
        String value = markerCount > 0 ? TRUE : FALSE; 
        map.put(EXIST_STATE, value);
        return map;
    }

    public void fireBookmarkSet() {
        markerCount++;
        fireSourceChanged(ISources.WORKBENCH, EXIST_STATE, TRUE);
    }
    
    public void fireBookmarkRemoved(int removedMarkerCount) {
        markerCount -= removedMarkerCount;
        if (markerCount == 0) {
            fireSourceChanged(ISources.WORKBENCH, EXIST_STATE, FALSE);
        }
    }

    public void fireAllBookmarkRemoved() {
        if (markerCount > 0) {
            markerCount = 0;
            fireSourceChanged(ISources.WORKBENCH, EXIST_STATE, FALSE);
        }
    }
    
    @Override
    public void dispose() {
    }

}
