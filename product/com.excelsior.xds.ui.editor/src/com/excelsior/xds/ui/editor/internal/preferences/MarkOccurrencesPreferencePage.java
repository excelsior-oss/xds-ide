package com.excelsior.xds.ui.editor.internal.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;

import com.excelsior.xds.core.preferences.PreferenceKeys;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.internal.nls.Messages;

public class MarkOccurrencesPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {
    
    private Button cboxOnOff;
    
    @Override
    public IPreferenceStore getPreferenceStore() {
        return XdsEditorsPlugin.getDefault().getPreferenceStore();
    }

    //--- Preference page:
    
    public MarkOccurrencesPreferencePage() {
        setPreferenceStore(XdsEditorsPlugin.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }
    
    @Override
    protected Control createContents(final Composite parent) {
        Composite composite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.numColumns= 1;
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        composite.setLayout(layout);

        Link link= new Link(composite, SWT.NONE);
        link.setText(Messages.MarkOccurrencesPreferencePage_LinkText);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String data= null;
                AnnotationPreference preference= EditorsUI.getAnnotationPreferenceLookup().getAnnotationPreference("org.eclipse.jdt.ui.occurrences"); //$NON-NLS-1$
                if (preference != null)
                    data= preference.getPreferenceLabel();
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, data);
            }
        });
        
        SWTFactory.createLabel(composite, "", 1); //$NON-NLS-1$
        
        cboxOnOff = SWTFactory.createCheckbox(composite, Messages.MarkOccurrencesPreferencePage_MarkOccurrencesOfElement, 1);
        
        initFromStore();
        return composite;
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        PreferenceKeys.PKEY_HIGHLIGHT_OCCURENCES.setStoredBoolean(true);
        initFromStore();
    }

    @Override
    public boolean performOk() {
        PreferenceKeys.PKEY_HIGHLIGHT_OCCURENCES.setStoredBoolean(cboxOnOff.getSelection());
        return super.performOk();
    }

    private void initFromStore() {
        cboxOnOff.setSelection(PreferenceKeys.PKEY_HIGHLIGHT_OCCURENCES.getStoredBoolean());
    }
    
}
