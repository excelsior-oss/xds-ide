package com.excelsior.xds.ui.project.wizard;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.ResourcesPlugin;
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
import com.excelsior.xds.ui.commons.utils.HelpUtils;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.commons.utils.SwtUtils;
import com.excelsior.xds.ui.internal.nls.Messages;
import com.excelsior.xds.ui.sdk.ProjectSdkPanel;

/**
 * The first page of the Modula-2 project creation wizard from existing sources. 
 */
public class NewProjectFromSourcesPage extends WizardPage {
	
	private static final String PAGE_NAME = "NewProjectFromSourcesPage"; //$NON-NLS-1$
	
	private Text textProjectName;
	private Text textProjectDir;
	private Text textProjectFile;
	private Text textMainModule;
	private Button btnBrowseProjectFile;
	private Button btnBrowseMainModule;
	private Button rbProjectFile;
	
	private boolean autoeditProjectName;
	private boolean autoeditProjectDir;
	private boolean autoeditProjectFile;
	private boolean autoeditInProgress;
	
	private String projectName;
	private String projectRoot;
	private String projectFile;
	private String mainModule;
	private boolean isProjectFileMode; // else - main module

    private ProjectSdkPanel projectSdk;
	
	private static final String ILLEGAL_FILENAME_CHARS = "/\\*:?<>|\"\t"; //$NON-NLS-1$

	/**
	 * Create the wizard.
	 */
	public NewProjectFromSourcesPage() {
		super(PAGE_NAME);
		setTitle(Messages.NewProjectFromSourcesPage_Title);
		setDescription(Messages.NewProjectFromSourcesPage_Description);
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
	    
        HelpUtils.setHelp(parent, IXdsHelpContextIds.NEW_PROJECT_FROM_SOURCES_DLG);

	    final int nColumns = 3;
        initializeDialogUnits(parent);

	    Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(nColumns, false));

