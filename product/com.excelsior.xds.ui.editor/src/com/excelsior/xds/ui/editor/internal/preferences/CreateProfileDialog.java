package com.excelsior.xds.ui.editor.internal.preferences;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.internal.preferences.ProfileManager.IProfile;

public class CreateProfileDialog extends StatusDialog {


    private static final String PREF_OPEN_EDIT_DIALOG= XdsEditorsPlugin.PLUGIN_ID + ".create_profile_dialog.open_edit"; //$NON-NLS-1$

    private Text fNameText;
    private Combo fProfileCombo;
    private Button fEditCheckbox;

    private final static Status fOk= new Status(IStatus.OK, XdsEditorsPlugin.PLUGIN_ID, ""); //$NON-NLS-1$
    private final static Status fEmpty= new Status(IStatus.ERROR, XdsEditorsPlugin.PLUGIN_ID, Messages.CreateProfileDialog_ProfNameEmpty);
    private final static Status fDuplicate= new Status(IStatus.ERROR, XdsEditorsPlugin.PLUGIN_ID, Messages.CreateProfileDialog_NameExists);

    private final ProfileManager fProfileManager;
    private final IProfile fProfileFactory;
    private final List<IProfile> fProfilesList;
    private final List<String> fProfileNamesList;

    private IProfile fCreatedProfile;
    protected boolean fOpenEditDialog;


    public CreateProfileDialog(Shell parentShell, ProfileManager profileManager, IProfile profileFactory) {
        super(parentShell);
        fProfileManager= profileManager;
        fProfileFactory= profileFactory;
        fProfilesList= fProfileManager.getProfiles();
        fProfileNamesList= fProfileManager.getProfileNames();
    }


    @Override
    public void create() {
        super.create();
        setTitle(Messages.CreateProfileDialog_NewProfile);
    }

    @Override
    public Control createDialogArea(Composite parent) {

        final int numColumns= 2;

        final Composite composite= (Composite) super.createDialogArea(parent);
        ((GridLayout) composite.getLayout()).numColumns= numColumns;
        
        // Create "Profile name:" label
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = numColumns;
        gd.widthHint= convertWidthInCharsToPixels(60);
        final Label nameLabel = new Label(composite, SWT.WRAP);
        nameLabel.setText(Messages.CreateProfileDialog_ProfName + ':');
        nameLabel.setLayoutData(gd);

        // Create text field to enter name
        gd = new GridData( GridData.FILL_HORIZONTAL);
        gd.horizontalSpan= numColumns;
        fNameText= new Text(composite, SWT.SINGLE | SWT.BORDER);
        fNameText.setLayoutData(gd);
        fNameText.addModifyListener( new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                doValidation();
            }
        });

        // Create "Initialize settings ..." label
        gd = new GridData();
        gd.horizontalSpan = numColumns;
        Label profileLabel = new Label(composite, SWT.WRAP);
        profileLabel.setText(Messages.CreateProfileDialog_InitFromProfile + ':');
        profileLabel.setLayoutData(gd);

        gd= new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan= numColumns;
        fProfileCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        fProfileCombo.setLayoutData(gd);
        fProfileCombo.setVisibleItemCount(30);


        // "Open the edit dialog now" checkbox
        gd= new GridData();
        gd.horizontalSpan= numColumns;
        fEditCheckbox= new Button(composite, SWT.CHECK);
        fEditCheckbox.setText(Messages.CreateProfileDialog_OpenEditDlg);
        fEditCheckbox.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                fOpenEditDialog= ((Button)e.widget).getSelection();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        final IDialogSettings dialogSettings= XdsEditorsPlugin.getDefault().getDialogSettings();//.get(PREF_OPEN_EDIT_DIALOG);
        if (dialogSettings.get(PREF_OPEN_EDIT_DIALOG) != null) {
            fOpenEditDialog= dialogSettings.getBoolean(PREF_OPEN_EDIT_DIALOG);
        } else {
            fOpenEditDialog= true;
        }
        fEditCheckbox.setSelection(fOpenEditDialog);

        fProfileCombo.setItems(fProfileNamesList.toArray(new String[0]));
        fProfileCombo.setText(fProfileManager.getActiveProfileName());
        updateStatus(fEmpty);

        applyDialogFont(composite);

        fNameText.setFocus();

        return composite;
    }


    /**
     * Validate the current settings
     */
    protected void doValidation() {
        final String name= fNameText.getText().trim();

        if (fProfileNamesList.contains(name)) {
            updateStatus(fDuplicate);
            return;
        }
        if (name.length() == 0) {
            updateStatus(fEmpty);
            return;
        }
        updateStatus(fOk);
    }


    @Override
    protected void okPressed() {
        if (!getStatus().isOK())
            return;

        XdsEditorsPlugin.getDefault().getDialogSettings().put(PREF_OPEN_EDIT_DIALOG, fOpenEditDialog);
        
        IProfile from = fProfilesList.get(fProfileCombo.getSelectionIndex());
        fCreatedProfile = fProfileFactory.createFromProfile(from, fNameText.getText().trim());
        
        fProfileManager.add(fCreatedProfile);
        super.okPressed();
    }

    public final IProfile getCreatedProfile() {
        return fCreatedProfile;
    }

    public final boolean openEditDialog() {
        return fOpenEditDialog;
    }
}
