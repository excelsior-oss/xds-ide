package com.excelsior.texteditor.xfind.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.excelsior.texteditor.xfind.XFindPlugin;
import com.excelsior.texteditor.xfind.internal.nls.Messages;

public class XFindPreferencePage extends PreferencePage implements IWorkbenchPreferencePage 
{
    // hFind is separate package and may work without other our plugins. So this ID is hardcoded
    // here but F1-help will work only when XDS documentatin is available.
    private static final String HELP_ID = "com.excelsior.xds.help.preferences_xfind_panel";
    
    private IPreferenceStore fStore;
    private Button rbTop;
    private Button rbBottom;
    
    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected Control createContents(final Composite parent) {
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, HELP_ID);

        fStore = XFindPlugin.getDefault().getPreferenceStore(); 

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));
        
        Group group = new Group(composite, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        group.setLayout(new GridLayout(1, true));
        group.setText(Messages.XFindPreferencePage_xFindPlacement);
        
        group.setLayout(new GridLayout(2, false));

        createVerticalSpacer(group, 0.3); 

        rbTop = new Button(group, SWT.RADIO);
        rbTop.setText(Messages.XFindPreferencePage_Top);

        rbBottom = new Button(group, SWT.RADIO);
        rbBottom.setText(Messages.XFindPreferencePage_Bottom);
        
        if (!fStore.contains(XFindPanel.XFIND_PANEL_PLACEMENT)) {
            fStore.setDefault(XFindPanel.XFIND_PANEL_PLACEMENT, XFindPanel.XFIND_PANEL_PLACEMENT_TOP);
        }

        boolean bTop = (fStore.getInt(XFindPanel.XFIND_PANEL_PLACEMENT) != XFindPanel.XFIND_PANEL_PLACEMENT_BOTTOM);
        rbTop.setSelection(bTop);
        rbBottom.setSelection(!bTop);

        createVerticalSpacer(group, 0.3); 
        
        return composite;
    }
    
    
    @Override
    public boolean performOk() {
        fStore.setValue(XFindPanel.XFIND_PANEL_PLACEMENT, rbTop.getSelection() ? 
                XFindPanel.XFIND_PANEL_PLACEMENT_TOP : XFindPanel.XFIND_PANEL_PLACEMENT_BOTTOM);
        return true;
    }
    
    protected void performDefaults() {
        rbTop.setSelection(true);
        rbBottom.setSelection(false);
    }
    
    public static void createVerticalSpacer(Composite parent, double xLines) {
        Label lbl = new Label(parent, SWT.NONE);
        
        GC gc = new GC(parent);
        int cyLine;
        try{
        	cyLine = gc.textExtent("Wq").y; //$NON-NLS-1$
        }
        finally{
        	gc.dispose();
        }
        int cy = (int)((double)cyLine * xLines);
        
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        Layout layout = parent.getLayout();
        if(layout instanceof GridLayout) {
            gd.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
        }
        gd.heightHint = cy;
        lbl.setLayoutData(gd);
    }


}
