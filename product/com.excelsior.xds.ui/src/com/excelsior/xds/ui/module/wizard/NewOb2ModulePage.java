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

public class NewOb2ModulePage extends WizardPage {
    
    private static final String PAGE_NAME = "NewModulePage"; //$NON-NLS-1$
    
    private IProject project;
    private String moduleName   = ""; //$NON-NLS-1$
    private String sourceFolder = ""; //$NON-NLS-1$
    private boolean isMainModule;
    
    private Text textModuleName;
    private Button cboxMainModule;
    
    private IContainer rootContainer;
    
    private Text textSourceFolder;

    protected NewOb2ModulePage() {
        super(PAGE_NAME);
        setTitle(Messages.NewOb2ModulePage_Ob2Module);
        setDescription(Messages.NewOb2ModulePage_CreateNewOb2Module);
    }
    
    public IProject getProject() {
        return project;
    }

    public String getModuleName() {
        return moduleName;
    }
    
    public boolean isMainModule() {
        return isMainModule;
    }

    public String getSourceFolder() {
        return sourceFolder;
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
                SWTFactory.createLabel(container, Messages.NewOb2ModulePage_ModuleName+':', 2);
		textModuleName = SWTFactory.createSingleText(container, 3);
		textModuleName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
                moduleName = textModuleName.getText().trim();
                updateModulePathControls();
                setPageComplete(validatePage());
			}
		});
      
		// Module folder: [_____________] [Browse ]
                SWTFactory.createLabel(container, Messages.NewOb2ModulePage_ModuleFolder+':', 2);
		textSourceFolder = SWTFactory.createSingleText(container, 2, SWT.SINGLE | SWT.BORDER);
        textSourceFolder.setText(""); //$NON-NLS-1$
        textSourceFolder.setEditable(false);
        Button btnBrowseSourceFolder = SWTFactory.createPushButton(container, Messages.Common_Browse, null);
        btnBrowseSourceFolder.addListener(SWT.Selection, new Listener() {
			@Override 
			public void handleEvent(Event event) {
                IContainer f = browseFolder(ResourceUtils.getWorkspaceRoot(), Messages.NewOb2ModulePage_SelectFolder);
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
        cboxMainModule = SWTFactory.createCheckbox(container, Messages.NewOb2ModulePage_CreateMainModule, nColumns);
        SwtUtils.setNameAttribute(this, cboxMainModule, "cboxMainModule");//$NON-NLS-1$
        cboxMainModule.addSelectionListener(new SelectionAdapter() {
            @Override 
            public void widgetSelected(SelectionEvent e) {
                isMainModule = cboxMainModule.getSelection();
                setPageComplete(validatePage());
            }
        });
        SWTFactory.createFreeSpace(container, 0, 2, nColumns);

        if (initializeModel()) {
            modelToControls(true);
        } else {
        	// No XDS project - nothing 2do
            cboxMainModule.setEnabled(false);
            textModuleName.setEnabled(false);
            btnBrowseSourceFolder.setEnabled(false);
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
                err = Messages.NewOb2ModulePage_CantCreateNoXdsProject;
                return false;
            }
            if (StringUtils.isBlank(moduleName)) {
                err = Messages.NewOb2ModulePage_EnterModuleName;
                errType = WizardPage.WARNING;
                return false;
            }
            
            if (!isValidName(moduleName)) {
                err = Messages.NewOb2ModulePage_ModNameInvalid;
                return false;
            }
            
            if (checkExist(sourceFolder, moduleName + ".ob2") ) { //$NON-NLS-1$
                err = String.format(Messages.NewOb2ModulePage_ModuleExists, moduleName + ".ob2", sourceFolder); //$NON-NLS-2$
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

        isMainModule              = false;

        return true;
    }
    
    private void modelToControls(boolean isSourceFolderChanged) {
        textSourceFolder.setText(sourceFolder);
        updateModulePathControls();
    }

    private void updateModulePathControls() {
        textSourceFolder.setText(rootContainer.getFullPath().toPortableString()); 
    }

    private void setSourceFolder(IContainer f) {
        sourceFolder = f.getFullPath().toPortableString();
    }

    private void setProjectAndSourceFoldersFromRootContainer() {
        if (rootContainer != null) {
            setSourceFolder(rootContainer);
            this.project = rootContainer.getProject();
        }
    }
}