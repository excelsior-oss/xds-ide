package com.excelsior.xds.ui.editor.internal.preferences;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;

import com.excelsior.xds.core.help.IXdsHelpContextIds;
import com.excelsior.xds.core.preferences.PreferenceKeys;
import com.excelsior.xds.ui.commons.utils.HelpUtils;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.commons.text.PairedBracketsPainter;
import com.excelsior.xds.ui.editor.internal.nls.Messages;

public class EditorPreferencePage extends    PreferencePage 
                                  implements IWorkbenchPreferencePage,
                                             IXdsEditorsPreferenceIds                                  
{

    private Button        cbShowBrackets;
    private ColorSelector clrShowBrackets;
    private ColorSelector clrShowBracketsNoMatch;
    private Label         labelShowBrackets;
    private Label         labelShowBracketsNoMatch;
    
    private Button        cbConstructions;

    private Button        cbInactiveCode;
    
    public EditorPreferencePage() {
    }

    public EditorPreferencePage(String title) {
        super(title);
    }

    public EditorPreferencePage(String title, ImageDescriptor image) {
        super(title, image);
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected Control createContents(final Composite parent) {
        parent.setLayout(new GridLayout());
        HelpUtils.setHelp(parent, IXdsHelpContextIds.MODULA2_EDITOR_PREFERENCE_PAGE);
        
        Composite composite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.numColumns= 3;
        composite.setLayout(layout);

        // XDS Modula-2 editor preferences. See 'Text Editors' for general text ....
        Link link= new Link(composite, SWT.NONE);
        link.setText(Messages.XdsEditorPreferencePage_link);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if ("org.eclipse.ui.preferencePages.GeneralTextEditor".equals(e.text)) //$NON-NLS-1$
                    PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
                else if ("org.eclipse.ui.preferencePages.ColorsAndFonts".equals(e.text)) //$NON-NLS-1$
                    PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, "selectFont:org.eclipse.jface.textfont"); //$NON-NLS-1$
//                else if ("org.eclipse.ui.editors.preferencePages.Annotations".equals(e.text)) //$NON-NLS-1$
//                    PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null); //$NON-NLS-1$
                
            }
        });
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint= 300; // only expand further if anyone else requires it
        gridData.horizontalSpan = 3;
        link.setLayoutData(gridData);

        SWTFactory.createVerticalSpacer(composite,  0.5);

        // [v] Highlight matching brackets
        cbShowBrackets = SWTFactory.createCheckbox(composite, Messages.XdsEditorPreferencePage_HighlightBrackets, 3);
        cbShowBrackets.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                reenableAll();
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
        // Matching brackets highlight color: [   ]
        SWTFactory.createLabel(composite, "    ", 1); //$NON-NLS-1$
        labelShowBrackets = SWTFactory.createLabel(composite, Messages.XdsEditorPreferencePage_MatchedBracketsColor+':', 1);
        clrShowBrackets = SWTFactory.createColorSelector(composite, 1);
        
        // No matching brackets highlight color: [   ]
        SWTFactory.createLabel(composite, "    ", 1); //$NON-NLS-1$
        labelShowBracketsNoMatch = SWTFactory.createLabel(composite, Messages.XdsEditorPreferencePage_UnmatchedBracketsColor+':', 1);
        clrShowBracketsNoMatch = SWTFactory.createColorSelector(composite, 1);

        SWTFactory.createVerticalSpacer(composite,  0.5);

        // [v] Highlight operators structure
        cbConstructions = SWTFactory.createCheckbox(composite, Messages.EditorPreferencePage_HlghlightOperatorsStructure, 3);
        cbConstructions.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                reenableAll();
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
        // See _Annotations_ page to configure highlighting settings
        Label label = SWTFactory.createLabel(composite, "    ", 1); //$NON-NLS-1$
        gridData = new GridData();
        gridData.heightHint= clrShowBrackets.getButton().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        label.setLayoutData(gridData);
        
        link= new Link(composite, SWT.NONE);
        link.setText(Messages.EditorPreferencePage_LinkToAnnotationsPage);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String data= null;
                AnnotationPreference preference= EditorsUI.getAnnotationPreferenceLookup().getAnnotationPreference("com.excelsior.xds.ui.editor.constructionMarkerAnnotation"); //$NON-NLS-1$
                if (preference != null)
                    data= preference.getPreferenceLabel();
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, data);
            }
        });
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        gridData.widthHint= 300;
        link.setLayoutData(gridData);

        SWTFactory.createVerticalSpacer(composite,  0.5);

        // [v] Highlight inactive code
        cbInactiveCode = SWTFactory.createCheckbox(composite, Messages.XdsEditorPreferencePage_HighlightInactiveCode, 3);
        
        // See _Syntax_Coloring_ page to configure highlighting settings
        label = SWTFactory.createLabel(composite, "    ", 1); //$NON-NLS-1$
        gridData = new GridData();
        gridData.heightHint= clrShowBrackets.getButton().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        label.setLayoutData(gridData);
        
        link= new Link(composite, SWT.NONE);
        link.setText(Messages.EditorPreferencePage_LinkToColorSettingsPage);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
            }
        });
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        gridData.widthHint= 300;
        link.setLayoutData(gridData);

        
        //// Set values:
        IPreferenceStore store = XdsEditorsPlugin.getDefault().getPreferenceStore();
        if (store != null) {
            initEditorPrefsInStoreIfNeed(store);
            cbShowBrackets.setSelection(store.getBoolean(PREF_HIGHLIGHT_MATCHING_BRACKETS));
            clrShowBrackets.setColorValue(PreferenceConverter.getColor(store, PREF_MATCHED_BRACKETS_COLOR));
            clrShowBracketsNoMatch.setColorValue(PreferenceConverter.getColor(store, PREF_UNMATCHED_BRACKETS_COLOR));
            cbConstructions.setSelection(PreferenceKeys.PKEY_HIGHLIGHT_CONSTRUCTIONS.getStoredBoolean());
            cbInactiveCode.setSelection(PreferenceKeys.PKEY_HIGHLIGHT_INACTIVE_CODE.getStoredBoolean());
        }
        reenableAll();

        return composite;
    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = XdsEditorsPlugin.getDefault().getPreferenceStore();
        if (store != null) {
            initEditorPrefsInStoreIfNeed(store);
            store.setValue(PREF_HIGHLIGHT_MATCHING_BRACKETS, cbShowBrackets.getSelection());
            PreferenceConverter.setValue(store, PREF_MATCHED_BRACKETS_COLOR, clrShowBrackets.getColorValue());
            PreferenceConverter.setValue(store, PREF_UNMATCHED_BRACKETS_COLOR, clrShowBracketsNoMatch.getColorValue());
            PreferenceKeys.PKEY_HIGHLIGHT_CONSTRUCTIONS.setStoredBoolean(cbConstructions.getSelection());
            PreferenceKeys.PKEY_HIGHLIGHT_INACTIVE_CODE.setStoredBoolean(cbInactiveCode.getSelection());
        }
        return true;
    }
    
    @Override
    public void performDefaults() {
        cbShowBrackets.setSelection(true);
        clrShowBrackets.setColorValue(PairedBracketsPainter.DEF_RGB_MATCHED);
        clrShowBracketsNoMatch.setColorValue(PairedBracketsPainter.DEF_RGB_UNMATCHED);
        cbConstructions.setSelection(true);
        reenableAll();
    }
    
    private void reenableAll() {
        boolean b = cbShowBrackets.getSelection();
        clrShowBrackets.setEnabled(b);
        clrShowBracketsNoMatch.setEnabled(b);
        labelShowBrackets.setEnabled(b);
        labelShowBracketsNoMatch.setEnabled(b);
    }
    
    public static void initEditorPrefsInStoreIfNeed(IPreferenceStore store) {
        if (store != null) {
            if (!store.contains(PREF_UNMATCHED_BRACKETS_COLOR)) {
                // NOTE: store.contains(ModulaEditor.EDITOR_MATCHING_BRACKETS) does not works - for boolean with FALSE value it returns TRUE averytime
                store.setValue(PREF_HIGHLIGHT_MATCHING_BRACKETS, true);
                PreferenceConverter.setValue(store, PREF_MATCHED_BRACKETS_COLOR, PairedBracketsPainter.DEF_RGB_MATCHED);
                PreferenceConverter.setValue(store, PREF_UNMATCHED_BRACKETS_COLOR, PairedBracketsPainter.DEF_RGB_UNMATCHED);
            }
        }
    }
    
}
