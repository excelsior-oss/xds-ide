package com.excelsior.xds.ui.project.wizard;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.excelsior.xds.core.help.IXdsHelpContextIds;
import com.excelsior.xds.core.project.NewProjectSettings;
import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.ui.commons.utils.HelpUtils;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.commons.utils.SwtUtils;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.internal.nls.Messages;
import com.excelsior.xds.ui.sdk.ProjectSdkPanel;

/**
 * The first page of the Modula-2 project creation wizard from scratch. 
 */
public class NewProjectFromScratchPage extends WizardPage {

    private static final String PAGE_NAME = "NewProjectFromScratchPage"; //$NON-NLS-1$
    private static final String PROJNAME_VAR = "$(projname)"; //$NON-NLS-1$

    private Text textProjectName;
    private Text textProjectDir;
    private Button cboxCreatePrj;
    private Text textPrjFName;
    private Text textMainName;
    private Button cboxCreateDirs;
    private Text textDirs;
    private Button cboxCreateRedFile;
    private Text textRedFile;
    private Button cboxTemplateFromSdk;
    
    private String prjDefSubfolder;  // ~"prjDir" or "", used in autoedit mode
    private String mainDefSubfolder; // ~"src" or "", used in autoedit mode

    private String projectName = "";
    private String projectRoot;
    private boolean createPrj;
    private String prjName = "";
    private boolean useTemplateFromSdk;
    private boolean createMain;
    private String mainModule;
    private boolean createRedFile;
    private boolean createDirs;
    private String dirsFromSdk = ""; //$NON-NLS-1$
    private String redFromSdk = ""; //$NON-NLS-1$

    private ProjectSdkPanel projectSdk;

    private static final String ILLEGAL_FILENAME_CHARS = "/\\*:?<>|\""; //$NON-NLS-1$
    private static final String ILLEGAL_FILEPATH_CHARS = "*:?<>|\""; //$NON-NLS-1$

    /**
     * Create the wizard.
     */
    public NewProjectFromScratchPage() {
        super(PAGE_NAME);
        setTitle(Messages.NewProjectFromScratchPage_Title);
        setDescription(Messages.NewProjectFromScratchPage_Description);
    }

    /**
     * Create contents of the wizard.
     * @param parent
     */
    @Override
    public void createControl(Composite parent) {

        HelpUtils.setHelp(parent, IXdsHelpContextIds.NEW_PROJECT_FROM_SCRATCH_DLG);

        final int nColumns = 5;
        initializeDialogUnits(parent);

        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout(nColumns, false));

