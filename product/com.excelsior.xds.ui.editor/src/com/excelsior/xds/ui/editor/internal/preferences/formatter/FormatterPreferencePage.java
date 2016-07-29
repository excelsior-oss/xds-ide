package com.excelsior.xds.ui.editor.internal.preferences.formatter;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.excelsior.xds.core.help.IXdsHelpContextIds;
import com.excelsior.xds.parser.modula.XdsSourceType;
import com.excelsior.xds.ui.commons.utils.HelpUtils;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.internal.preferences.CreateProfileDialog;
import com.excelsior.xds.ui.editor.internal.preferences.ProfileManager;
import com.excelsior.xds.ui.editor.internal.preferences.ProfileManager.IProfile;


public class FormatterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage                                  
/* 
 * NOTE: all profile-type-depended code here is located in numeral places, all other code uses IProfile only
 * 
 * To create other profile-based preference pages we can transform it to abstract class with 
 *   abstract private void initValues();
 *   abstract private void handleNewButton();
 *   abstract private void handleEditButton();
 * and some specific preview.
 * 
 */
{
    
    private static final String DLG_PREFERENCES_ID = "com.excelsior.xds.ui.editor.internal.preferences.FormatterPreferencePageDlg"; //$NON-NLS-1$
    
    private Button fEditButton;
    private Button fDeleteButton;
    private Combo fProfileCombo;
    private FormatterPreview fPreview;
    
    private ProfileManager fProfileManager; 


    public FormatterPreferencePage() {
    }

    public FormatterPreferencePage(String title) {
        super(title);
    }

    public FormatterPreferencePage(String title, ImageDescriptor image) {
        super(title, image);
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected Control createContents(Composite parent) {
        HelpUtils.setHelp(parent, IXdsHelpContextIds.MODULA2_FORMATTER_PREFERENCE_PAGE);
        Composite composite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        composite.setLayout(layout);
        composite.setFont(parent.getFont());

        GridData data= new GridData(GridData.FILL, GridData.FILL, true, true);

        Control fConfigurationBlockControl= createPreferenceContent(composite);
        fConfigurationBlockControl.setLayoutData(data);

        Dialog.applyDialogFont(composite);
        
        initValues();
        
        return composite;
    }

    
    private Composite createPreferenceContent(Composite parent) {

        final int numColumns = 5;

        final Composite fComposite = new Composite(parent, SWT.NONE);
        fComposite.setFont(parent.getFont());
        final GridLayout layout = new GridLayout(numColumns, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        fComposite.setLayout(layout);
        
        SWTFactory.createLabel(fComposite, Messages.FormatterPreferencePage_ActiveProfile+':', numColumns);

        fProfileCombo = SWTFactory.createCombo(fComposite, 3, SWT.DROP_DOWN | SWT.READ_ONLY);
        fProfileCombo.setFont(fComposite.getFont());
        fProfileCombo.setVisibleItemCount(30);
        fProfileCombo.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent e) {
                handleComboSelection();
            }
        });

        fEditButton = SWTFactory.createPushButton(fComposite, Messages.FormatterPreferencePage_Edit, null);
        fEditButton.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent e) {
                handleEditButton(false);
            }
        });
        
        fDeleteButton = SWTFactory.createPushButton(fComposite, Messages.FormatterPreferencePage_Remove, null);
        fDeleteButton.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent e) {
                handleDeleteButton();
            }
        });

        SWTFactory.createPushButton(fComposite, Messages.FormatterPreferencePage_New, null).addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent e) {
                handleNewButton();
            }
        });
        
        SWTFactory.createPushButton(fComposite, Messages.FormatterPreferencePage_Import, null).addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent e) {
                handleImportButton();
            }
        });
        
        SWTFactory.createPushButton(fComposite, Messages.FormatterPreferencePage_ExportAll, null).addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent e) {
                handleExportButton();
            }
        });
        
        SWTFactory.createLabel(fComposite, "", numColumns); //$NON-NLS-1$

        configurePreview(fComposite, numColumns, fProfileManager);

