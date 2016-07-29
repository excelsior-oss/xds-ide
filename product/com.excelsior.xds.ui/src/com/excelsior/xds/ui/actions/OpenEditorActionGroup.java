package com.excelsior.xds.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.navigator.ICommonActionConstants;

public class OpenEditorActionGroup extends ActionGroup {

	private IWorkbenchSite fSite;
	private boolean fIsEditorOwner;
	private OpenAction fOpen;
	private ISelectionProvider fSelectionProvider;

	/**
	 * Creates a new <code>OpenActionGroup</code>. The group requires
	 * that the selection provided by the part's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 *
	 * @param part the view part that owns this action group
	 */
	public OpenEditorActionGroup(IViewPart part) {
		this(part.getSite(), null);
	}

	public OpenEditorActionGroup(IWorkbenchPartSite site, ISelectionProvider specialSelectionProvider) {
		fSite= site;
		fOpen= new OpenAction(fSite);
		fSelectionProvider= specialSelectionProvider == null ? fSite.getSelectionProvider() : specialSelectionProvider;
		initialize();
		if (specialSelectionProvider != null)
			fOpen.setSpecialSelectionProvider(specialSelectionProvider);
	}

	public OpenEditorActionGroup(TextEditor editor) {
		fIsEditorOwner= true;
		fOpen= new OpenAction(editor);
		editor.setAction("OpenEditor", fOpen); //$NON-NLS-1$
		fSite= editor.getEditorSite();
		fSelectionProvider= fSite.getSelectionProvider();
		initialize();
	}

	public IAction getOpenAction() {
		return fOpen;
	}

	private void initialize() {
		ISelection selection= fSelectionProvider.getSelection();
		fOpen.update(selection);
		if (!fIsEditorOwner) {
			fSelectionProvider.addSelectionChangedListener(fOpen);
		}
	}

	/* (non-Javadoc)
	 * Method declared in ActionGroup
	 */
	@Override
	public void fillActionBars(IActionBars actionBar) {
		super.fillActionBars(actionBar);
		setGlobalActionHandlers(actionBar);
	}

	/* (non-Javadoc)
	 * Method declared in ActionGroup
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		appendToGroup(menu, fOpen);
		if (!fIsEditorOwner) {
			addOpenWithMenu(menu);
		}
	}

	/*
	 * @see ActionGroup#dispose()
	 */
	@Override
	public void dispose() {
		fSelectionProvider.removeSelectionChangedListener(fOpen);
		super.dispose();
	}

	private void setGlobalActionHandlers(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, fOpen);
	}

	private void appendToGroup(IMenuManager menu, IAction action) {
		if (action.isEnabled())
			menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, action);
	}

	private void addOpenWithMenu(IMenuManager menu) {
		ISelection selection= getContext().getSelection();
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection))
			return;
		IStructuredSelection ss= (IStructuredSelection)selection;
		if (ss.size() != 1)
			return;

		Object o= ss.getFirstElement();
		if (!(o instanceof IAdaptable))
			return;

		IAdaptable element= (IAdaptable)o;
		Object resource= element.getAdapter(IResource.class);
		if (!(resource instanceof IFile))
			return;

		// Create a menu.
		IMenuManager submenu= new MenuManager("Open With");
		submenu.add(new OpenWithMenu(fSite.getPage(), (IFile) resource));

		// Add the submenu.
		menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, submenu);
	}
}
