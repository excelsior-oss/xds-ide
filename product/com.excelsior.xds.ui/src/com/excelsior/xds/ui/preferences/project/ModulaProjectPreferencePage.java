package com.excelsior.xds.ui.preferences.project;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;

import com.excelsior.xds.core.help.IXdsHelpContextIds;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.core.model.XdsProjectConfiguration;
import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectType;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkManager;
import com.excelsior.xds.core.utils.XdsFileUtils;
import com.excelsior.xds.ui.commons.controls.LocationSelector;
import com.excelsior.xds.ui.commons.utils.HelpUtils;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.dialogs.SelectModulaSourceFileDialog;
import com.excelsior.xds.ui.internal.nls.Messages;
import com.excelsior.xds.ui.sdk.ProjectSdkPanel;

/**
 * Top level Modula-2 property page for projects.
 */
public class ModulaProjectPreferencePage extends PreferencePage implements IWorkbenchPropertyPage {
    public static final String ID = "com.excelsior.xds.ui.preferences.ModulaProjectPreferencePage"; //$NON-NLS-1$

    private IProject project;
    private IXdsProject xdsProject;
    private XdsProjectSettings xdsProjectSettings;

    private ProjectSdkPanel projectSdk;

    private Button rbProjectFile;
    private Text textProjectFile;
    private Button rbMainModule;
    private Text textMainModule;
    private Button btnBrowseProjectFile;
    private Button btnBrowseMainModule;
    private LocationSelector fWorkingDirSelector;
    private LocationSelector fExeSelector;

    private boolean isProjectFileMode; // else - main module
    private boolean isChanged;

    // Model:
    private String projectFile;
    private String mainModule;
    private boolean useMainModule;

    /**
     * ID for the page
     */

    /**
     * @wbp.parser.constructor
     */
    public ModulaProjectPreferencePage() {
        this(null, null);
    }

    public ModulaProjectPreferencePage(String title) {
        this(title, null);
    }

    public ModulaProjectPreferencePage(String title, ImageDescriptor image) {
        super(title, image);
        fWorkingDirSelector = new LocationSelector(false, false);
        fExeSelector = new LocationSelector(true, false);
    }

