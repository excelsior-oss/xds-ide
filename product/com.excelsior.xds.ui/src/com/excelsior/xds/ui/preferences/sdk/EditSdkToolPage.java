package com.excelsior.xds.ui.preferences.sdk;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.ide.IDEEncoding;

import com.excelsior.xds.core.help.IXdsHelpContextIds;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkTool;
import com.excelsior.xds.core.text.TextEncoding;
import com.excelsior.xds.ui.commons.controls.LocationSelector;
import com.excelsior.xds.ui.commons.utils.HelpUtils;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.internal.nls.Messages;

public class EditSdkToolPage extends WizardPage {

    public static final String ID = "com.excelsior.xds.ui.preferences.sdk.EditSdkToolPage"; //$NON-NLS-1$

    private SdkTool editedTool;
    private Sdk  sdk;
    private List<String> menuGroups;

    private Text textSdkToolName;
    private Text textSdkToolLocation;
    private Text textExtensions;
    private Button btnRunOnFiles;
    private Combo cboxAvailability;
    private Text textMenuItem;
    private Text textInactiveMenuItem;
    private Combo cboxEncoding;

    private ArrayList<Control> firstColumn;

    ArrayList<ToolTab> tabs;

    /**
     * Create the wizard.
     * @param editedTool 
     */
    public EditSdkToolPage(SdkTool editedTool, Sdk sdk, List<String> menuGroups) {
        super(ID);
        setTitle(Messages.EditSdkToolDialog_Header);
        setDescription(Messages.EditSdkToolDialog_Description);
        this.editedTool = editedTool;
        this.sdk = sdk;
        this.menuGroups = menuGroups;
    }

    private Text mkLine( Composite parent, String labelTxt, String buttonTxt, Button[] button
            , boolean enableCommonAlignment ) 
    {
        Label label = SWTFactory.createLabel(parent, labelTxt, 1);
        if (enableCommonAlignment)
            firstColumn.add(label);
        Text txt = SWTFactory.createSingleText(parent,  buttonTxt == null ? 2 : 1);
        if (buttonTxt != null) {
            button[0] = SWTFactory.createPushButton(parent, buttonTxt, null);
        }
        return txt;
    }

