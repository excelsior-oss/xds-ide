package com.excelsior.xds.ui.commands;

import org.eclipse.ui.actions.RenameResourceAction;

/**
 * A command handler to rename the currently selected XDS resources.
 */
public class RenameXdsResourceHandler extends StandardResourceActionHandler
{
    public RenameXdsResourceHandler () {
        super();
        setAction(new RenameResourceAction(shellProvider));
    }

}
