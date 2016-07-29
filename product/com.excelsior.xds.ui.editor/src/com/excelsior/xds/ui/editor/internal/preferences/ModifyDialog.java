package com.excelsior.xds.ui.editor.internal.preferences;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.StatusDialog;

import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.internal.nls.Messages;


public abstract class ModifyDialog extends StatusDialog {

    /**
     * The keys to retrieve the preferred area from the dialog settings.
     */
    private static final String DS_KEY_PREFERRED_WIDTH= "modify_dialog.preferred_width"; //$NON-NLS-1$
    private static final String DS_KEY_PREFERRED_HEIGHT= "modify_dialog.preferred_height"; //$NON-NLS-1$
    private static final String DS_KEY_PREFERRED_X= "modify_dialog.preferred_x"; //$NON-NLS-1$
    private static final String DS_KEY_PREFERRED_Y= "modify_dialog.preferred_y"; //$NON-NLS-1$


    /**
     * The key to store the number (beginning at 0) of the tab page which had the
     * focus last time.
     */
    private static final String DS_KEY_LAST_FOCUS= "modify_dialog.last_focus"; //$NON-NLS-1$

    private final String fKeyPreferredWidth;
    private final String fKeyPreferredHight;
    private final String fKeyPreferredX;
    private final String fKeyPreferredY;
    private final String fKeyLastFocus;
    private final boolean fNewProfile;
    private final List<IModifyDialogTabPage> fTabPages;
    private final IDialogSettings fDialogSettings;
    private Text txtProfileName;
    private TabFolder fTabFolder;

    public ModifyDialog(Shell parentShell, String title, String dialogPreferencesKey, boolean fNewProfile) {
        super(parentShell);

        this.fNewProfile = fNewProfile;

        String plg = XdsEditorsPlugin.PLUGIN_ID;
        fKeyPreferredWidth= plg + dialogPreferencesKey + DS_KEY_PREFERRED_WIDTH;
        fKeyPreferredHight= plg + dialogPreferencesKey + DS_KEY_PREFERRED_HEIGHT;
        fKeyPreferredX= plg + dialogPreferencesKey + DS_KEY_PREFERRED_X;
        fKeyPreferredY= plg + dialogPreferencesKey + DS_KEY_PREFERRED_Y;
        fKeyLastFocus= plg + dialogPreferencesKey + DS_KEY_LAST_FOCUS;

        setTitle(title);
        setStatusLineAboveButtons(false);
        fTabPages= new ArrayList<IModifyDialogTabPage>();
        fDialogSettings= XdsEditorsPlugin.getDefault().getDialogSettings();
    }

    /*
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     * @since 3.4
     */
    @Override
    protected boolean isResizable() {
        return true;
    }

    protected abstract void addPages();

    @Override
    public void create() {
        super.create();
        int lastFocusNr= 0;
        try {
            lastFocusNr= fDialogSettings.getInt(fKeyLastFocus);
            if (lastFocusNr < 0) lastFocusNr= 0;
            if (lastFocusNr > fTabPages.size() - 1) lastFocusNr= fTabPages.size() - 1;
        } catch (NumberFormatException x) {
            lastFocusNr= 0;
        }

        if (!fNewProfile) {
            fTabFolder.setSelection(lastFocusNr);
            ((IModifyDialogTabPage)fTabFolder.getSelection()[0].getData()).setInitialFocus();
        }
    }



    @Override
    protected Control createDialogArea(Composite parent) {

        final Composite composite= (Composite)super.createDialogArea(parent);

        Composite nameComposite= new Composite(composite, SWT.NONE);
        nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        nameComposite.setLayout(new GridLayout(3, false));

        SWTFactory.createLabel(nameComposite, Messages.ModifyDialog_ProfileName + ':', 1);
        txtProfileName = SWTFactory.createSingleText(nameComposite, 1);
        SWTFactory.createPushButton(nameComposite, Messages.ModifyDialog_Export, null).addSelectionListener( new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent e) {
                handleExportButton();
            }
        });

        fTabFolder = new TabFolder(composite, SWT.NONE);
        fTabFolder.setFont(composite.getFont());
        fTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        addPages();

        applyDialogFont(composite);

        fTabFolder.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {}
            public void widgetSelected(SelectionEvent e) {
                final TabItem tabItem= (TabItem)e.item;
                final IModifyDialogTabPage page= (IModifyDialogTabPage)tabItem.getData();
                fDialogSettings.put(fKeyLastFocus, fTabPages.indexOf(page));
                page.makeVisible();
            }
        });

        //PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, getHelpContextId());

        return composite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#getInitialSize()
     */
    @Override
    protected Point getInitialSize() {
        Point initialSize= super.getInitialSize();
        try {
            int lastWidth= fDialogSettings.getInt(fKeyPreferredWidth);
            if (initialSize.x > lastWidth)
                lastWidth= initialSize.x;
            int lastHeight= fDialogSettings.getInt(fKeyPreferredHight);
            if (initialSize.y > lastHeight)
                lastHeight= initialSize.y;
            return new Point(lastWidth, lastHeight);
        } catch (NumberFormatException ex) {
        }
        return initialSize;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
     */
    @Override
    protected Point getInitialLocation(Point initialSize) {
        try {
            return new Point(fDialogSettings.getInt(fKeyPreferredX), fDialogSettings.getInt(fKeyPreferredY));
        } catch (NumberFormatException ex) {
            return super.getInitialLocation(initialSize);
        }
    }

    @Override
    public boolean close() {
        final Rectangle shell= getShell().getBounds();

        fDialogSettings.put(fKeyPreferredWidth, shell.width);
        fDialogSettings.put(fKeyPreferredHight, shell.height);
        fDialogSettings.put(fKeyPreferredX, shell.x);
        fDialogSettings.put(fKeyPreferredY, shell.y);

        return super.close();
    }

    protected final void addTabPage(String title, IModifyDialogTabPage tabPage) {
        final TabItem tabItem= new TabItem(fTabFolder, SWT.NONE);
        applyDialogFont(tabItem.getControl());
        tabItem.setText(title);
        tabItem.setData(tabPage);
        tabItem.setControl(tabPage.createContents(fTabFolder));
        fTabPages.add(tabPage);
    }
    
    abstract protected void handleExportButton();
    
    protected Text getTxtProfileName() {
        return txtProfileName;
    }

    public interface IModifyDialogTabPage {

        /**
         * Create the contents of this tab page.
         *
         * @param parent the parent composite
         * @return created content control
         */
        public Composite createContents(Composite parent);

        /**
         * This is called when the page becomes visible.
         * Common tasks to do include:
         * <ul><li>Updating the preview.</li>
         * <li>Setting the focus</li>
         * </ul>
         */
        public void makeVisible();

        /**
         * Each tab page should remember where its last focus was, and reset it
         * correctly within this method. This method is only called after
         * initialization on the first tab page to be displayed in order to restore
         * the focus of the last session.
         */
        public void setInitialFocus();

    }
}