//        new ProfileComboController();

        return fComposite;
    }
    
    private void configurePreview(Composite composite, int numColumns, ProfileManager profileManager) {
        SWTFactory.createLabel(composite, Messages.FormatterPreferencePage_Preview+':', numColumns);
        fPreview = new FormatterPreview(composite, "indent_preview.mod", XdsSourceType.Modula); //$NON-NLS-1$

        final GridData gd = new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = numColumns;
        gd.verticalSpan= 7;
        gd.widthHint = 0;
        gd.heightHint = 0;
        fPreview.getTextWidget().setLayoutData(gd);
    }


    private void initValues() {
        ArrayList<IProfile> defLst = new ArrayList<IProfile>();
        defLst.add(new FormatterProfile());
        fProfileManager = new ProfileManager(defLst, new FormatterProfile(), FormatterProfile.ID_FORMAT_PREFS);
        fProfileManager.readFromStore(XdsEditorsPlugin.getDefault().getPreferenceStore());
        String activeProfileName = fProfileManager.getActiveProfileName();
        int idx = 0;
        for (int i=0; i<fProfileManager.size(); ++i) {
            String nm = fProfileManager.get(i).getName();
            fProfileCombo.add(nm);
            if (nm.equals(activeProfileName)) {
                idx = i;
            }
        }
        fProfileCombo.select(idx);
        handleComboSelection();
        fPreview.setProfile((FormatterProfile)fProfileManager.getActiveProfile());
    }
    
    
    private FormatterProfile getSelectedProfile() {
        try {
            return (FormatterProfile)fProfileManager.get(fProfileCombo.getSelectionIndex());
        } catch (Exception e) {
            return (FormatterProfile)fProfileManager.get(0);
        }
    }

    
    private void handleComboSelection() {
        FormatterProfile fp = getSelectedProfile();
        fDeleteButton.setEnabled(!fp.isDefaultProfile());
        fProfileManager.setActiveProfileName(fp.getName());
        fPreview.setProfile(fp);
    }
    
    
    @Override
    protected void performDefaults() {
        fProfileCombo.select(0);
        handleComboSelection();
        super.performDefaults();
    }
    
    @Override
    // 'Apply' calls it too:
    public boolean performOk() {
        fProfileManager.saveToStore(XdsEditorsPlugin.getDefault().getPreferenceStore());
        return true;
    }


    private void handleEditButton(boolean fNewProfile) {
        String title = String.format(Messages.FormatterPreferencePage_Profile, getSelectedProfile().getName());
        FormatterProfile sel = (FormatterProfile)getSelectedProfile();
        FormatterProfile edt = new FormatterProfile(sel.getName(), sel);
        FormatterModifyDialog fmd = new FormatterModifyDialog(getShell(), title, DLG_PREFERENCES_ID, edt, fNewProfile, sel.isDefaultProfile());
        if (Dialog.OK ==fmd.open() && !sel.isDefaultProfile()) {
            sel.copyFrom(edt);
            fPreview.setProfile(sel);
        }
    }
    
    private void handleDeleteButton() {
        int idx = fProfileCombo.getSelectionIndex();
        if (!fProfileManager.get(idx).isDefaultProfile()) {
            fProfileManager.remove(idx);
            fProfileCombo.remove(idx);
            fProfileCombo.select(idx < fProfileCombo.getItemCount() ? idx : idx-1);
            handleComboSelection();
        }
    }
    
    private void handleNewButton() {
        final CreateProfileDialog p= new CreateProfileDialog(getShell(), fProfileManager, new FormatterProfile());
        if (p.open() == Window.OK) {
            fProfileCombo.add(p.getCreatedProfile().getName());
            fProfileCombo.select(fProfileCombo.getItemCount()-1);
            handleComboSelection();
            if (p.openEditDialog()) {
                handleEditButton(true);
            }
        }
    }
    
    private void handleImportButton() {
        fProfileManager.importProfiles(getShell());
        
        fProfileCombo.removeAll();
        String activeProfileName = fProfileManager.getActiveProfileName();
        int idx = 0;
        for (int i=0; i<fProfileManager.size(); ++i) {
            String nm = fProfileManager.get(i).getName();
            fProfileCombo.add(nm);
            if (nm.equals(activeProfileName)) {
                idx = i;
            }
        }
        fProfileCombo.select(idx);
        
        fPreview.setProfile((FormatterProfile)fProfileManager.get(idx));
    }
    
    private void handleExportButton() {
        ProfileManager.exportProfiles(getShell(), null, fProfileManager);
    }
    
}
