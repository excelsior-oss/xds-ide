package com.excelsior.xds.ui.preferences.sdk;

import org.eclipse.swt.widgets.TabFolder;

import com.excelsior.xds.ui.internal.nls.Messages;

public class EditSdkDialogTabEnvironment extends EditSdkDialogTabAbstract {

    public static final String TAB_NAME = Messages.EditSdkDialogTabEnvironment_Environment;

    @Override
    public final String getTabName() {
        return TAB_NAME;
    }
    
    public EditSdkDialogTabEnvironment(TabFolder tabFolder, EditSdkDialog editSdkDialog) {
        super(tabFolder, editSdkDialog);
    }

}
