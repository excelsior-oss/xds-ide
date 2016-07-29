package com.excelsior.xds.ui.module.wizard;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.excelsior.xds.core.help.IXdsHelpContextIds;
import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.project.SpecialFolderNames;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.ui.commons.utils.HelpUtils;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.commons.utils.SelectionUtils;
import com.excelsior.xds.ui.commons.utils.SwtUtils;
import com.excelsior.xds.ui.dialogs.SelectModulaSourceFolderDialog;
import com.excelsior.xds.ui.internal.nls.Messages;

public class NewModulePage extends WizardPage {
    
    private static final String PAGE_NAME = "NewModulePage"; //$NON-NLS-1$
    
    private IProject project;
    private String moduleName   = ""; //$NON-NLS-1$
    private String sourceFolder = ""; //$NON-NLS-1$
    private boolean isUseDefinitionModule;
    private String definitionModuleSourceFolder = ""; //$NON-NLS-1$
    private boolean isUseImplementationModule;
    private String implementationModuleSourceFolder = ""; //$NON-NLS-1$
    private boolean isMainModule;
    
    private Text textDefinitionModulePath;
    private Text textImplementationModulePath;
    private Text textModuleName;
    private Button cboxMainModule;
    private Button btnBrowseImplementationModuleSourceFolder;
    private Button cboxImplementationModule;
    private Button btnBrowseDefinitionModuleSourceFolder;
    private Button cboxDefinitionModule;
    private Label  labelDefinitionModule;
    
    private IContainer rootContainer;
    
    private Text textSourceFolder;

    protected NewModulePage() {
        super(PAGE_NAME);
        setTitle(Messages.NewModulePage_Title);
        setDescription(Messages.NewModulePage_Description);
    }
    
    public IProject getProject() {
        return project;
    }

    public String getModuleName() {
        return moduleName;
    }
    
    public boolean isUseDefinitionModule() {
        return isUseDefinitionModule;
    }

    public boolean isUseImplementationModule() {
        return isUseImplementationModule;
    }

    public boolean isMainModule() {
        return isMainModule;
    }

    public String getImplementationModuleSourceFolder() {
        return implementationModuleSourceFolder;
    }
    
    public String getDefinitionModuleSourceFolder() {
        return definitionModuleSourceFolder;
    }

    @Override
    public void createControl(Composite parent) {
	    final int nColumns = 5;
        initializeDialogUnits(parent);
        HelpUtils.setHelp(parent, IXdsHelpContextIds.NEW_MODULE_DLG);


        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout(nColumns, false));
        setControl(container);
        
