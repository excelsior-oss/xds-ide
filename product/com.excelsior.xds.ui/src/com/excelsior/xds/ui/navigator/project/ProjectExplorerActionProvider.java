package com.excelsior.xds.ui.navigator.project;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

import com.excelsior.xds.core.project.ProjectUtils;

public class ProjectExplorerActionProvider extends CommonActionProvider {

	public ProjectExplorerActionProvider() {
	}

	@Override
	public void init(ICommonActionExtensionSite commonActionExtensionSite) {
		super.init(commonActionExtensionSite);
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		IMenuManager viewMenu = actionBars.getMenuManager();
		ICommonActionExtensionSite site = getActionSite();
		IContributionItem contributionItem = viewMenu.find(ShowResourcesContribution.ID);
		if (contributionItem != null) {
			viewMenu.remove(contributionItem);
		}
		if (ProjectUtils.isWorkspaceContainsXdsProjects()) {
			viewMenu.add(new ShowResourcesContribution(site.getStructuredViewer(), site.getExtensionStateModel()));
		}
	}
}
