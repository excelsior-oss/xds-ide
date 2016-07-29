package com.excelsior.xds.ui.preferences.sdk;

import org.eclipse.swt.widgets.TabFolder;

import com.excelsior.xds.ui.internal.nls.Messages;

class EditSdkDialogTabComponents extends EditSdkDialogTabAbstract
{
    public static final String TAB_NAME = Messages.EditSdkDialogTabComponents_Components;

    @Override
    public final String getTabName() {
        return TAB_NAME;
    }

    public EditSdkDialogTabComponents(TabFolder tabFolder, EditSdkDialog editSdkDialog) {
		super(tabFolder, editSdkDialog);
	}
	
}