package com.excelsior.xds.ui.preferences.sdk;

import org.eclipse.swt.widgets.TabFolder;

import com.excelsior.xds.ui.internal.nls.Messages;

public class EditSdkDialogTabTemplates extends EditSdkDialogTabAbstract {

    public static final String TAB_NAME = Messages.EditSdkDialogTabTemplates_Templates;

    @Override
    public final String getTabName() {
        return TAB_NAME;
    }
    
    public EditSdkDialogTabTemplates(TabFolder tabFolder, EditSdkDialog editSdkDialog) {
        super(tabFolder, editSdkDialog);
    }

}
