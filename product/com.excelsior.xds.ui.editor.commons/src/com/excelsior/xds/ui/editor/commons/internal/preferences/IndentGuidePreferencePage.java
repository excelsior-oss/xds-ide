package com.excelsior.xds.ui.editor.commons.internal.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ColorUtil;

import com.excelsior.xds.ui.editor.commons.internal.nls.Messages;
import com.excelsior.xds.ui.editor.commons.plugin.EditorCommonsPlugin;


public class IndentGuidePreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {
    
    private Button enabled;
    private Composite attributes;
    private Spinner lineAlpha;
    private Combo lineStyle;
    private static final String[] styles = {
            Messages.IndentGuidePreferencePage_SOLID,
            Messages.IndentGuidePreferencePage_DASH,
            Messages.IndentGuidePreferencePage_DOT,
            Messages.IndentGuidePreferencePage_DASHDOT,
            Messages.IndentGuidePreferencePage_DASHDOTDOT};
    private Spinner lineWidth;
    private Spinner lineShift;
    private ColorFieldEditor colorFieldEditor;

    //--- Preference store IO & color:
    
    private static Color color;
    private static IPreferenceStore preferenceStore;
    
    public static IPreferenceStore staticGetPreferenceStore() {
        if (preferenceStore == null) {
            preferenceStore = EditorCommonsPlugin.getDefault().getPreferenceStore();

            if (!preferenceStore.contains(PreferenceConstants.LINE_COLOR)) {
                // not initialized yet: init all defaults:
                PreferenceConverter.setDefault(preferenceStore, PreferenceConstants.LINE_COLOR, new RGB(128, 128, 128));
                preferenceStore.setDefault(PreferenceConstants.ENABLED, true);
                preferenceStore.setDefault(PreferenceConstants.LINE_ALPHA, 100);
                preferenceStore.setDefault(PreferenceConstants.LINE_STYLE, 1);
                preferenceStore.setDefault(PreferenceConstants.LINE_WIDTH, 1);
                preferenceStore.setDefault(PreferenceConstants.LINE_SHIFT, 3);
            }
        }
        return preferenceStore;
    }
    
    @Override
    public IPreferenceStore getPreferenceStore() {
        return staticGetPreferenceStore();
    }

    public static Color getColor() {
        if (color == null) {
            String colorString = staticGetPreferenceStore().getString(
                    PreferenceConstants.LINE_COLOR);
            color = new Color(PlatformUI.getWorkbench().getDisplay(),
                    ColorUtil.getColorValue(colorString));
        }
        return color;
    }

    public static void setColor(Color clr) {
        if (color != null) {
            color.dispose();
        }
        color = clr;
    }

    
    //--- Preference page:
    
    public IndentGuidePreferencePage() {
        setPreferenceStore(EditorCommonsPlugin.getDefault().getPreferenceStore());
        setDescription(Messages.IndentGuidePreferencePage_pageDesc);
    }

