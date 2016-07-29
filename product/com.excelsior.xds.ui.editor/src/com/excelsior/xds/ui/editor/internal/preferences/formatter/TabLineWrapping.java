package com.excelsior.xds.ui.editor.internal.preferences.formatter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

import com.excelsior.xds.parser.modula.XdsSourceType;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.commons.utils.SwtUtils;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.internal.preferences.ModifyDialog.IModifyDialogTabPage;

public class TabLineWrapping implements IModifyDialogTabPage {
    private FormatterProfile fp;
    private FormatterModifyDialog fmtDialog;
    private FormatterPreview fPreview;
    private final IDialogSettings fDialogSettings;
    
    private Text textWrappingWidth;
    private Text textDemoWidth;
    
    private static final String DEMO_WRAP_WIDTH_KEY = XdsEditorsPlugin.PLUGIN_ID + "TabLineWrapping.DEMO_WRAP_WIDTH"; //$NON-NLS-1$

    public TabLineWrapping(FormatterProfile fp, FormatterModifyDialog fmtDialog) {
        this.fp = fp;
        this.fmtDialog = fmtDialog;
        fDialogSettings = XdsEditorsPlugin.getDefault().getDialogSettings();
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
        layout = new GridLayout(2, true);
        compR.setLayout(layout);
        compR.setFont(font);
        GridData gd = new GridData(GridData.FILL_BOTH);
        compR.setLayoutData(gd);

        // --General settings--------
        Group grp = SWTFactory.createGroup(compL, Messages.TabLineWrapping_GeneralSettings, 2, 1, GridData.FILL_HORIZONTAL);
        
        // Maximum line width: [  123]
        int textWidthHint = SwtUtils.getTextWidth(grp, "9999999999"); //$NON-NLS-1$
        SWTFactory.createLabel(grp, Messages.TabLineWrapping_MaxLineWidth + ':', 1);
        textWrappingWidth = SWTFactory.createSingleText(grp,  1);
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
        gd.widthHint = textWidthHint;
        textWrappingWidth.setLayoutData(gd);
        textWrappingWidth.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validate(false);
            }
        });
        
        configurePreview(compR, 2);

        // Set line width for preview window: [  123]
        SWTFactory.createLabel(compR, Messages.TabLineWrapping_SetWidthToPreview + ':', 1);
        textDemoWidth = SWTFactory.createSingleText(compR,  1);
        gd = new GridData(GridData.BEGINNING);
        gd.widthHint = textWidthHint;
        textDemoWidth.setLayoutData(gd);


        textDemoWidth.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validate(true);
            }
        });

        // Init values:
        
        textWrappingWidth.setText("" + fp.getWrappingWidth()); //$NON-NLS-1$

        int demoWidth = 40;
        try {
            demoWidth = fDialogSettings.getInt(DEMO_WRAP_WIDTH_KEY);
        } catch (Exception e) {}
        textDemoWidth.setText("" + demoWidth); //$NON-NLS-1$
        
        updatePreviewWidth(demoWidth);

        return sashForm;
    }

    
    private void configurePreview(Composite composite, int numColumns) {
        SWTFactory.createLabel(composite, Messages.IndentationTabPage_Preview+':', numColumns);
        fPreview = new FormatterPreview(composite, "indent_preview.mod", XdsSourceType.Modula); //$NON-NLS-1$

        final GridData gd = new GridData(GridData.FILL_VERTICAL | GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = numColumns;
        gd.widthHint = 0;
        gd.heightHint = 0;
        fPreview.getTextWidget().setLayoutData(gd);
        fPreview.turnOnMarginPainter();
        fPreview.setProfile(fp);
    }
    
    @Override
    public void makeVisible() {
        fPreview.setProfile(fp);
    }

    @Override
    public void setInitialFocus() {
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

    private final static Status fOk= new Status(IStatus.OK, XdsEditorsPlugin.PLUGIN_ID, ""); //$NON-NLS-1$
    private final static Status fBadValue= new Status(IStatus.ERROR, XdsEditorsPlugin.PLUGIN_ID, Messages.TabLineWrapping_InvalidValue);

    private void validate(boolean repaintPreview) {
        int valDemo = getValue(textDemoWidth);
        int valFp = getValue(textWrappingWidth);
        if (valDemo >= 0) {
            fDialogSettings.put(DEMO_WRAP_WIDTH_KEY, valDemo);
            if (repaintPreview) {
                updatePreviewWidth(valDemo);
            }
        }
        if (valFp >= 0) {
            fp.setWrappingWidth(valFp);
        }
        fmtDialog.updateStatus(valDemo>=0 && valFp>=0 ? fOk : fBadValue);
    }
    
    private int getValue(Text txt) {
        int val = -1;
        String s = txt.getText().trim();
        if (!s.isEmpty()) {
            val = 0;
            for (int i=0; i<s.length(); ++i) {
                char ch = s.charAt(i);
                if (ch >= '0' && ch <= '9') {
                    val = val * 10 + ch - '0';
                } else {
                    val = -1;
                    break;
                }
            }
        }
        return (val >= 0 && val <= 9999) ? val : -1;
    }
    
    private void updatePreviewWidth(int width) {
        FormatterProfile fp1 = new FormatterProfile();
        fp1.copyFrom(fp);
        fp1.setWrappingWidth(width);
        fPreview.setProfile(fp1);
    }

}
