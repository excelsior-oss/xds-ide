package com.excelsior.xds.ui.commands;

import org.eclipse.ui.actions.MoveResourceAction;

import com.excelsior.xds.ui.commons.utils.SelectionUtils;

/**
 * A command handler to move the currently selected XDS resources elsewhere
 * in the workspace. All resources being moved as a group must be siblings.
 */
public class MoveXdsResourceHandler extends StandardResourceActionHandler 
{
    public MoveXdsResourceHandler () {
        super();
        setAction(new MoveResourceAction(shellProvider));
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