    /**
     * Create contents of the wizard.
     * @param parent
     */
    public void createControl(Composite parent) {

        HelpUtils.setHelp(parent, IXdsHelpContextIds.EDIT_SDK_TOOL_DLG);

        initializeDialogUnits(parent);
        Composite container = new Composite(parent, SWT.NULL);
        setControl(container);

        container.setLayout(new GridLayout(3, false));

        firstColumn = new ArrayList<Control>();

        Button[] button = {null};

        // Name: [                           ]
        textSdkToolName = mkLine(container, Messages.EditSdkToolDialog_Name+':', null, button, true);
        textSdkToolName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String sdkToolName = textSdkToolName.getText().trim();
                editedTool.setToolName(sdkToolName);
                validatePage();
            }
        });

        // Location: [           ] [Browse...]
        textSdkToolLocation = mkLine(container, Messages.EditSdkToolDialog_Location+':', Messages.Common_Browse, button, true);
        textSdkToolLocation.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String location = textSdkToolLocation.getText().trim();
                editedTool.setLocation(location);
                validatePage();
            }
        });
        button[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String path = textSdkToolLocation.getText().trim();
                if (path.isEmpty()) {
                    path = (new File(sdk.getCompilerExecutablePath())).getParent();
                    if (path == null) path = sdk.getSdkHomePath();
                }
                String exePath = SWTFactory.browseFile( getShell(), false
                        , Messages.EditSdkToolDialog_LocationBrowseText
                        , new String[]{"*.exe", "*.bat"}, path ); //$NON-NLS-1$  //$NON-NLS-2$
                        if (exePath != null) {
                            textSdkToolLocation.setText(exePath);
                        }
            }
        });

        // - Menu item text ---
        //
        SWTFactory.createVerticalSpacer(container, 0.2);
        Group group  = SWTFactory.createGroup(container, Messages.EditSdkToolDialog_ToolMenuSettings, 3, 3, GridData.FILL_HORIZONTAL);
        SWTFactory.createVerticalSpacer(container, 0.2);

        // Enabled menu item text: [           ] [Variables...]
        textMenuItem = mkLine(group, Messages.EditSdkToolDialog_MenuItem+':', Messages.Common_Variables, button, false);
        textMenuItem.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String caption = textMenuItem.getText().trim();
                editedTool.setMenuItem(caption);
                validatePage();
            }
        });
        button[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String s = selectVar();
                if (s != null) {
                    textMenuItem.insert(s);
                }
            }
        });

        // Disabled menu item text: [           ] [Variables...]
        textInactiveMenuItem = mkLine(group, Messages.EditSdkToolDialog_InactiveMenuItem+':', Messages.Common_Variables, button, false);
        textInactiveMenuItem.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String caption = textInactiveMenuItem.getText().trim();
                editedTool.setInactiveMenuItem(caption);
                validatePage();
            }
        });
        button[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String s = selectVar();
                if (s != null) {
                    textInactiveMenuItem.insert(s);
                }
            }
        });

        // Menu group: [          [V]]
        SWTFactory.createLabel(group, Messages.EditSdkToolDialog_MenuGroup+':', 1);
        final Combo cmbGroups = SWTFactory.createCombo(group, 2, SWT.DROP_DOWN | SWT.READ_ONLY);
        { // fill the list:
            int sel = 0;
            cmbGroups.add(Messages.EditSdkToolPage_NoGroup);
            for (String grp : menuGroups) {
                cmbGroups.add(grp);
                if (grp.equals(editedTool.getMenuGroup())) {
                    sel = cmbGroups.getItemCount()-1;
                }
            }
            cmbGroups.select(sel);
        }
        cmbGroups.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int i = cmbGroups.getSelectionIndex();
                editedTool.setMenuGroup(i == 0 ? "" : menuGroups.get(i-1)); //$NON-NLS-1$
            }
        });
        
        SWTFactory.createVerticalSpacer(group, 0.2);
 
        
        // [x] Applicability conditions---
        group = SWTFactory.createGroup(container, Messages.EditSdkToolDialog_ToolLaunchSettings, 3, 3, GridData.FILL_HORIZONTAL);
        SWTFactory.createVerticalSpacer(container, 0.2);

        // [v] Available for files with extensions: [                            ]
        btnRunOnFiles = SWTFactory.createCheckbox(group, Messages.EditSdkToolDialog_AvailableForFileExtensions+':', 1);
        btnRunOnFiles.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (btnRunOnFiles.getSelection()) {
                    String extensions = textExtensions.getText().trim();
                    editedTool.setFileExtensions(extensions);
                    textExtensions.setEnabled(true);
                } else {
                    editedTool.setFileExtensions(null);
                    textExtensions.setEnabled(false);
                }
                validatePage();
            }
        });
        textExtensions = SWTFactory.createSingleText(group,  1);
        textExtensions.addModifyListener(new ModifyListener() { 
            @Override
            public void modifyText(ModifyEvent e) {
                String extensions = textExtensions.getText().trim();
                editedTool.setFileExtensions(extensions);
                validatePage();
            }
        });

        // Available for projects based on: [        ][v]
                SWTFactory.createVerticalSpacer(group, 0);
        SWTFactory.createLabel(group, Messages.EditSdkToolDialog_AvailableForProjectsBasedOn+':', 1);
        cboxAvailability = SWTFactory.createCombo(group,  2, SWT.DROP_DOWN | SWT.READ_ONLY);
        cboxAvailability.add(Messages.EditSdkToolDialog_AvailableForProjectsBasedOn_AnySourceRoot);
        cboxAvailability.add(Messages.EditSdkToolDialog_AvailableForProjectsBasedOn_AnySourceRootIndividualSettings);
        cboxAvailability.add(Messages.EditSdkToolDialog_AvailableForProjectsBasedOn_ProjectFile);
        cboxAvailability.add(Messages.EditSdkToolDialog_AvailableForProjectsBasedOn_MainModule);
        cboxAvailability.addSelectionListener(new SelectionListener() {
            @Override public void widgetSelected(SelectionEvent e) {
                handleAvailabilitySelection();
            }
            @Override public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        // Tool settings | Tool settings for project file | Tool settings for main module
        SWTFactory.createVerticalSpacer(group, 0);
        TabFolder tabFolder = new TabFolder(group, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        tabFolder.setLayoutData(gd);

        tabs = new ArrayList<ToolTab>();
        tabs.add(new ToolTab(tabFolder, Messages.EditSdkToolDialog_ToolsSettings_ProjectFile,   SdkTool.SourceRootSettingsType.PRJ_FILE));
        tabs.add(new ToolTab(tabFolder, Messages.EditSdkToolDialog_ToolsSettings_MainModule,    SdkTool.SourceRootSettingsType.MAIN_MODULE));
        tabs.add(new ToolTab(tabFolder, Messages.EditSdkToolDialog_ToolsSettings_AnySourceRoot, SdkTool.SourceRootSettingsType.ANY_TYPE));

        // Output encoding: [        ][v]
        SWTFactory.createVerticalSpacer(group, 0);
        SWTFactory.createLabel(group, Messages.EditSdkToolPage_OutputEncoding+':', 1);
        cboxEncoding = SWTFactory.createCombo(group,  2, SWT.NONE);
        cboxEncoding.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                editedTool.setPropertyValue(SdkTool.Property.CONSOLE_CODEPAGE, cboxEncoding.getText().trim());
                validatePage();
            }
        });

        // Set same width for all labels in 1st column: 
        {
            int w = 1;
            for (Control ctr : firstColumn) {
                int ww = ctr.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
                if (ww > w) w = ww;
            }
            for (Control ctr : firstColumn) {
                ((GridData)ctr.getLayoutData()).widthHint = w;
            }
            firstColumn = null;
        }

        initializeControls();
        validatePage();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void initializeControls() {
        textSdkToolName.setText(editedTool.getToolName());
        textSdkToolLocation.setText(editedTool.getLocation());
        textMenuItem.setText(editedTool.getMenuItem());
        textInactiveMenuItem.setText(editedTool.getInactiveMenuItem());
        boolean isFileExtensionsEnabled = !StringUtils.isBlank(editedTool.getFileExtensions());
        btnRunOnFiles.setSelection( isFileExtensionsEnabled );
        if (isFileExtensionsEnabled) {
            textExtensions.setText(editedTool.getFileExtensions());
        }
        textExtensions.setEnabled(isFileExtensionsEnabled);
        int idx = 0;
        switch (editedTool.getSourceRoot()) {
        case PRJ_FILE:              idx = 2; break;
        case MAIN_MODULE:           idx = 3; break;
        case ANY_TYPE_OWN_SETTINGS: idx = 1; break;
		default:
			break;
        }
        cboxAvailability.select(idx);
        handleAvailabilitySelection();
        for (ToolTab t : tabs) {
            t.initializeControls();
        }
        // Encoding selector:
            List encodings = IDEEncoding.getIDEEncodings();
            addEncIfNeed(encodings, "Cp1251"); //$NON-NLS-1$
            addEncIfNeed(encodings, "Cp866"); //$NON-NLS-1$

            String curEnc = editedTool.getPropertyValue(SdkTool.Property.CONSOLE_CODEPAGE);
            if (!TextEncoding.isCodepageSupported(curEnc)) {
                curEnc = WorkbenchEncoding.getWorkbenchDefaultEncoding();
            }

            String[] encodingStrings = new String[encodings.size()];
            encodings.toArray(encodingStrings);

            cboxEncoding.setItems(encodingStrings);
            cboxEncoding.setText(curEnc);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void addEncIfNeed(List encodings, String cp) {
        try {
            Charset cs = Charset.forName(cp);
            for (Object o : encodings) {
                try {
                    Charset cs2 = Charset.forName(o.toString());
                    if (cs2.equals(cs)) {
                        return; // The same encoding exists in the list (may be with some alias)
                    }
                }catch (Exception e) {} // wtf?
            }
            encodings.add(0, cp);
        } catch (Exception e) {} // not supported - skip
    }


    private void handleAvailabilitySelection() {
        SdkTool.SourceRootSettingsType tabs2show[][] = {
                {SdkTool.SourceRootSettingsType.ANY_TYPE}, 
                {SdkTool.SourceRootSettingsType.PRJ_FILE, SdkTool.SourceRootSettingsType.MAIN_MODULE}, 
                {SdkTool.SourceRootSettingsType.PRJ_FILE}, 
                {SdkTool.SourceRootSettingsType.MAIN_MODULE}
        };
        cboxAvailability.getSelectionIndex();
        int idx = cboxAvailability.getSelectionIndex();
        if (idx>=0 && idx < 4) {
            for (ToolTab t : tabs) {
                boolean on = false;
                for (int i = 0; i < tabs2show[idx].length; ++i) {
                    on |= (t.getMode() == tabs2show[idx][i]);
                }
                t.showTab(false);
                t.showTab(on);
            }
            editedTool.setSourceRoot(
                    new SdkTool.SourceRoot[]{ SdkTool.SourceRoot.ANY_TYPE 
                            , SdkTool.SourceRoot.ANY_TYPE_OWN_SETTINGS
                            , SdkTool.SourceRoot.PRJ_FILE
                            , SdkTool.SourceRoot.MAIN_MODULE
                    } [idx]);
        }
    }

    private String selectVar() {
        StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
        dialog.addVariableFilter(new StringVariableSelectionDialog.VariableFilter() {
            @Override
            public boolean isFiltered(IDynamicVariable var) {
                return !var.getName().startsWith("xds_"); //$NON-NLS-1$
            }
        });
        dialog.open();
        return dialog.getVariableExpression();
    }

    public static String validateTool(SdkTool toolToValidate, int errType[]) {
        if (toolToValidate.isSeparator()) {
            return null;
        }
        errType[0] = ERROR;
        String err = null;
        try {
            String s = toolToValidate.getToolName();
            if (s.isEmpty()) {
                errType[0] = WARNING;
                throw new Exception(Messages.EditSdkToolPage_EnterToolName);
            }

            s = toolToValidate.getLocation();
            if (s.isEmpty()) {
                errType[0] = WARNING;
                throw new Exception(Messages.EditSdkToolPage_EnterToolLocation);
            } else if (!(new File(s).isFile())) {
                throw new Exception(Messages.EditSdkToolPage_InvalidLocation);
            }

            if (!TextEncoding.isCodepageSupported(toolToValidate.getPropertyValue(SdkTool.Property.CONSOLE_CODEPAGE))) {
                throw new Exception(Messages.EditSdkToolPage_InvalidOutputEncoding);
            }

        } catch (Exception e) {
            err = e.getMessage();
        }

        return err;
    }


    protected boolean validatePage() {
        int errType[] = new int[]{ERROR};
        String err = validateTool(editedTool, errType);
        if (err == null) {
            if (btnRunOnFiles.getSelection()) {
                if (StringUtils.isBlank(editedTool.getFileExtensions())) {
                    errType[0] = WARNING;
                    err = Messages.EditSdkToolPage_EnterExtensionOrList;
                }
            }
        }

        setMessage(err, errType[0]);

        setPageComplete(err == null);
        return err == null;
    }

    @Override
    public boolean isPageComplete() {
        return !isCurrentPage() || super.isPageComplete();
    }


    private class ToolTab extends Composite implements Listener {
        private TabFolder tabFolder;
        private String tabTitle;
        private TabItem tabItem;
        private final SdkTool.SourceRootSettingsType mode;

        private Text textArguments;
        private LocationSelector fWorkingDirSelector;

        ToolTab(TabFolder tabFolder, String tabTitle, final SdkTool.SourceRootSettingsType mode) {
            super(tabFolder, SWT.NONE);
            this.tabFolder = tabFolder;
            this.tabTitle = tabTitle;
            this.mode = mode;

            // Create controls:
                GridLayout layout = new GridLayout();
            layout.numColumns = 3;
            setLayout(layout);
            setLayoutData(new GridData(GridData.FILL_BOTH));
            SWTFactory.createVerticalSpacer(this, 0.1);

            Button button[] = new Button[1];

            // Arguments: [           ] [Variables...]
            textArguments = mkLine(this, Messages.EditSdkToolDialog_Arguments+':', Messages.Common_Variables, button, false);
            textArguments.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    String commandLine = textArguments.getText();
                    editedTool.setArguments(commandLine, mode);
                    validatePage();
                }
            });
            button[0].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    String s = selectVar();
                    if (s != null) {
                        textArguments.insert(s);
                    }
                }
            });

            // Working directory:
            fWorkingDirSelector = new LocationSelector(false, false);
            fWorkingDirSelector.createControl(this, 3, Messages.EditSdkToolDialog_WorkingDirectory);
            fWorkingDirSelector.setActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String workingDir = fWorkingDirSelector.getLocation();
                    editedTool.setWorkingDirectory(workingDir == null ? "" : workingDir, mode); //$NON-NLS-1$
                }
            });
            SWTFactory.createVerticalSpacer(this, 0.2);
        }

        public SdkTool.SourceRootSettingsType getMode() {
            return mode;
        }

        public void showTab(boolean show) {
            if (show) {
                if (tabItem == null) {
                    tabItem = new TabItem(tabFolder, SWT.NULL);
                    tabItem.setText(tabTitle);
                    tabItem.setControl(this);
                }
            } else {
                if (tabItem != null) {
                    tabItem.dispose();
                    tabItem = null;
                }
            }
        }

        public void initializeControls() {
            textArguments.setText(editedTool.getArguments(mode));
            String wd = editedTool.getWorkingDirectory(mode);
            if (StringUtils.isBlank(wd)) wd = ""; //$NON-NLS-1$
            fWorkingDirSelector.setLocations(Messages.EditSdkToolDialog_DefaultWorkingDirectory, 
                    wd,
                    wd.isEmpty());
            fWorkingDirSelector.setDefLocationIsFake(true);
        }


        @Override
        public void handleEvent(Event event) {
            //TODO
        }

    }
}
