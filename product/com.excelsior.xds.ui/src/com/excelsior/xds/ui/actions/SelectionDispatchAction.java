package com.excelsior.xds.ui.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;

public abstract class SelectionDispatchAction extends Action implements ISelectionChangedListener {

	private IWorkbenchSite fSite;
	private ISelectionProvider fSpecialSelectionProvider;

	protected SelectionDispatchAction(IWorkbenchSite site) {
		Assert.isNotNull(site);
		fSite= site;
	}

	public IWorkbenchSite getSite() {
		return fSite;
	}

	public ISelection getSelection() {
		ISelectionProvider selectionProvider= getSelectionProvider();
		if (selectionProvider != null)
			return selectionProvider.getSelection();
		else
			return null;
	}

	public Shell getShell() {
		return fSite.getShell();
	}

	public ISelectionProvider getSelectionProvider() {
		if (fSpecialSelectionProvider != null) {
			return fSpecialSelectionProvider;
		}
		return fSite.getSelectionProvider();
	}

	public void setSpecialSelectionProvider(ISelectionProvider provider) {
		fSpecialSelectionProvider= provider;
	}

	public void update(ISelection selection) {
		dispatchSelectionChanged(selection);
	}

	public void selectionChanged(IStructuredSelection selection) {
		selectionChanged((ISelection)selection);
	}

	public void run(IStructuredSelection selection) {
		run((ISelection)selection);
	}

	public void selectionChanged(ITextSelection selection) {
		selectionChanged((ISelection)selection);
	}

	public void run(ITextSelection selection) {
		run((ISelection)selection);
	}

	public void selectionChanged(ISelection selection) {
		setEnabled(false);
	}

	public void run(ISelection selection) {
	}

	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	@Override
	public void run() {
		dispatchRun(getSelection());
	}

	/* (non-Javadoc)
	 * Method declared on ISelectionChangedListener.
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		dispatchSelectionChanged(event.getSelection());
	}

	private void dispatchSelectionChanged(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			selectionChanged((IStructuredSelection)selection);
		} else if (selection instanceof ITextSelection) {
			selectionChanged((ITextSelection)selection);
		} else {
			selectionChanged(selection);
		}
	}

	private void dispatchRun(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			run((IStructuredSelection)selection);
		} else if (selection instanceof ITextSelection) {
			run((ITextSelection)selection);
		} else {
			run(selection);
		}
	}
}