		// Module name: [_________________________________]
                SWTFactory.createLabel(container, Messages.NewModulePage_ModuleName+':', 2);
		textModuleName = SWTFactory.createSingleText(container, 3);
		textModuleName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
                moduleName = textModuleName.getText().trim();
                updateModulePathControls();
                boolean isValid = validatePage();
                setPageComplete(isValid);
			}
		});
      
		// Source folder: [_____________] [Browse ]
                SWTFactory.createLabel(container, Messages.NewModulePage_SourceFolder+':', 2);
		textSourceFolder = SWTFactory.createSingleText(container, 2, SWT.SINGLE | SWT.BORDER);
        textSourceFolder.setText(""); //$NON-NLS-1$
        textSourceFolder.setEditable(false);
        Button btnBrowseSourceFolder = SWTFactory.createPushButton(container, Messages.Common_Browse, null);
        btnBrowseSourceFolder.addListener(SWT.Selection, new Listener() {
			@Override 
			public void handleEvent(Event event) {
                IContainer f = browseFolder(ResourceUtils.getWorkspaceRoot(), Messages.NewModulePage_SelFolderForModule);
                if (f != null) {
                    rootContainer = f;
                    setProjectAndSourceFoldersFromRootContainer();
                    updateModulePathControls();
                    setPageComplete(validatePage());
                }
			}
		});
        
        // ------------------------------------------------
        SWTFactory.createSeparator(container, nColumns, convertHeightInCharsToPixels(1));

        // [V] Create main module
        cboxMainModule = SWTFactory.createCheckbox(container, Messages.NewModulePage_MainModule, nColumns);
        SwtUtils.setNameAttribute(this, cboxMainModule, "cboxMainModule");//$NON-NLS-1$
        cboxMainModule.addSelectionListener(new SelectionAdapter() {
            @Override 
            public void widgetSelected(SelectionEvent e) {
                isMainModule = cboxMainModule.getSelection();
                updateForMainModule();
                setPageComplete(validatePage());
            }
        });
        SWTFactory.createFreeSpace(container, 0, 2, nColumns);

        // [V] Implementation: [_____________] [Browse ]
        cboxImplementationModule  = SWTFactory.createCheckbox(container, null, 1);
        SWTFactory.createLabel(container, Messages.NewModulePage_Implementation+':', 2);
        cboxImplementationModule.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isUseImplementationModule = cboxImplementationModule.getSelection();
                setImplementationModuleSelection(isUseImplementationModule);
                setPageComplete(validatePage());
            }
        });
        cboxImplementationModule.setSelection(true);
        textImplementationModulePath = SWTFactory.createSingleText(container, 1, SWT.SINGLE | SWT.BORDER);
        textImplementationModulePath.setEditable(false);
        btnBrowseImplementationModuleSourceFolder = SWTFactory.createPushButton(container, Messages.Common_Browse, null);
        btnBrowseImplementationModuleSourceFolder.addListener(SWT.Selection, new Listener() {
            @Override 
            public void handleEvent(Event event) {
                IContainer f = browseFolder(rootContainer, Messages.NewModulePage_SelFolderForImplementation);
                if (f != null) {
                    implementationModuleSourceFolder = f.getFullPath().toPortableString();
                    updateModulePathControls();
                    setPageComplete(validatePage());
                }
            }
        });
        
        // [V] Definition: [_____________] [Browse ]
        cboxDefinitionModule  = SWTFactory.createCheckbox(container, null, 1);
        labelDefinitionModule = SWTFactory.createLabel(container, Messages.NewModulePage_Definition+':', 2);
        cboxDefinitionModule.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isUseDefinitionModule = cboxDefinitionModule.getSelection();
                setDefinitionModuleSelection(isUseDefinitionModule);
                setPageComplete(validatePage());
            }
        });
        cboxDefinitionModule.setSelection(true);
        textDefinitionModulePath = SWTFactory.createSingleText(container, 1, SWT.SINGLE | SWT.BORDER);
        textDefinitionModulePath.setEditable(false);
        btnBrowseDefinitionModuleSourceFolder = SWTFactory.createPushButton(container, Messages.Common_Browse, null);
        btnBrowseDefinitionModuleSourceFolder.addListener(SWT.Selection, new Listener() {
			@Override 
			public void handleEvent(Event event) {
                IContainer f = browseFolder(rootContainer, Messages.NewModulePage_SelFolderForDefinition);
                if (f != null) {
                    definitionModuleSourceFolder = f.getFullPath().toPortableString(); 
                    updateModulePathControls();
                    setPageComplete(validatePage());
                }
			}
		});

        if (initializeModel()) {
            modelToControls(true);
        } else {
        	// No XDS project - nothing 2do
            cboxDefinitionModule.setEnabled(false);
            cboxImplementationModule.setEnabled(false);
            cboxMainModule.setEnabled(false);
            textModuleName.setEnabled(false);
            btnBrowseSourceFolder.setEnabled(false);
            btnBrowseImplementationModuleSourceFolder.setEnabled(false);
            btnBrowseDefinitionModuleSourceFolder.setEnabled(false);
        }
    }
    
    private IContainer browseFolder(IContainer rootContainer, String dlgTitle) {
        if (rootContainer == null) rootContainer = ResourceUtils.getWorkspaceRoot();
        SelectModulaSourceFolderDialog dlg = new SelectModulaSourceFolderDialog(getShell(), dlgTitle, rootContainer, SpecialFolderNames.getSpecialFolderRelativePathes(project));
        if (dlg.open() == SelectModulaSourceFolderDialog.OK) {
            return dlg.getResultFolder();
        }
        return null;
    }
    
    @Override
    public boolean isPageComplete() {
        return validatePage();
    }

    private boolean validatePage() {
        String err = null;
        int    errType = WizardPage.ERROR;
        try {
            if (rootContainer == null) {
                err = Messages.NewModulePage_CantWhenNoXdsProject;
                return false;
            }
            if (StringUtils.isBlank(moduleName)) {
                err = Messages.NewModulePage_ModuleNameRequired;
                errType = WizardPage.WARNING;
                return false;
            }
            
            if (!isValidName(moduleName)) {
                err = Messages.NewModulePage_ModuleNameInvalid;
                return false;
            }
            
            if (cboxDefinitionModule.getSelection()) {
                if (StringUtils.isBlank(definitionModuleSourceFolder)) {
                    err = Messages.NewModulePage_NoDefName;
                    return false;
                }
                
                if (checkExist(definitionModuleSourceFolder, moduleName + ".def") ) { //$NON-NLS-1$
                    err = String.format(Messages.NewModulePage_DefAlreadyExists, moduleName + ".def", definitionModuleSourceFolder); //$NON-NLS-1$
                    return false;
                }
            }
            
            if (cboxImplementationModule.getSelection()) {
                if (StringUtils.isBlank(implementationModuleSourceFolder)) {
                    err = Messages.NewModulePage_NoImplName;
                    return false;
                }
                
                if (checkExist(implementationModuleSourceFolder, moduleName + ".mod") ) { //$NON-NLS-1$
                    err = String.format(Messages.NewModulePage_ImplAlreadyExists, moduleName + ".mod", implementationModuleSourceFolder); //$NON-NLS-1$
                    return false;
                }
            }
            
            if (!cboxDefinitionModule.getSelection() && !cboxImplementationModule.getSelection()) {
                err = Messages.NewModulePage_DefOrImplShouldBeSpecified;
                return false;
            }
        }
        finally {
            setMessage(err, errType);
        }
        return true;
    }
    
    private boolean isValidName(String s) {
        s = s.toLowerCase();
        for (int i=0; i<s.length(); ++i) {
            char c = s.charAt(i);
            if ((c >= 'a' && c<='z') || c == '_') {
                continue;
            }
            if (i>0 && c>='0' && c<='9') {
                continue;
            }
            return false;
        }
        return true;
    }
    
    private boolean checkExist(String folder, String file) {
        IWorkspaceRoot root = project.getWorkspace().getRoot();
        IContainer f = (IContainer)root.findMember(folder);
        if (f.exists()) {
            if (f.getFile(new Path(file)).exists()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean initializeModel() {
        List<IResource> selectedResouces = SelectionUtils.getSelectedResources();
        if (!selectedResouces.isEmpty()) {
            IResource res = selectedResouces.get(0);
            if (res instanceof IContainer) {
                rootContainer = (IContainer) res;
            }
            else {
                rootContainer = res.getParent();
            }
        }
        else {
            List<IProject> xdsProjects = ProjectUtils.getXdsProjects();
            if (!xdsProjects.isEmpty()) {
                rootContainer = xdsProjects.get(0);
            }
        }
        if (rootContainer == null) {
        	return false;
        }
        setProjectAndSourceFoldersFromRootContainer();

        isUseDefinitionModule     = true;
        isUseImplementationModule = true;
        isMainModule              = false;

        if (!selectedResouces.isEmpty()) {
            IResource r = selectedResouces.get(0);
            IContainer f = null;
            if (r instanceof IContainer) {
                f = (IContainer)r;
            }
            else {
                f = r.getParent();
            }
            definitionModuleSourceFolder     = f.getFullPath().toPortableString(); 
            implementationModuleSourceFolder = definitionModuleSourceFolder;
        }
        return true;
    }
    
    private void modelToControls(boolean isSourceFolderChanged) {
        textSourceFolder.setText(sourceFolder);
        updateModulePathControls();
    }

    private void updateModulePathControls() {
        textSourceFolder.setText(rootContainer.getFullPath().toPortableString());
        if (!StringUtils.isBlank(moduleName)) {
            if (cboxDefinitionModule.getSelection()) {
                textDefinitionModulePath.setText(getDefinitionModulePath());
            }
            if (cboxImplementationModule.getSelection()) {
                textImplementationModulePath.setText(getImplementationModulePath());
            }
        }
    }

    public String getImplementationModulePath() {
        return implementationModuleSourceFolder + "/" + moduleName + ".mod"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getDefinitionModulePath() {
        return definitionModuleSourceFolder + "/" + moduleName + ".def"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    private void setSourceFolder(IContainer f) {
        sourceFolder = f.getFullPath().toPortableString();
        implementationModuleSourceFolder = sourceFolder;
        definitionModuleSourceFolder     = sourceFolder;
    }

    private void setProjectAndSourceFoldersFromRootContainer() {
        if (rootContainer != null) {
            setSourceFolder(rootContainer);
            this.project = rootContainer.getProject();
        }
    }
    
    private void setDefinitionModuleSelection (boolean selected) {
        textDefinitionModulePath.setText( !selected || StringUtils.isBlank(moduleName) 
                                        ? "" : getDefinitionModulePath() ); //$NON-NLS-1$
//        labelDefinitionModule.setEnabled(selected);
        textDefinitionModulePath.setEnabled(selected);
        btnBrowseDefinitionModuleSourceFolder.setEnabled(selected);
    }

    private void setDefinitionModuleEnabled (boolean enabled) {
        setDefinitionModuleSelection(enabled && isUseDefinitionModule);
        cboxDefinitionModule.setEnabled(enabled);
        labelDefinitionModule.setEnabled(enabled);
        textDefinitionModulePath.setEnabled(enabled);
    }

    private void setImplementationModuleSelection (boolean selected) {
        textImplementationModulePath.setText( !selected || StringUtils.isBlank(moduleName) 
                                            ? "" : getImplementationModulePath() ); //$NON-NLS-1$
//        labelImplementationModule.setEnabled(selected);
        textImplementationModulePath.setEnabled(selected);
        btnBrowseImplementationModuleSourceFolder.setEnabled(selected);
    }

    private void updateForMainModule() {
        if (isMainModule) {
            cboxDefinitionModule.setSelection(false);
            setDefinitionModuleEnabled(false);

            cboxImplementationModule.setSelection(true);
            setImplementationModuleSelection(true);
            cboxImplementationModule.setEnabled(false);

        } else {
            cboxDefinitionModule.setSelection(isUseDefinitionModule);
            setDefinitionModuleEnabled(true);
            
            cboxImplementationModule.setSelection(isUseImplementationModule);
            setImplementationModuleSelection(isUseImplementationModule);
            cboxImplementationModule.setEnabled(true);

        }
    }
    
}