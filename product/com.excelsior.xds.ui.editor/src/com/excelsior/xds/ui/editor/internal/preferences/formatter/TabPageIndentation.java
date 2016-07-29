package com.excelsior.xds.ui.editor.internal.preferences.formatter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;

import com.excelsior.xds.parser.modula.XdsSourceType;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.internal.preferences.ModifyDialog.IModifyDialogTabPage;

public class TabPageIndentation implements IModifyDialogTabPage {
    private static final int IDX_SPACES_ONLY = 0;
    private static final int IDX_USE_TABS = 1;
    private static final int IDX_MIXED = 2;

    private int prefHeight;
    private FormatterProfile fp;
    private Combo cmbTabPolicy;
    private FormatterPreview fPreview;


    public TabPageIndentation(FormatterProfile fp) {
        this.fp = fp;
    }

    @Override
    public Composite createContents(Composite parent) {
        Font font = parent.getFont();
        
        final SashForm sashForm= new SashForm(parent, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite scrollContainer = new Composite(sashForm, SWT.NONE);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        scrollContainer.setLayoutData(gridData);

        GridLayout layout= new GridLayout(2, false);
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        layout.horizontalSpacing= 0;
        layout.verticalSpacing= 0;
        scrollContainer.setLayout(layout);

        ScrolledComposite scroll= new ScrolledComposite(scrollContainer, SWT.V_SCROLL | SWT.H_SCROLL);
        scroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        scroll.setExpandHorizontal(true);
        scroll.setExpandVertical(true);

        final Composite settingsContainer= new Composite(scroll, SWT.NONE);
        settingsContainer.setFont(sashForm.getFont());
        settingsContainer.setLayout(new PageLayout(scroll, 400, 400));
        settingsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        scroll.setContent(settingsContainer);
        settingsContainer.setSize(settingsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        scroll.pack(true);
        scroll.addControlListener(new ControlListener() {
            public void controlMoved(ControlEvent e) {
            }
            public void controlResized(ControlEvent e) {
                settingsContainer.setSize(settingsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            }
        });


        Composite compL= new Composite(settingsContainer, SWT.NONE);
        compL.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        layout= new GridLayout(1, false);
        compL.setLayout(layout);
        
        Composite compR = new Composite(sashForm, SWT.NONE);
        layout = new GridLayout(1, true);
        compR.setLayout(layout);
        compR.setFont(font);
        GridData gd = new GridData(GridData.FILL_BOTH);
        compR.setLayoutData(gd);

        { // deterine preferred items height 'prefHeight'
            Combo cmb = SWTFactory.createCombo(compL, 1, SWT.DROP_DOWN | SWT.READ_ONLY, GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
            prefHeight = cmb.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
            cmb.dispose();
        }


        // --General settings--------
        Group grp = SWTFactory.createGroup(compL, Messages.IndentationTabPage_GeneralSettings, 2, 1, GridData.FILL_HORIZONTAL);
        
        // Tab policy:
        SWTFactory.createLabel(grp, Messages.IndentationTabPage_TabPolicy + ':', 1);
        cmbTabPolicy = SWTFactory.createCombo(grp, 1, SWT.DROP_DOWN | SWT.READ_ONLY, GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
        cmbTabPolicy.add(Messages.IndentationTabPage_SpacesOnly);
        cmbTabPolicy.add(Messages.IndentationTabPage_UseTabs);
        cmbTabPolicy.add(Messages.IndentationTabPage_Mixed);
        int tabm = fp.getValue(FormatterProfile.IndentSetting.TabMode);
        cmbTabPolicy.select(tabm == FormatterProfile.TABMODE_MIXED ? IDX_MIXED : 
                            tabm == FormatterProfile.TABMODE_TABS ? IDX_USE_TABS : IDX_SPACES_ONLY);
        cmbTabPolicy.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                switch (cmbTabPolicy.getSelectionIndex()) {
                case IDX_SPACES_ONLY: fp.setValue(FormatterProfile.IndentSetting.TabMode, FormatterProfile.TABMODE_SPACES); break;
                case IDX_USE_TABS: fp.setValue(FormatterProfile.IndentSetting.TabMode, FormatterProfile.TABMODE_TABS); break;
                case IDX_MIXED: fp.setValue(FormatterProfile.IndentSetting.TabMode, FormatterProfile.TABMODE_MIXED); break;
                }
                fPreview.setProfile(fp);
            }
        });
        // Use spaces to indent wrapped lines
        Button __tmp_disable = (Button)addSettingControl(grp, FormatterProfile.IndentSetting.UseSpacesToIndentWrappedLines, Messages.IndentationTabPage_SpacesToWrappedLines);
        __tmp_disable.setEnabled(false); //XXX temporary disabled not inplemented control
        // Indentation size:
        addSettingControl(grp, FormatterProfile.IndentSetting.IndentSize, Messages.IndentationTabPage_IndentSize + ':');
        // Tab size:
        addSettingControl(grp, FormatterProfile.IndentSetting.TabSize, Messages.IndentationTabPage_TabSize + ':');
        
        // --Indent--------
        grp = SWTFactory.createGroup(compL, Messages.IndentationTabPage_Indent, 2, 1, GridData.FILL_HORIZONTAL);
        addSettingControl(grp, FormatterProfile.IndentSetting.IndentDeclInModule, Messages.IndentationTabPage_DeclWithinModule);
        addSettingControl(grp, FormatterProfile.IndentSetting.IndentDeclInProc, Messages.IndentationTabPage_DeclWithinProc);
        addSettingControl(grp, FormatterProfile.IndentSetting.IndentDeclInVCT, Messages.IndentationTabPage_DeclWithinVCT);
        addSettingControl(grp, FormatterProfile.IndentSetting.IndentDeclOfLocalProc, Messages.IndentationTabPage_DeclOfLocalProcs + ':');
        addSettingControl(grp, FormatterProfile.IndentSetting.IndentDeclOfLocalMods, Messages.IndentationTabPage_DeclOfLocalMods + ':');
        addSettingControl(grp, FormatterProfile.IndentSetting.IndentDeclOfRecFields, Messages.IndentationTabPage_DeclOfRecFields);
        addSettingControl(grp, FormatterProfile.IndentSetting.IndentStatements, Messages.IndentationTabPage_Statements);
        addSettingControl(grp, FormatterProfile.IndentSetting.IndentInCaseBody, Messages.IndentationTabPage_StatementsInCase);
        addSettingControl(grp, FormatterProfile.IndentSetting.IndentInCaseAlternative, Messages.IndentationTabPage_StatementsInCaseAlt + ':');

        configurePreview(compR, 1);

        return sashForm;
    }
    
    private Control addSettingControl(Composite parent, FormatterProfile.IndentSetting bs, String label) {
        if (bs.isRange()) {
            Button cbox = null;
            if (bs.isRangeWithCheckbox()) {
                cbox = SWTFactory.createCheckbox(parent, label, 1);
            } else {
                SWTFactory.createLabel(parent, label, 1);
            }
            Combo cmb = SWTFactory.createCombo(parent, 1, SWT.DROP_DOWN | SWT.READ_ONLY, GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
            for (int i=bs.getMinVal(); i<=bs.getMaxVal(); ++i) {
                cmb.add(""+i); //$NON-NLS-1$
            }
            cmb.select(fp.getValueForDialog(bs) - bs.getMinVal());
            SettingSelectionListener ssl = new SettingSelectionListener(cmb, cbox, bs);
            cmb.addSelectionListener(ssl);
            if (cbox != null) {
                boolean unch = fp.getRangeCheckboxUncheckedState(bs);
                cbox.setSelection(!unch);
                cmb.setEnabled(!unch);
                cbox.addSelectionListener(ssl);
            }
            return cmb;
        } else {
            Button cb = SWTFactory.createCheckbox(parent, label, 2);
            cb.setSelection(fp.getAsBoolean(bs));
            cb.addSelectionListener(new SettingSelectionListener(cb, null, bs));
            
            GridData gd = (GridData)cb.getLayoutData();
            gd.heightHint = prefHeight;
            cb.setLayoutData(gd);
            
            return cb;
        }
    }
    
    private void configurePreview(Composite composite, int numColumns) {
        SWTFactory.createLabel(composite, Messages.IndentationTabPage_Preview+':', numColumns);
        fPreview = new FormatterPreview(composite, "indent_preview.mod", XdsSourceType.Modula); //$NON-NLS-1$

        final GridData gd = new GridData(GridData.FILL_VERTICAL | GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = numColumns;
        gd.widthHint = 0;
        gd.heightHint = 0;
        fPreview.getTextWidget().setLayoutData(gd);
        fPreview.setProfile(fp);
    }
    
    @Override
    public void makeVisible() {
        fPreview.setProfile(fp);
    }

    @Override
    public void setInitialFocus() {
        cmbTabPolicy.setFocus();
    }


    private class SettingSelectionListener extends SelectionAdapter {
        private Control ctrl;
        private Button rangeChbox;
        private FormatterProfile.IndentSetting bs;
        
        public SettingSelectionListener(Control ctrl, Button rangeChbox, FormatterProfile.IndentSetting bs) {
            this.ctrl = ctrl;
            this.rangeChbox = rangeChbox;
            this.bs = bs;
        }
        public void widgetSelected(SelectionEvent e) {
            if (ctrl instanceof Button) {
                fp.setBoolean(bs, ((Button)ctrl).getSelection());
            } else if (ctrl instanceof Combo) {
                if (rangeChbox != null && !rangeChbox.getSelection()) {
                    fp.setValue(bs, ((Combo)ctrl).getSelectionIndex() + bs.getMinVal(), true);
                    ctrl.setEnabled(false);
                } else {
                    fp.setValue(bs, ((Combo)ctrl).getSelectionIndex() + bs.getMinVal(), false);
                    ctrl.setEnabled(true);
                }
            }
            fPreview.setProfile(fp);
        }
    }
    
    private static class PageLayout extends Layout {

        private final ScrolledComposite fContainer;
        private final int fMinimalWidth;
        private final int fMinimalHight;

        private PageLayout(ScrolledComposite container, int minimalWidth, int minimalHight) {
            fContainer= container;
            fMinimalWidth= minimalWidth;
            fMinimalHight= minimalHight;
        }

        @Override
        public Point computeSize(Composite composite, int wHint, int hHint, boolean force) {
            if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
                return new Point(wHint, hHint);
            }

            int x = fMinimalWidth;
            int y = fMinimalHight;
            Control[] children = composite.getChildren();
            for (int i = 0; i < children.length; i++) {
                Point size = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
                x = Math.max(x, size.x);
                y = Math.max(y, size.y);
            }

            Rectangle area= fContainer.getClientArea();
            if (area.width > x) {
                fContainer.setExpandHorizontal(true);
            } else {
                fContainer.setExpandHorizontal(false);
            }

            if (area.height > y) {
                fContainer.setExpandVertical(true);
            } else {
                fContainer.setExpandVertical(false);
            }

            if (wHint != SWT.DEFAULT) {
                x = wHint;
            }
            if (hHint != SWT.DEFAULT) {
                y = hHint;
            }

            return new Point(x, y);
        }

        @Override
        public void layout(Composite composite, boolean force) {
            Rectangle rect = composite.getClientArea();
            Control[] children = composite.getChildren();
            for (int i = 0; i < children.length; i++) {
                children[i].setSize(rect.width, rect.height);
            }
        }
    }

}
