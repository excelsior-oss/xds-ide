package com.excelsior.xds.ui.commands;

import org.eclipse.ui.actions.DeleteResourceAction;

/**
 * A command handler to delete the currently selected XDS resources.
 */
public class DeleteXdsResourceHandler extends StandardResourceActionHandler 
{
    public DeleteXdsResourceHandler() {
		super();
		setAction(new DeleteResourceAction(shellProvider));
	}
}