    public void init(IWorkbench workbench) {
    }
    
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));

        enabled = new Button(composite, SWT.CHECK);
        enabled.setText(Messages.IndentGuidePreferencePage_EnableIndentGuide);
        enabled.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                enableControls(enabled.getSelection());
            }
        });

        Group group = new Group(composite, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        group.setLayout(new GridLayout(1, true));
        group.setText(Messages.IndentGuidePreferencePage_LineAttributes);
        attributes = new Composite(group, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        gridData.horizontalIndent = 5;
        attributes.setLayoutData(gridData);
        attributes.setLayout(new GridLayout(2, false));

        new Label(attributes, SWT.NONE)
                .setText(Messages.IndentGuidePreferencePage_Alpha_0_255 + ':');
        lineAlpha = new Spinner(attributes, SWT.BORDER);
        lineAlpha.setMinimum(0);
        lineAlpha.setMaximum(255);
        new Label(attributes, SWT.NONE)
                .setText(Messages.IndentGuidePreferencePage_Style + ':');
        lineStyle = new Combo(attributes, SWT.READ_ONLY);
        lineStyle.setItems(styles);
        new Label(attributes, SWT.NONE)
                .setText(Messages.IndentGuidePreferencePage_Width_1_8 + ':');
        lineWidth = new Spinner(attributes, SWT.BORDER);
        lineWidth.setMinimum(1);
        lineWidth.setMaximum(8);
        new Label(attributes, SWT.NONE)
                .setText(Messages.IndentGuidePreferencePage_Shift_1_8 + ':');
        lineShift = new Spinner(attributes, SWT.BORDER);
        lineShift.setMinimum(0);
        lineShift.setMaximum(8);
        colorFieldEditor = new ColorFieldEditor(PreferenceConstants.LINE_COLOR,
                Messages.IndentGuidePreferencePage_Color + ':', attributes);
        colorFieldEditor.setPreferenceStore(getPreferenceStore());

        loadPreferences();
        return composite;
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        IPreferenceStore store = getPreferenceStore();
        enabled.setSelection(store
                .getDefaultBoolean(PreferenceConstants.ENABLED));
        lineAlpha.setSelection(store
                .getDefaultInt(PreferenceConstants.LINE_ALPHA));
        int index = store.getDefaultInt(PreferenceConstants.LINE_STYLE) - 1;
        if (index < 0 || index >= styles.length) {
            index = 0;
        }
        lineStyle.setText(styles[index]);
        lineWidth.setSelection(store
                .getDefaultInt(PreferenceConstants.LINE_WIDTH));
        lineShift.setSelection(store
                .getDefaultInt(PreferenceConstants.LINE_SHIFT));
        colorFieldEditor.loadDefault();
        enableControls(enabled.getSelection());
    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();
        store.setValue(PreferenceConstants.ENABLED, enabled.getSelection());
        store.setValue(PreferenceConstants.LINE_ALPHA, lineAlpha.getSelection());
        store.setValue(PreferenceConstants.LINE_STYLE,
                lineStyle.getSelectionIndex() + 1);
        store.setValue(PreferenceConstants.LINE_WIDTH, lineWidth.getSelection());
        store.setValue(PreferenceConstants.LINE_SHIFT, lineShift.getSelection());
        colorFieldEditor.store();
        RGB rgb = colorFieldEditor.getColorSelector().getColorValue();
        Color color = new Color(PlatformUI.getWorkbench().getDisplay(), rgb);
        setColor(color);
        return super.performOk();
    }

    private void loadPreferences() {
        IPreferenceStore store = getPreferenceStore();
        enabled.setSelection(store.getBoolean(PreferenceConstants.ENABLED));
        lineAlpha.setSelection(store.getInt(PreferenceConstants.LINE_ALPHA));
        int index = store.getInt(PreferenceConstants.LINE_STYLE) - 1;
        if (index < 0 || index >= styles.length) {
            index = 0;
        }
        lineStyle.setText(styles[index]);
        lineWidth.setSelection(store.getInt(PreferenceConstants.LINE_WIDTH));
        lineShift.setSelection(store.getInt(PreferenceConstants.LINE_SHIFT));
        colorFieldEditor.load();
        enableControls(enabled.getSelection());
    }

    private void enableControls(boolean enabled) {
        for (Control control : attributes.getChildren()) {
            control.setEnabled(enabled);
        }
    }
    
    public class PreferenceConstants {
        private static final String PREFERENCIES_PREFIX = EditorCommonsPlugin.PLUGIN_ID + ".IndentGuide."; //$NON-NLS-1$
        
        public static final String ENABLED    = PREFERENCIES_PREFIX + "enabled"; //$NON-NLS-1$
        public static final String LINE_ALPHA = PREFERENCIES_PREFIX + "line_alpha"; //$NON-NLS-1$
        public static final String LINE_STYLE = PREFERENCIES_PREFIX + "line_style"; //$NON-NLS-1$
        public static final String LINE_WIDTH = PREFERENCIES_PREFIX + "line_width"; //$NON-NLS-1$
        public static final String LINE_SHIFT = PREFERENCIES_PREFIX + "line_shift"; //$NON-NLS-1$
        public static final String LINE_COLOR = PREFERENCIES_PREFIX + "line_color"; //$NON-NLS-1$

    }


}
