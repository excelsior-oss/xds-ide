package com.excelsior.xds.ui.tools.menu;

import org.eclipse.swt.widgets.MenuItem;

import com.excelsior.xds.core.sdk.SdkTool;
import com.excelsior.xds.core.tool.ITool;

class DynamicToolsMenuItem {
	final MenuItem menuItem;
	final SdkTool toolDesc;
	final ITool tool;
	
	DynamicToolsMenuItem(MenuItem menuItem, SdkTool toolDesc, ITool tool) {
		this.menuItem = menuItem;
		this.toolDesc = toolDesc;
		this.tool = tool;
	}
}
