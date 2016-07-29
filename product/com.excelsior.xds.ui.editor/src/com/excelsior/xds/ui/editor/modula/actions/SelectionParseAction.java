package com.excelsior.xds.ui.editor.modula.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchSite;

import com.excelsior.xds.ui.editor.modula.ModulaEditor;

public class SelectionParseAction extends Action {

	protected IWorkbenchSite site;
	protected ModulaEditor editor;

	public SelectionParseAction() {
		super();
	}

	public SelectionParseAction(ModulaEditor editor) {
		super();
		this.editor = editor;
		site = editor.getSite();
	}

	public SelectionParseAction(IWorkbenchSite site) {
		super();
		this.site = site;
	}

	public IWorkbenchSite getSite() {
		return site;
	}

	protected ISelection getSelection() {
		ISelection sel = null;
		if (site != null && site.getSelectionProvider() != null) {
			sel = site.getSelectionProvider().getSelection();
		}

		return sel;
	}

	protected ITextSelection getSelectedStringFromEditor() {
		ISelection selection = getSelection();
		if (!(selection instanceof ITextSelection))
			return null;

		return (ITextSelection) selection;
	}
}
