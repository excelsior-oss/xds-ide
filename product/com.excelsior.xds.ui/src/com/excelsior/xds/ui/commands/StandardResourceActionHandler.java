package com.excelsior.xds.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.handlers.HandlerUtil;

import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;

public class StandardResourceActionHandler extends AbstractHandler 
{
	private BaseSelectionListenerAction action;
	
	protected IShellProvider shellProvider = new IShellProvider() {
        public Shell getShell() {
            return WorkbenchUtils.getActivePartShell();
        }};
	
    public StandardResourceActionHandler() {
		super();
	}
    
	public void setAction(BaseSelectionListenerAction action) {
		this.action = action;
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	public boolean isEnabled() {
	    return super.isEnabled();
	}

    /**
     * {@inheritDoc}
     */
	@Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
        	action.selectionChanged((IStructuredSelection)selection);
        }
        action.run();
        return null;
    }

}

