package com.excelsior.xds.ui.commands;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.excelsior.xds.ui.actions.PasteAction;
import com.excelsior.xds.ui.commons.utils.SelectionUtils;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;

@SuppressWarnings("deprecation")
public class PasteResourceHandler extends StandardResourceActionHandler {
	private PasteAction pasteAction;
	private ISelectionProvider selectionProvider;
    public PasteResourceHandler() {
		super();
		pasteAction = new PasteAction(shellProvider, null);
		selectionProvider = WorkbenchUtils.getActivePartSelectionProvider();
		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(pasteAction);
		}
		IStructuredSelection structuredSelection = SelectionUtils.getStructuredSelection();
		if (structuredSelection != null) {
			pasteAction.selectionChanged(structuredSelection);
		}
		setAction(pasteAction);
	}
    
    @Override
	public void dispose() {
    	if (selectionProvider != null) {
    		selectionProvider.removeSelectionChangedListener(pasteAction);
    	}
	}

	@Override
    public boolean isEnabled() {
    	return super.isEnabled() && pasteAction.isEnabled();
    }
}

