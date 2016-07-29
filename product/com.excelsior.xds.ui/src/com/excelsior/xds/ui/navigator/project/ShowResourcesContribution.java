package com.excelsior.xds.ui.navigator.project;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.navigator.IExtensionStateModel;

import com.excelsior.xds.ui.internal.nls.Messages;

public class ShowResourcesContribution extends ContributionItem {
	
	public static final String ID = "com.excelsior.xds.ui.navigator.menu.showResources"; //$NON-NLS-1$
	
	private StructuredViewer structuredViewer;
	private ExtensionStateModelAccessor stateModelAccess;

	public ShowResourcesContribution(StructuredViewer structuredViewer, IExtensionStateModel extensionStateModel) {
		super(ID);
		this.structuredViewer = structuredViewer;
		this.stateModelAccess = new ExtensionStateModelAccessor(extensionStateModel);
	}

	public ShowResourcesContribution() {
		super(ID);
	}

	@Override
	public void fill(Menu menu, int index) {
		final MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
		menuItem.setText(Messages.ProjectExplorerViewMenu_ShowAllFiles);
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				run();
			}
		});
		menuItem.setSelection(stateModelAccess.isShowResources());
		menu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
			}
		});
	}

	protected void run() {
		stateModelAccess.setShowResources(!stateModelAccess.isShowResources());
		structuredViewer.getControl().setRedraw(false);
		try {
			structuredViewer.refresh();
		} finally {
			structuredViewer.getControl().setRedraw(true);
		}
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
}