        // Project name: [_________________________________]
        SWTFactory.createLabel(container, Messages.NewProjectPage_ProjectName+':', 2);
        textProjectName = SWTFactory.createSingleText(container, 3);
        SwtUtils.setNameAttribute(this, textProjectName, "textProjectName");//$NON-NLS-1$
        SWTFactory.addCharsFilterValidator(textProjectName, ILLEGAL_FILENAME_CHARS);
        textProjectName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String oldTxt = projectName; 
                projectName = StringUtils.trim(textProjectName.getText());
                autoEdit(textProjectName, oldTxt, textMainName);
                autoEdit(textProjectName, oldTxt, textPrjFName);
//                if (autoEditModule && !mainModule.contains(PROJNAME_VAR)) {
//                    String folder = mainDefSubfolder.isEmpty() ? "" : (mainDefSubfolder + File.separatorChar); //$NON-NLS-1$
//                    textMainName.setText(folder + (projectName.length()>0 ? projectName + ".mod" : "")); //$NON-NLS-1$ //$NON-NLS-2$
//                    autoEditModule = true;
//                }
//                if (autoEditPrj) {
//                    String folder = prjDefSubfolder.isEmpty() ? "" : (prjDefSubfolder + File.separatorChar); //$NON-NLS-1$ 
//                    textPrjFName.setText(folder + (projectName.length()>0 ? projectName + ".prj" : "")); //$NON-NLS-1$ //$NON-NLS-2$
//                    autoEditPrj = true;
//                }
                getWizard().getContainer().updateButtons();
            }
        });

        // Project root: [_____________] [Browse ]
        SWTFactory.createLabel(container, Messages.NewProjectPage_ProjectRoot+':', 2);
        textProjectDir = SWTFactory.createSingleText(container, 2);
        SwtUtils.setNameAttribute(this, textProjectDir, "textProjectDir");//$NON-NLS-1$
        textProjectDir.addModifyListener(new ModifyListener() {
            @Override public void modifyText(ModifyEvent e) {
                projectRoot = StringUtils.trim(textProjectDir.getText());
                getWizard().getContainer().updateButtons();
            }
        });
        Button button = SWTFactory.createPushButton(container, Messages.Common_Browse, null);
        button.addListener(SWT.Selection, new Listener() {
            @Override public void handleEvent(Event event) {
                browseProjectDir();
            }
        });

        // Project SDK:      [___________________[V] [Configure]
        projectSdk = new ProjectSdkPanel(this, container, new int[]{2,2,1}) {
            @Override
            protected void onChanged() {
                setAllBySdk();
                setDefModuleAndPrjNames();
                getWizard().getContainer().updateButtons();
            }           
        };

        // ------------------------------------------------
        SWTFactory.createSeparator(container, nColumns, convertHeightInCharsToPixels(1));

        // [V] Create project file: [_________________]
        cboxCreatePrj  = SWTFactory.createCheckbox(container, Messages.NewProjectFromScratchPage_CreateXdsPrjFile+':', 3);
        SwtUtils.setNameAttribute(this, cboxCreatePrj, "cboxCreatePrj");//$NON-NLS-1$
        cboxCreatePrj.setSelection(true);
        cboxCreatePrj.addSelectionListener(new SelectionListener() {
            @Override public void widgetSelected(SelectionEvent e) {
                createPrj = cboxCreatePrj.getSelection();
                textPrjFName.setEnabled(createPrj);
                getWizard().getContainer().updateButtons();
            }
            @Override public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        textPrjFName = SWTFactory.createSingleText(container, 2);
        SwtUtils.setNameAttribute(this, textPrjFName, "textPrjName");//$NON-NLS-1$
        SWTFactory.addCharsFilterValidator(textPrjFName, ILLEGAL_FILEPATH_CHARS + " "); //$NON-NLS-1$
        textPrjFName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String oldTxt = prjName; 
                prjName = StringUtils.trim(textPrjFName.getText());
                autoEdit(textPrjFName, oldTxt, textMainName);
                getWizard().getContainer().updateButtons();
            }
        });

        // [V] Create Main module : [______________________]
        final Button cboxCreateMain = SWTFactory.createCheckbox(container, Messages.NewProjectFromScratchPage_CreateMainModule+':', 3);
        SwtUtils.setNameAttribute(this, cboxCreateMain, "cboxCreateMain");//$NON-NLS-1$
        cboxCreateMain.setSelection(true);
        cboxCreateMain.addSelectionListener(new SelectionListener() {
            @Override public void widgetSelected(SelectionEvent e) {
                createMain = cboxCreateMain.getSelection();
                textMainName.setEnabled(createMain);
                getWizard().getContainer().updateButtons();
            }
            @Override public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        textMainName = SWTFactory.createSingleText(container, 2);
        SwtUtils.setNameAttribute(this, textMainName, "textMainName");//$NON-NLS-1$
        SWTFactory.addCharsFilterValidator(textMainName, ILLEGAL_FILEPATH_CHARS);
        textMainName.addModifyListener(new ModifyListener() {
            @Override public void modifyText(ModifyEvent e) {
                mainModule = StringUtils.trim(textMainName.getText());
                getWizard().getContainer().updateButtons();
            }
        });

        // ------------------------------------------------
        SWTFactory.createSeparator(container, nColumns, convertHeightInCharsToPixels(1));

        // [V] Use project template from SDK
        cboxTemplateFromSdk = SWTFactory.createCheckbox(container, Messages.NewProjectFromScratchPage_UseSdkProjectTemplate, nColumns);
        SwtUtils.setNameAttribute(this, cboxTemplateFromSdk, "cboxTemplateFromSdk");//$NON-NLS-1$
        cboxTemplateFromSdk.addSelectionListener(new SelectionListener() {
            @Override public void widgetSelected(SelectionEvent e) {
                useTemplateFromSdk = cboxTemplateFromSdk.getSelection();
                reenableForUseSdk();
                getWizard().getContainer().updateButtons();
            }
            @Override public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        SWTFactory.createFreeSpace(container, 0, 3, nColumns);

        // [V] Create directories : [______________________]
        cboxCreateDirs = SWTFactory.createCheckbox(container, Messages.NewProjectFromScratchPage_CreateDirectories+':', 3);
        SwtUtils.setNameAttribute(this, cboxCreateDirs, "cboxCreateDirs"); //$NON-NLS-1$
        textDirs = SWTFactory.createSingleText(container, 2);
        SwtUtils.setNameAttribute(this, textDirs, "textDirs");//$NON-NLS-1$
        textDirs.setEditable(false);
        cboxCreateDirs.setSelection(true);
        cboxCreateDirs.addSelectionListener(new SelectionListener() {
            @Override public void widgetSelected(SelectionEvent e) {
                createDirs = cboxCreateDirs.getSelection();
                textDirs.setEnabled(createDirs);
            }
            @Override public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        // [V] Create redirection file : [______________________]
        cboxCreateRedFile = SWTFactory.createCheckbox(container, Messages.NewProjectFromScratchPage_CreateRedFile+':', 3);
        SwtUtils.setNameAttribute(this, cboxCreateRedFile, "cboxCreateRedFile"); //$NON-NLS-1$
        textRedFile = SWTFactory.createSingleText(container, 2);
        SwtUtils.setNameAttribute(this, textRedFile, "textRedFile");//$NON-NLS-1$
        textRedFile.setEditable(false);
        cboxCreateRedFile.setSelection(true);
        cboxCreateRedFile.addSelectionListener(new SelectionListener() {
            @Override public void widgetSelected(SelectionEvent e) {
                createRedFile = cboxCreateRedFile.getSelection();
                textRedFile.setEnabled(createRedFile);
            }
            @Override public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        setControl(container);
        initContents();
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            textProjectName.setFocus();
        }
    }

    /**
     * Initialize the user interface.
     */
    protected void initContents() {
        projectSdk.initContents();

        projectName = ""; //$NON-NLS-1$
        projectRoot = ""; //$NON-NLS-1$
        prjName     = ""; //$NON-NLS-1$
        mainModule  = ""; //$NON-NLS-1$
        prjDefSubfolder  = ""; //$NON-NLS-1$
        mainDefSubfolder = ""; //$NON-NLS-1$

        createPrj   = true;

        // Tpr:
        setDefModuleAndPrjNames();
        setAllBySdk();

        // red/dirs (cboxes are selected ON now): 
        createRedFile = true;
        createDirs = true;

        // Create main (cboxCreateMain is selected now):
        createMain = true;
    }

    /**
     * Set tprFile to file or ""
     * Set text in textTprFile
     * Set cboxUseTprFile ON/OFF if file exists/not (and it should reenable ctrls)
     */
    private void setAllBySdk() {
        {
            dirsFromSdk = ""; //$NON-NLS-1$
            redFromSdk = ""; //$NON-NLS-1$
            useTemplateFromSdk = false;
            if (projectSdk.isValid()) {
                Sdk sdk = projectSdk.getSelectedSdkInstance();
                if (sdk != null) { // else - int. error?..
                    // Red file:
                    String trd  = sdk.getTrdFile();
                    if (Sdk.isFile(trd)) { 
                        String xc = FilenameUtils.getBaseName(sdk.getCompilerExecutablePath());
                        redFromSdk = xc + ".red"; //$NON-NLS-1$
                        useTemplateFromSdk = true;
                    }
                    // Dirs:
                    dirsFromSdk = StringUtils.trim(sdk.getDirsToCreate());
                    if (Sdk.isSet(dirsFromSdk)) {
                        useTemplateFromSdk = true;
                    }
                    // Prj template:
                    if (Sdk.isFile(sdk.getTprFile())) {
                        useTemplateFromSdk = true;
                    }
                }
            }
        }

        createDirs = !StringUtils.isBlank(dirsFromSdk);
        textDirs.setText(dirsFromSdk);
        cboxCreateDirs.setSelection(createDirs);
        cboxCreateDirs.setEnabled(createDirs);
        textDirs.setEnabled(createDirs);

        createRedFile = !StringUtils.isBlank(redFromSdk); 
        textRedFile.setText(redFromSdk);
        cboxCreateRedFile.setSelection(createRedFile);
        cboxCreateRedFile.setEnabled(createRedFile);
        textRedFile.setEnabled(createRedFile);

        cboxTemplateFromSdk.setSelection(useTemplateFromSdk);
        cboxTemplateFromSdk.setEnabled(useTemplateFromSdk);

        reenableForUseSdk();
    }

    private void reenableForUseSdk() {
        if (useTemplateFromSdk) {
            boolean  available = !StringUtils.isBlank(dirsFromSdk);
            cboxCreateDirs.setSelection(createDirs && available);
            textDirs.setText(dirsFromSdk);
            cboxCreateDirs.setEnabled(available);
            textDirs.setEnabled(createDirs && available);

            available = !StringUtils.isBlank(redFromSdk);
            cboxCreateRedFile.setSelection(createRedFile && available);
            textRedFile.setText(redFromSdk);
            cboxCreateRedFile.setEnabled(available);
            textRedFile.setEnabled(createRedFile && available);
        } else {
            cboxCreateDirs.setSelection(false);
            textDirs.setText(""); //$NON-NLS-1$
            cboxCreateDirs.setEnabled(false);
            textDirs.setEnabled(false);

            cboxCreateRedFile.setSelection(false);
            textRedFile.setText(""); //$NON-NLS-1$
            cboxCreateRedFile.setEnabled(false);
            textRedFile.setEnabled(false);
        }
    }

    private void setDefModuleAndPrjNames() {
        if (projectSdk.isValid()) {
            Sdk sdk = projectSdk.getSelectedSdkInstance();
            if (sdk != null) {
                mainDefSubfolder = trimSubfolderName(sdk.getFolderMainModule());
                prjDefSubfolder  = trimSubfolderName(sdk.getFolderPrjFile());
                String oldStyleMainName = sdk.getXdsIni().getMainModuleName();
                if (mainDefSubfolder.isEmpty() && !StringUtils.isBlank(oldStyleMainName)) {
                    textMainName.setText(oldStyleMainName);
                } else {
                    String fname = FilenameUtils.getName(textMainName.getText().trim()); // may be edited by user now
                    if (fname.contains("$(")) fname = "";
                    if (!mainDefSubfolder.isEmpty()) {
                        mainDefSubfolder += File.separatorChar;
                    }
                    textMainName.setText(mainDefSubfolder + fname);
                }
                { 
                    String fname = FilenameUtils.getName(textPrjFName.getText().trim()); // may be edited by user now
                    if (fname.contains("$(")) fname = "";
                    if (!prjDefSubfolder.isEmpty()) {
                        prjDefSubfolder += File.separatorChar;
                    }
                    textPrjFName.setText(prjDefSubfolder + fname);
                }
            }
        }
    }

    private void browseProjectDir() {
        String s = SWTFactory.browseDirectory(getShell(), Messages.Common_DirectorySelection, Messages.NewProjectFromScratchPage_SelectDirForProject+':', projectRoot);
        if (s != null) {
            textProjectDir.setText(s);
        }
    }

    /**
     * Returns whether this page's controls currently all contain valid 
     * values.
     *
     * @return <code>true</code> if all controls are valid, and
     *   <code>false</code> if at least one is invalid
     */
    private boolean validatePage() {
        int    errType = WizardPage.ERROR;
        String err     = null;

        try {
            // SDK:
            if (!projectSdk.isValid()) {
                throw new ValidationException(projectSdk.getErrorMessage(), true);
            }

            validateEclipseProjectAndLocation(projectName, projectRoot, false);

            // Prj:
            if (createPrj) {
                // Prj name:
                if (prjName.isEmpty()) {
                    throw new ValidationException(Messages.NewProjectFromScratchPage_EnterXdsPrjFileName, true);
                }
            }

            // Create Main module:
            if (createMain) {
                if (mainModule.isEmpty()) {
                    throw new ValidationException(Messages.NewProjectFromScratchPage_EnterMainModuleName, false);
                } else {
                    String s = checkMainName(mainModule);
                    if (s != null) {
                        throw new ValidationException(s, true);
                    }
                }
            }

            if (!createPrj && !createMain) {
                throw new ValidationException(Messages.NewProjectFromScratchPage_PrjForMainModuleShouldBeSpecified, true);
            }

        } catch (ValidationException e) {
            err = e.getMessage();
            errType = e.isError() ? WizardPage.ERROR : WizardPage.WARNING;
        }

        setMessage(err, errType);

        return err==null;
    }

    private String checkMainName(String mainModule) {
        mainModule = mainModule.replace(PROJNAME_VAR, projectName);
        File f = new File(mainModule);
        if (f.getParent() != null) {
            if (f.isAbsolute()) {
                return Messages.NewProjectFromScratchPage_MainModPathShouldBeRelative;
            }
            mainModule = f.getName();
        }
        // is it valid module name?
        String s = mainModule.toLowerCase();
        if (s.endsWith(".ob2") || s.endsWith(".mod")) { //$NON-NLS-1$ //$NON-NLS-2$
            s = s.substring(0, s.length()-4);
        }
        if (s.isEmpty()) {
            return Messages.NewProjectFromScratchPage_InvalidMainModuleName;
        }
        for (int i=0; i<s.length(); ++i) {
            char ch = s.charAt(i);
            if (i>0 && ch>='0' && ch <='9') {
                continue;
            }
            if ((ch >='a' && ch <= 'z') || ch == '_') {
                continue;
            }
            return Messages.NewProjectFromScratchPage_InvalidMainModuleName;
        }
        return null;
    }



    @Override
    public boolean isPageComplete() {
        return validatePage();
    }

    public NewProjectSettings getSettings() {
        NewProjectSettings settings = new NewProjectSettings(projectName, projectRoot, projectSdk.getSelectedSdk());
        if (createPrj) {
            settings.setXdsProjectFile(prjName);
        }
        if (createMain) {
            settings.setMainModule(mainModule.replace(PROJNAME_VAR, projectName));
        }
        if (useTemplateFromSdk) {
            String prjTpl = projectSdk.getSelectedSdkInstance().getTprFile();
            if (Sdk.isFile(prjTpl)) {
                settings.setTemplateFile(prjTpl);
            }
            settings.setCreateRedFile(createRedFile);
            settings.setCreateDirs(createDirs);
        }
        return settings;
    }

    @Override
    public boolean canFlipToNextPage() {
        return false;
    }

    private static boolean isPathsEquals(IPath one, IPath two) {
        if (!WorkbenchUtils.isCaseSensitiveFilesystem()) {
            // If we are on a case-insensitive file system then convert to all lower case.
            one = new Path(one.toOSString().toLowerCase());
            two = new Path(two.toOSString().toLowerCase());
        }
        return one.isPrefixOf(two) || two.isPrefixOf(one);
    }
    
    private static void validateProjectName(String projectName) throws ValidationException {
        // Project name:
        if (projectName == null || projectName.isEmpty()) {
            throw new ValidationException(Messages.NewProjectPage_EnterProjectName, false);
        }
        
        if (ProjectUtils.isProjectExistsWithAnotherCase(projectName)) {
        	throw new ValidationException(Messages.NewProjectPage_ProjectExistsWithAnotherCase, true);
        }
    }

    private static void validateProjectFolder(String projectRoot) throws ValidationException {
        // Project location:
        if (projectRoot == null || projectRoot.isEmpty()) {
            throw new ValidationException(Messages.NewProjectPage_EnterProjectRootLocation, false);
        } else {
            try {
                File d = new File(projectRoot);
                if (!d.isAbsolute()) {
                    throw new IOException();
                }
                projectRoot = d.getCanonicalPath(); // throws IOException on errors
                if (d.getParent() == null) {
                    throw new ValidationException(Messages.NewProjectFromScratchPage_LocationShouldBeNotRoot, true);
                }
            } catch (IOException e) {
                throw new ValidationException(Messages.NewProjectFromScratchPage_LocationShouldBeValidFullPath, true);
            }
        }
    }

    public static void validateEclipseProjectAndLocation(String projectName, String projectRoot, boolean folderFirst) throws ValidationException {
        if (folderFirst) {
            validateProjectFolder(projectRoot);
            validateProjectName(projectName);
        } else {
            validateProjectName(projectName);
            validateProjectFolder(projectRoot);
        }

        // check whether project already exists
        final IProject handle= ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (handle.exists()) {
            throw new ValidationException(Messages.NewProjectFromScratchPage_ProjectAlreadyExists, true);
        }

        IProject p = ProjectUtils.getXdsProjectWithProjectRoot(projectRoot);
        if (p != null) {
            throw new ValidationException(String.format(Messages.NewProjectFromScratchPage_OtherProjectUsesThisFilesLocation, p.getName()), true);
        }

        // Another checks:
        IPath ipPrj = new Path(projectRoot);
        IPath ipPrjParent = ipPrj.removeLastSegments(1);
        IPath ipWorkspace = ResourcesPlugin.getWorkspace().getRoot().getLocation();

        if (isPathsEquals(ipPrjParent, ipWorkspace)) {
            if (handle.getName().equals(ipPrj.lastSegment())) { 
                // <workspace_dir>\<projectName> is legal location but 
                // workspace.validateProjectLocation() rejects it
            } else {
                IStatus is = ResourcesPlugin.getWorkspace().validateProjectLocation(handle, ipPrj);
                if (!is.isOK()) {
                    throw new ValidationException (is.getMessage(), true);
                }
            }
        }
    }

    /**
     * @param src - Text control after text change
     * @param srcTextBefore - text from 'src' before change 
     * @param dest - Text control to edit
     * 
     * When file name (w/o extension) in 'dest' is the same with file name (w/o extension) from
     * 'srcTextBefore', changes this name in 'dest' with the new one from 'src'  
     */
    private void autoEdit(Text src, String srcTextBefore, Text dest) {
        if (hsAutoEditRecursionDetector.add(dest)) {
            String oldSrcName = FilenameUtils.getBaseName(srcTextBefore.trim()).trim();
            String destTxt = dest.getText().trim();
            String destName = FilenameUtils.getBaseName(destTxt).trim();
            if (destName.isEmpty() || (destName.equals(oldSrcName) && !destName.contains("$("))) {
                String newSrcName = FilenameUtils.getBaseName(src.getText().trim()).trim();
                String newDstText = FilenameUtils.getPath(destTxt) + newSrcName;
                String ext = FilenameUtils.getExtension(destTxt);
                if (!ext.isEmpty()) {
                    newDstText += '.';
                    newDstText += ext;
                }
                dest.setText(newDstText);
            }
            hsAutoEditRecursionDetector.remove(dest);
        }
    }
    private final Set<Text> hsAutoEditRecursionDetector = new HashSet<Text>();


    //---
    
    private static final Set<Character> hsFolderCutChars = new HashSet<Character>();
    static {
        hsFolderCutChars.add(' ');
        hsFolderCutChars.add('\\');
        hsFolderCutChars.add('/');
        hsFolderCutChars.add('\t');
    }
    
    // trims ' ', '\\', '\' and '\t' characters around string
    private String trimSubfolderName(String s) {
        if (s == null) return ""; //$NON-NLS-1$
        int len = s.length();
        int b = 0;
        while (b < s.length() && hsFolderCutChars.contains(s.charAt(b))) {
            ++b;
        }
        int e = len-1;
        while (e > b && hsFolderCutChars.contains(s.charAt(b))) {
            --e;
        }
        return s.substring(b,  e+1);
    }
    
    

}
