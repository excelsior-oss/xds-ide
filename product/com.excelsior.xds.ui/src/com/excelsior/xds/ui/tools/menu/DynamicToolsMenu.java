package com.excelsior.xds.ui.tools.menu;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.excelsior.xds.core.console.IXdsConsole;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkTool;
import com.excelsior.xds.core.tool.ITool;
import com.excelsior.xds.core.tool.XToolFactory;
import com.excelsior.xds.ui.commons.utils.SelectionUtils;
import com.excelsior.xds.ui.internal.nls.Messages;
import com.excelsior.xds.ui.internal.services.ServiceHolder;

public class DynamicToolsMenu extends ContributionItem {

	public DynamicToolsMenu() {
	}

	public DynamicToolsMenu(String id) {
		super(id);
	}
	
	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public void fill(Menu menu, int index) {
        boolean hasTools = false;
		DynamicToolsMenuManager.getInstance().clear();
		
		try {
			List<IResource> targetResources = SelectionUtils.getSelectedResources();
			
            if (CollectionUtils.isEmpty(targetResources)) {
			    return;
			}
			
			Set<IProject> projects = getProjects(targetResources);
			if (CollectionUtils.isEmpty(projects)) {
				return;
			}
			
			Sdk activeSdk = null;
			for (IProject project : projects) {
				XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(project);
				Sdk projectSdk = xdsProjectSettings.getProjectSdk();
                if (activeSdk == null) {
                    activeSdk = projectSdk;
                }
				if (projectSdk == null || projectSdk != activeSdk) {
				    return; // project with SDK removed from SDK registry or different SDKs - hz what 2 do 
				} 
			}
			
			//NOW: selection has project(s) with the same SDK, add all tools from this SDK to the menu
			String group = ""; //$NON-NLS-1$
			Menu curMenu = menu;
            for (int i = activeSdk.getTools().size() - 1; i > -1 ; i--) {
                final SdkTool toolDesc = activeSdk.getTools().get(i);
                String grp = toolDesc.getMenuGroup();
                if (!StringUtils.isBlank(grp)) {
                    if (!grp.equals(group)) {
                        // new submenu
                        final MenuItem subMenuItem = new MenuItem(menu, SWT.CASCADE, index);
                        subMenuItem.setText(grp);
                        curMenu = new Menu(menu.getShell(), SWT.DROP_DOWN);
                        subMenuItem.setMenu(curMenu);
                        group = grp;
                    }
                } else { // root menu item
                    group = ""; //$NON-NLS-1$
                    curMenu = menu;
                }
                if (toolDesc.isSeparator()) {
                    new MenuItem(curMenu, SWT.SEPARATOR, index);
                } else {
                    final ITool runnableTool = XToolFactory.createFrom(toolDesc);
                    
                    MenuItem menuItem = new MenuItem(curMenu, SWT.NONE, index);
                    menuItem.setText(toolDesc.getToolName());
                    menuItem.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            try {
                                invokeTool(runnableTool);
                            } catch (CoreException e1) {
                                LogHelper.logError(e1);
                            }
                        }
                    });
                    DynamicToolsMenuManager.getInstance().add(new DynamicToolsMenuItem(menuItem, toolDesc, runnableTool));
                    hasTools = true;
                }
            }
            DynamicToolsMenuManager.getInstance().updateMenu();
		}
		finally {
			if (!hasTools) {
				MenuItem menuItem = new MenuItem(menu, SWT.NONE, index);
				menuItem.setText(Messages.DynamicToolsMenu_NoActiveToolsAvailable);
				menuItem.setEnabled(false);
			}
		}
	}

	private void invokeTool(final ITool runnableTool) throws CoreException {
	    String consoleTitle = " [" + Messages.ConsoleType_XDS_Tool + "] " + runnableTool.getLocation(); //$NON-NLS-1$ //$NON-NLS-2$
	    IXdsConsole console = ServiceHolder.getInstance().getConsoleFactory().getXdsConsole(consoleTitle);
        console.clearConsole();
        console.show();
        runnableTool.invoke(SelectionUtils.getSelectedResources(), console);
	}
	
	private Set<IProject> getProjects(List<IResource> resources) {
		Set<IProject> projects = new HashSet<IProject>();
		if (resources != null) {
			for (IResource r : resources) {
				projects.add(r.getProject());
			}
		}
		return projects;
	}
}