		// Project name: [_________________________________]
                SWTFactory.createLabel(container, Messages.NewProjectPage_ProjectName+':', 1);
		textProjectName = SWTFactory.createSingleText(container, 2);
		SwtUtils.setNameAttribute(this, textProjectName, "textProjectName");//$NON-NLS-1$
		SWTFactory.addCharsFilterValidator(textProjectName, ILLEGAL_FILENAME_CHARS);
		textProjectName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				projectName = StringUtils.trim(textProjectName.getText());
				if (!autoeditInProgress) {
					autoeditProjectName = false;
					getWizard().getContainer().updateButtons();
				}
			}
		});
		
		
		// Project root: [_____________] [Browse ]
                SWTFactory.createLabel(container, Messages.NewProjectPage_ProjectRoot+':', 1);
		textProjectDir = SWTFactory.createSingleText(container, 1, SWT.SINGLE | SWT.BORDER);
		SwtUtils.setNameAttribute(this, textProjectDir, "textProjectDir");//$NON-NLS-1$
		textProjectDir.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				projectRoot = StringUtils.trim(textProjectDir.getText());
				if (!autoeditInProgress) {
					autoeditProjectDir = false;
	
					autoeditInProgress = true;
					File dir = new File(projectRoot);
					String shortname = null;
					boolean ob2modFound = false;
					// try to show 1st project from the dir:
					if (autoeditProjectFile && dir.isDirectory() && dir.list()!=null) {
						for (String f : dir.list()) {
						    f = f.toLowerCase();
							if (f.endsWith(".prj")) { //$NON-NLS-1$
								textProjectFile.setText(new File(dir, f).getAbsolutePath());
								shortname = f.substring(0,f.length()-4);
							} else if (!ob2modFound && (f.endsWith(".ob2") || f.endsWith(".mod"))) { //$NON-NLS-1$ //$NON-NLS-2$
							    ob2modFound = true;
							}
						}
					}
					if (autoeditProjectName) {
	                    // try to set this project name as eclipse project name:
					    if (shortname != null && ResourcesPlugin.getWorkspace().getRoot().findMember(shortname) == null) {
    						textProjectName.setText(shortname); 
    					} 
                        // try to set this project name as the directory name:
					    else if (ob2modFound) {
    					    String dirName = dir.getName();
    					    if (ResourcesPlugin.getWorkspace().getRoot().findMember(dirName) == null) {
                                textProjectName.setText(dirName); 
    					    }
    					}
					}
					autoeditInProgress = false;
					
					getWizard().getContainer().updateButtons();
				}
			}
		});

		Button button = SWTFactory.createPushButton(container, Messages.Common_Browse, null);
		button.addListener(SWT.Selection, new Listener() {
			@Override public void handleEvent(Event event) {
				browseProjectDir();
			}
		});
		
        // Project SDK:      [___________________[V] [Configure]
        projectSdk = new ProjectSdkPanel(this, container, new int[]{1,1,1}) {
            @Override
            protected void onChanged() {
                getWizard().getContainer().updateButtons();
            }           
        };


        //
		SWTFactory.createSeparator(container, nColumns, convertHeightInCharsToPixels(1));
		
		// (o) Project file :      [______________] [Browse]
                rbProjectFile = SWTFactory.createRadiobutton(container, Messages.NewProjectFromSourcesPage_ProjectFileLabel+':', 1);
		SwtUtils.setNameAttribute(this, rbProjectFile, "rbProjectFile");//$NON-NLS-1$
		rbProjectFile.addSelectionListener(new SelectionListener() {
			@Override public void widgetSelected(SelectionEvent e) {
				setProjectFileMode(rbProjectFile.getSelection());
				getWizard().getContainer().updateButtons();
			}
			@Override public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		textProjectFile = SWTFactory.createSingleText(container, 1, SWT.SINGLE | SWT.BORDER);
		SwtUtils.setNameAttribute(this, textProjectFile, "textProjectFile");//$NON-NLS-1$
		textProjectFile.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				projectFile = StringUtils.trim(textProjectFile.getText());
				if (!autoeditInProgress) {
					autoeditProjectFile = false;
					
					autoeditInProgress = true;
					File f = new File(projectFile);
					String name = f.getName();
					if (f.isFile() && name.length()>4 && name.toLowerCase().endsWith(".prj")) { //$NON-NLS-1$
						String dir = f.getParent();
						if (dir.length()>3) { // don't allow roots ~= c:\zz.prj
							if (autoeditProjectDir) {
								textProjectDir.setText(dir);
							}
							if (autoeditProjectName) {
								String shortname = name.substring(0,name.length()-4);
								if (ResourcesPlugin.getWorkspace().getRoot().findMember(shortname) == null) {
									textProjectName.setText(shortname);
								}
							}
						}
					}
					autoeditInProgress = false;
					
					getWizard().getContainer().updateButtons();
				}
			}
		});
		btnBrowseProjectFile = SWTFactory.createPushButton(container, Messages.Common_Browse, null);
		SwtUtils.setNameAttribute(this, btnBrowseProjectFile, "btnBrowseProjectFile"); //$NON-NLS-1$
		btnBrowseProjectFile.addListener(SWT.Selection, new Listener() {
			@Override public void handleEvent(Event event) {
				browseProjectFile();
			}
		});
		
		// ( ) Main module :       [______________] [Browse]
                SWTFactory.createRadiobutton(container, Messages.NewProjectFromSourcesPage_MainModuleLabel+':', 1);
		textMainModule = SWTFactory.createSingleText(container, 1, SWT.SINGLE | SWT.BORDER);
		SwtUtils.setNameAttribute(this, textMainModule, "textMainModule"); //$NON-NLS-1$
		textMainModule.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				mainModule = StringUtils.trim(textMainModule.getText());
				if (!autoeditInProgress) {
					
					autoeditInProgress = true;
					File f = new File(mainModule);
					String name = f.getName();
					if (f.isFile() && name.length()>4 && name.toLowerCase().endsWith(".mod") || name.toLowerCase().endsWith(".ob2")) { //$NON-NLS-1$ //$NON-NLS-2$
						String dir = f.getParent();
						if (dir.length()>3) { // don't allow roots ~= c:\zz.mod
							if (autoeditProjectDir) {
								textProjectDir.setText(dir);
							}
							if (autoeditProjectName) {
								String shortname = name.substring(0,name.length()-4);
								if (ResourcesPlugin.getWorkspace().getRoot().findMember(shortname) == null) {
									textProjectName.setText(shortname);
								}
							}
						}
					}
					autoeditInProgress = false;
					
					getWizard().getContainer().updateButtons();
				}
			}
		});
		btnBrowseMainModule = SWTFactory.createPushButton(container, Messages.Common_Browse, null);
		SwtUtils.setNameAttribute(this, btnBrowseMainModule, "btnBrowseMainModule"); //$NON-NLS-1$
		btnBrowseMainModule.addListener(SWT.Selection, new Listener() {
			@Override public void handleEvent(Event event) {
				browseMainModule();
			}
		});

		setControl(container);
		initContents();
	}
	
	
	/**
	 * Initialize the user interface.
	 */
	protected void initContents() {
		projectSdk.initContents();

		projectName = ""; //$NON-NLS-1$
		projectRoot  = ""; //$NON-NLS-1$
		projectFile = ""; //$NON-NLS-1$
		mainModule  = ""; //$NON-NLS-1$
		rbProjectFile.setSelection(true);
		setProjectFileMode(true);
		
		autoeditProjectName = true;
		autoeditProjectDir  = true;
		autoeditProjectFile = true;
		autoeditInProgress  = false;
	}
	
	private void setProjectFileMode(boolean b) {
		isProjectFileMode = b;
		textProjectFile.setEnabled(b);
		textMainModule.setEnabled(!b);
		btnBrowseProjectFile.setEnabled(b);
		btnBrowseMainModule.setEnabled(!b);
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
		    textProjectDir.setFocus();
		}
	}

	private void browseProjectDir() {
		String s = SWTFactory.browseDirectory( getShell()
		                                     , Messages.Common_DirectorySelection
                                                     , Messages.NewProjectFromSourcesPage_ProjectRoot_BrowseMessage+':'
		                                     , StringUtils.isBlank(projectRoot) ? null : projectRoot);
		if (s != null) {
			textProjectDir.setText(s);
		}
	}
	
	private void browseProjectFile() {
		String s = SWTFactory.browseFile(getShell(), false, Messages.NewProjectFromSourcesPage_SelectProjectFile, new String[]{"*.prj"}, projectRoot); //$NON-NLS-1$
		if (s != null) {
			textProjectFile.setText(s);
		}
	}
	
	private void browseMainModule() {
		String s = SWTFactory.browseFile(getShell(), false, Messages.NewProjectFromSourcesPage_SelectMainModule, new String[]{"*.mod;*.ob2"}, projectRoot); //$NON-NLS-1$
		if (s != null) {
			textMainModule.setText(s);
		}
	}
	
    /**
     * Returns whether this page's controls currently all contain valid 
     * values.
     *
     * @return <code>true</code> if all controls are valid, and
     *   <code>false</code> if at least one is invalid
     */
	protected boolean validatePage() {
		int    errType = WizardPage.ERROR;
		String err     = null;

		try {
			// SDK:
			if (!projectSdk.isValid())
				throw new ValidationException(projectSdk.getErrorMessage(), true);
			
			NewProjectFromScratchPage.validateEclipseProjectAndLocation(projectName, projectRoot, true);
			
			// Project file OR Main module:
			String fname   = mainModule;
			String exts[]  = new String[]{".mod", ".ob2"}; //$NON-NLS-1$ //$NON-NLS-2$
			String msgname = Messages.NewProjectFromSourcesPage_MainModule;
			String msgext  = Messages.NewProjectFromSourcesPage_ModOrOb2;
			if (isProjectFileMode) {
				fname   = projectFile;
				exts    = new String[]{".prj"}; //$NON-NLS-1$
				msgname = Messages.NewProjectFromSourcesPage_ProjectFile;
				msgext  = ".prj"; //$NON-NLS-1$
			}
			
			if (fname.isEmpty()) {
				throw new ValidationException(msgname + Messages.NewProjectFromSourcesPage_IsNotSelected, true);
			} else {
				File f = new File (fname);
				try {
					if (!f.isFile()) {
						throw new IOException();
					}
					String dir = new File(projectRoot).getCanonicalPath();
					dir += File.separator;
					
					String tmp =  f.getCanonicalPath();
					
					if (!tmp.startsWith(dir)) {
						throw new ValidationException(msgname + Messages.NewProjectFromSourcesPage_ShouldBeInsideProjectFilesLocation, true);
					}
					String s = fname.toLowerCase();
					for (String ext : exts) {
						if (s.endsWith(ext)) {
							s = null;
							break;
						}
					}
					if (s != null) {
						throw new ValidationException(msgname + Messages.NewProjectFromSourcesPage_ShouldHave + msgext + Messages.NewProjectFromSourcesPage_Extension, true);
					}
					
					if (isProjectFileMode) {
						projectFile = tmp;
					} else {
						mainModule = tmp;
					}
				} catch (IOException e) {
					throw new ValidationException(msgname + Messages.NewProjectFromSourcesPage_InvalidFile, true);
				}
			}

		} catch (ValidationException e) {
			err = e.getMessage();
			errType = e.isError() ? WizardPage.ERROR : WizardPage.WARNING;
		}
		
		setMessage(err, errType);

		return err==null;
	}
	
	@Override
	public boolean isPageComplete() {
		return validatePage();
	}
	
	public NewProjectSettings getSettings() {
		NewProjectSettings settings = new NewProjectSettings(projectName, projectRoot, projectSdk.getSelectedSdk());
		if (isProjectFileMode) {
			settings.setXdsProjectFile(projectFile);
		} else {
			settings.setMainModule(mainModule);
		}
		return settings;
	}

	@Override
	public boolean canFlipToNextPage() {
		return false;
	}
}
