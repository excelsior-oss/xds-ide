package com.excelsior.xds.ui.commands;

import com.excelsior.xds.ui.actions.CopyAction;
import com.excelsior.xds.ui.commons.utils.SelectionUtils;

@SuppressWarnings("deprecation")
public class CopyXdsResourceToClipboardHandler extends StandardResourceActionHandler 
{
    public CopyXdsResourceToClipboardHandler() {
		super();
		setAction(new CopyAction(shellProvider, null));
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return super.isEnabled()
            && SelectionUtils.isSelectedSiblingResources();
    }
    
}

