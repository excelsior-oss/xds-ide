package com.excelsior.xds.ui.editor.internal.preferences.formatter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.internal.preferences.ModifyDialog;
import com.excelsior.xds.ui.editor.internal.preferences.ProfileManager;

public class FormatterModifyDialog extends ModifyDialog {
    private FormatterProfile fp;
    private boolean isDefProfile;

    private IStatus errDefStatus = new Status(IStatus.ERROR, XdsEditorsPlugin.PLUGIN_ID, Messages.FormatterModifyDialog_CantModifyDefProfile);

    public FormatterModifyDialog(Shell parentShell, String title, String dialogPreferencesKey, FormatterProfile fp, boolean fNewProfile, boolean isDefProfile) {
        super(parentShell, title, dialogPreferencesKey, fNewProfile);
        this.fp = fp;
        this.isDefProfile = isDefProfile;
    }
    
    @Override
    public void create() {
        super.create();
        if (isDefProfile) {
            super.updateStatus(errDefStatus);
        }
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Control res = super.createDialogArea(parent);
        Text txtProfileName = this.getTxtProfileName();
        txtProfileName.setText(fp.getName());
        txtProfileName.setEditable(false);
        return res;
    }
    
    /**
     * To use from tab validators, preserves error status for default profiles
     */
    @Override 
    protected void updateStatus(IStatus status) {
        if (isDefProfile && !status.matches(IStatus.ERROR)) {
                status = errDefStatus;
        }
        super.updateStatus(status);
    }

    @Override
    protected void addPages() {
        addTabPage(Messages.FormatterModifyDialog_Indentation, new TabPageIndentation(fp));
        addTabPage(Messages.FormatterModifyDialog_WhiteSpace, new TabPageWhiteSpace(fp));
        addTabPage(Messages.FormatterModifyDialog_NewLines, new TabPageNewLines(fp));
        addTabPage(Messages.FormatterModifyDialog_LineWrapping, new TabLineWrapping(fp, this));
    }

    @Override
    protected void handleExportButton() {
        ProfileManager.exportProfiles(getShell(), fp, null);
    }

}
