package com.excelsior.xds.ui.tools.menu;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.variables.VariableUtils;
import com.excelsior.xds.ui.commons.utils.SelectionUtils;

public class DynamicToolsMenuManager {
	public List<DynamicToolsMenuItem> contributionItems = new ArrayList<DynamicToolsMenuItem>();
	
	private static class DynamicToolsMenuManagerHolder{
		static DynamicToolsMenuManager INSTANCE = new DynamicToolsMenuManager();
	}
	
	public static DynamicToolsMenuManager getInstance(){
		return DynamicToolsMenuManagerHolder.INSTANCE;
	}
	
	public void clear() {
		contributionItems.clear();
	}
	
	public void add(DynamicToolsMenuItem item) {
		contributionItems.add(item);
	}
	
	public void updateMenu() {
		List<IResource> targetResources = SelectionUtils.getSelectedResources();
		
		for (DynamicToolsMenuItem item : contributionItems) {
			if (item.menuItem.isDisposed()) 
			    continue;
			item.menuItem.setEnabled(item.tool.isEnabled(targetResources));
			String itemText = null;
            if (!item.menuItem.isEnabled()) {
                try {
                    itemText = VariableUtils.performStringSubstitution(item.toolDesc.getInactiveMenuItem());
                } catch (CoreException e) {
                    LogHelper.logError(e);
                }
            }
			if (item.menuItem.isEnabled() || StringUtils.isBlank(itemText) ) {
				try {
					itemText = VariableUtils.performStringSubstitution(item.toolDesc.getMenuItem());
				} catch (CoreException e) {
					LogHelper.logError(e);
				}
			}
			if (StringUtils.isBlank(itemText)) {
			    itemText = item.toolDesc.getToolName();
			}
			item.menuItem.setText(itemText);
		}
	}
}