    @Override
    protected Control createContents(Composite parent) {
        final int nColumns = 3;
        initializeDialogUnits(parent);
        HelpUtils.setHelp(parent, IXdsHelpContextIds.MODULA2_PROPERTY_PAGE);

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        container.setLayout(new GridLayout(nColumns, false));

        // (o) Project file: [______________] [Browse]
        rbProjectFile = SWTFactory.createRadiobutton(container,
                Messages.ModulaProjectPreferencePage_ProjectFile+':', 1);
        rbProjectFile.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                useMainModule = !rbProjectFile.getSelection();
                setProjectFileMode(!useMainModule);
                refreshDefaultWorkingDir();
                validatePage();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        textProjectFile = SWTFactory.createSingleText(container, 1, SWT.SINGLE
                | SWT.BORDER | SWT.READ_ONLY);
        btnBrowseProjectFile = SWTFactory.createPushButton(container,
                Messages.Common_Browse, null);
        btnBrowseProjectFile.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                browseProjectFile();
            }
        });

        // ( ) Main module: [______________] [Browse]
        rbMainModule = SWTFactory.createRadiobutton(container,
                Messages.ModulaProjectPreferencePage_MainModule+':', 1);
        textMainModule = SWTFactory.createSingleText(container, 1, SWT.SINGLE
                | SWT.BORDER | SWT.READ_ONLY);
        btnBrowseMainModule = SWTFactory.createPushButton(container,
                Messages.Common_Browse, null);
        btnBrowseMainModule.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                browseMainModule();
            }
        });

        //
        SWTFactory.createSeparator(container, nColumns,
                convertHeightInCharsToPixels(1));

        // Project SDK: [___________________[V] [Configure]
        projectSdk = new ProjectSdkPanel(this, container, new int[] { 1, 1, 1 }) {
            @Override
            protected void onChanged() {
                super.onChanged();
                refreshDefaultWorkingDir();
                validatePage();
            }
        };

        // /

        Composite comp4groups = new Composite(container, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        comp4groups.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = nColumns;
        comp4groups.setLayoutData(gd);

        // Working directory:
        
        fWorkingDirSelector.createControl(comp4groups, 1, Messages.ModulaProjectPreferencePage_WorkingDirectory);
        fWorkingDirSelector.setActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validatePage();
            }
        });

        // Application executable:
        fExeSelector.createControl(comp4groups, 1, Messages.ModulaProjectPreferencePage_Executable);
        fExeSelector.setActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validatePage();
            }
        });

        performDefaults();

        return container;
    }

    @Override
    protected void performDefaults() {
        String sdk_name = xdsProjectSettings.getProjectSpecificSdkName(); // null
                                                                          // =>
                                                                          // not
                                                                          // selected
        projectSdk.initContents(sdk_name);

        projectFile = StringUtils.isEmpty(xdsProjectSettings
                .getXdsProjectFile()) ? "" : xdsProjectSettings //$NON-NLS-1$
                .getXdsProjectFile();
        textProjectFile.setText(projectFile);

        mainModule = StringUtils.isEmpty(xdsProjectSettings.getMainModule()) ? "" //$NON-NLS-1$
                : xdsProjectSettings.getMainModule();
        textMainModule.setText(mainModule);

        boolean isPrj = XdsProjectType.PROJECT_FILE.equals(xdsProjectSettings
                .getProjectType());
        useMainModule = !isPrj;
        rbProjectFile.setSelection(isPrj);
        rbMainModule.setSelection(!isPrj);
        setProjectFileMode(isPrj);

        // Work dir:
        {
            String dir = xdsProjectSettings.getXdsWorkingDirString();
            fWorkingDirSelector.setLocations("", dir != null ? dir : "", StringUtils.isBlank(dir)); //$NON-NLS-1$ //$NON-NLS-2$
            fWorkingDirSelector.setBrowseProject(project);
            refreshDefaultWorkingDir();
        }

        // Exe paths:
        {
            String autoExe;
            { // Autodetect exe path:
                // hide manual setting:
                String tmp = xdsProjectSettings.getApplicationExecutable();
                xdsProjectSettings.setApplicationExecutable(null);
                // autodetect executable as it will be detected w/o manual setting:
                XdsProjectConfiguration pc = new XdsProjectConfiguration(project);
                autoExe = pc.getExePath();
                // restore manual setting (may be null):
                xdsProjectSettings.setApplicationExecutable(tmp);
            }
            if (autoExe == null) autoExe = ""; //$NON-NLS-1$
    
            String manualExe = xdsProjectSettings.getApplicationExecutable();
            if (manualExe == null) {  
                manualExe = ""; //$NON-NLS-1$
            }
            fExeSelector.setLocations(autoExe, manualExe, StringUtils.isBlank(manualExe));
            fExeSelector.setFileBrowsePath(ProjectUtils.getProjectLocation(project));
            fExeSelector.setBrowseProject(project);
        }

        isChanged = false;
    }

    private void refreshDefaultWorkingDir() {
        String dir = ""; //$NON-NLS-1$
        if (projectFile != null && mainModule != null) { // initialization
                                                         // complete
        	File f = xdsProjectSettings.getDefaultXdsWorkingDir();
            dir = f.getAbsolutePath();
        }
        fWorkingDirSelector.setDefLocation(dir);
    }

    private void setProjectFileMode(boolean b) {
        isProjectFileMode = b;
        textProjectFile.setEnabled(b);
        textMainModule.setEnabled(!b);
        btnBrowseProjectFile.setEnabled(b);
        btnBrowseMainModule.setEnabled(!b);
    }

    private void browseProjectFile() {
        HashSet<String> exts = new HashSet<String>();
        exts.add(XdsFileUtils.XDS_PROJECT_FILE_EXTENSION);
        SelectModulaSourceFileDialog dlg = new SelectModulaSourceFileDialog(
                Messages.ModulaProjectPreferencePage_SelectPrjFile, getShell(), xdsProject.getProject(),
                exts);
        dlg.forceToShowAllItems();
        if (SelectModulaSourceFileDialog.OK == dlg.open()) {
            projectFile = dlg.getResultAsRelativePath();
            textProjectFile.setText(projectFile);
            refreshDefaultWorkingDir();
            validatePage();
        }
    }

    private void browseMainModule() {
        HashSet<String> exts = new HashSet<String>();
        exts.add(XdsFileUtils.MODULA_PROGRAM_MODULE_FILE_EXTENSION);
        exts.add(XdsFileUtils.OBERON_MODULE_FILE_EXTENSION);
        SelectModulaSourceFileDialog dlg = new SelectModulaSourceFileDialog(
                Messages.ModulaProjectPreferencePage_SelectMainModule, getShell(), xdsProject.getProject(), exts);
        dlg.forceToShowAllItems();
        if (SelectModulaSourceFileDialog.OK == dlg.open()) {
            mainModule = dlg.getResultAsRelativePath();
            textMainModule.setText(mainModule);
            refreshDefaultWorkingDir();
            validatePage();
        }
    }

    private void validatePage() {
        isChanged = true;
        
        { // refresh exe extensions:
            String exeExt = "exe"; //$NON-NLS-1$
            Sdk sdk = projectSdk.getSelectedSdk();
            if (sdk == null) {
                sdk = SdkManager.getInstance().loadSdkRegistry().getDefaultSdk();
            }
            if (sdk != null) {
                exeExt = sdk.getExecutableFileExtensions();
            }
            if (!StringUtils.isBlank(exeExt)) {
                fExeSelector.setFileBrowseExtension("." + exeExt); //$NON-NLS-1$
            }
        }

        String message = null;
        try {
            // Project SDK:
            if (!projectSdk.isValid()) {
                throw new Exception(projectSdk.getErrorMessage());
            }

            if (isProjectFileMode) {
                if (projectFile.isEmpty()) {
                    throw new Exception(Messages.ModulaProjectPreferencePage_SelectPrjFile);
                }
            } else {
                if (mainModule.isEmpty()) {
                    throw new Exception(Messages.ModulaProjectPreferencePage_SelectMainModule);
                }
            }

            // Validate executable:
            {
                String exe = fExeSelector.getLocation(); 
                if (exe != null) {
                    if (exe.isEmpty()) {
                        throw new Exception(Messages.ModulaProjectPreferencePage_EnterApplExecutable);
                    }
                }
            }

        } catch (Exception e) {
            message = e.getMessage();
        }
        setErrorMessage(message);

        setValid(message == null);
        updateApplyButton();
    }

    @Override
    protected void performApply() {
        Sdk sdk = projectSdk.getSelectedSdk();
        xdsProjectSettings.setProjectSdk(sdk);

        xdsProjectSettings
                .setProjectType(isProjectFileMode ? XdsProjectType.PROJECT_FILE
                        : XdsProjectType.MAIN_MODULE);

        if (isProjectFileMode) {
            xdsProjectSettings.setXdsProjectFile(projectFile.trim());
        } else {
            xdsProjectSettings.setMainModule(mainModule.trim());
        }

        xdsProjectSettings.setXdsWorkingDir(fWorkingDirSelector.getLocation());
        xdsProjectSettings.setApplicationExecutable(fExeSelector.getLocation());
        
        xdsProjectSettings.flush();
        
        getApplyButton().setEnabled(false);
        isChanged = false;
    }

    @Override
    public boolean performOk() {
        if (isChanged && isValid()) {
            performApply();
        }
        return true;
    }

    @Override
    // IWorkbenchPropertyPage
    public IAdaptable getElement() {
        return xdsProject.getProject();
    }

    @Override
    // IWorkbenchPropertyPage
    public void setElement(IAdaptable element) {
        project = (IProject) element.getAdapter(IProject.class);
        xdsProject = XdsModelManager.getModel().getXdsProjectBy(project);
        xdsProjectSettings = xdsProject.getXdsProjectSettings();
    }

}
