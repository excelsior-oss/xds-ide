package com.excelsior.xds.ui.preferences.sdk;


import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import com.excelsior.xds.core.help.IXdsHelpContextIds;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkRegistry;
import com.excelsior.xds.ui.commons.utils.HelpUtils;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.internal.nls.Messages;

public class EditSdkDialog extends Wizard {
	
    private Sdk editedSdk;
    private SdkRegistry sdkRegistry;
    private String sdkNameInRegistry;
	private boolean isSdkOk;
    private DialogPage dialogPage;
    private boolean pageCreated = false;
    private ArrayList<EditSdkDialogTabAbstract> tabs;
    private Text textHomePath;
    private Text textName;

    
	public static Sdk playDialog(Shell shell, boolean isSdkNameNotInRegistry, Sdk sdk, SdkRegistry sdkRegistry) {
		EditSdkDialog esd        = new EditSdkDialog(isSdkNameNotInRegistry, sdk, sdkRegistry);
	    WizardDialog  dialog     = new WizardDialog(shell, esd);
	    dialog.create();
	    dialog.open();
	    return esd.getSdk(); // not null when valid Sdk and 'Ok' clicked 
	}

	
	private EditSdkDialog(boolean isSdkNameNotInRegistry, Sdk sdk, SdkRegistry sdkRegistry) {
		super();
		this.sdkRegistry       = sdkRegistry;
		this.sdkNameInRegistry = isSdkNameNotInRegistry ? "" : sdk.getName(); //$NON-NLS-1$
		this.editedSdk         = sdk.clone();
		this.isSdkOk           = false;
	    this.tabs              = new ArrayList<EditSdkDialogTabAbstract>();

		setWindowTitle(Messages.EditSdkDialog_Title);
	}
	
    public boolean validate(boolean setFocus) {
        if (!pageCreated) {
            return true;
        }
        
        int errType = WizardPage.ERROR;
        String err = null;
        Text badTextControl = textHomePath;
        String badTabName = null;
        boolean badOnCurrentTab = true;

        String homePath = textHomePath.getText().trim();
        if (homePath.length() == 0) {
            err = Messages.EditSdkDialog_EnterXdsHomeDir;
            errType = WizardPage.WARNING;
        } else if (!validatePath(homePath, true)) {
            err = Messages.EditSdkDialog_InvalidXdsHome;
        } else {
            editedSdk.setSdkHomePath(homePath); // TODO: move the fuck out of this method
        }

        String name = textName.getText().trim();
        if (err == null) {
            badTextControl = textName;
            if (name.length() == 0) {
                err = Messages.EditSdkDialog_EnterNameForXds;
                errType = WizardPage.WARNING;
            } else if (!name.equals(sdkNameInRegistry) && sdkRegistry.findSdk(name) != null) {
                err = Messages.EditSdkDialog_NameIsUsed;
            } else {
                editedSdk.setName(name); // TODO: move the fuck out of this method
            }
        }

        badOnCurrentTab = false;
        if (err == null) {
            // Validate tabs
            int maxErr = WizardPage.NONE;
            for (EditSdkDialogTabAbstract tab : tabs) {
                String  msg[] = new String[]{null};
                int     typ[] = new int[]{0};
                Text    badTxt[] = new Text[]{null};
                if (!tab.validate(msg, typ, badTxt)) {
                    if (tab.isCurrentTab() || // messages from the current tab has high priority
                        maxErr == WizardPage.NONE ||
                        maxErr == WizardPage.WARNING && typ[0] != WizardPage.WARNING) 
                    {
                        err     = msg[0];
                        errType = typ[0];
                        badTextControl = badTxt[0];
                        badTabName = tab.getTabName();
                    }
                    if (tab.isCurrentTab()) {
                        badOnCurrentTab = true;
                        badTabName = null;
                        break; 
                    }
                }
                
            }
        }
        
        if (err != null && badTabName != null) {
            err = "[" + badTabName + "]: " + err; //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        dialogPage.setMessage(err, errType);

        if (err != null) {
            isSdkOk = false;
            if (setFocus && badTextControl != null && badOnCurrentTab) {
                badTextControl.setFocus();
            }
        } else {
            Assert.isTrue(editedSdk.isValid(), "XDS editor validates invalid SDK"); //$NON-NLS-1$
            isSdkOk = true;
        }
          
        dialogPage.setPageComplete(isSdkOk);
        return isSdkOk;
    }	
    
    public static boolean validatePath(String path, boolean directory) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        File f = new File(path); 
        return f.exists() && f.isDirectory() == directory;
    }

	
	private Sdk getSdk() {
	    if (isSdkOk) {
//	        SdkToolsControl.suppressBadSeparators(editedSdk);
	        return editedSdk;
	    }
		return null;
	}
	
	//package scope
	String getHomePathText() {
	    return textHomePath.getText();
    }
	
    //package scope
	Sdk getEditedSdk() {
	    return editedSdk;
	}
	
	@Override
	public void addPages()
	{
        dialogPage = new DialogPage();
		addPage(dialogPage);
	}
	
	@Override
	public boolean canFinish()
	{
		return isSdkOk;
	}

	@Override
	public boolean performFinish() 
	{
	    if (isSdkOk) {
	        for (EditSdkDialogTabAbstract tab : tabs) {
	            tab.performFinish();
	        }
	    }
		return isSdkOk;
	}
	
	@Override
	public boolean performCancel() 
	{
		isSdkOk = false;
		return true;
	}
	
	
	class DialogPage extends WizardPage {
	    private Button btnBrowseHome;

        protected DialogPage() {
            super(""); //$NON-NLS-1$
            setTitle(Messages.EditSdkDialog_Header);
            setDescription(Messages.EditSdkDialog_Description);
        }

        @Override
        public void createControl(Composite parent) {
            
            HelpUtils.setHelp(parent, IXdsHelpContextIds.EDIT_SDK_DLG);

            ScrolledComposite scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
            scroll.setLayout(new FillLayout());
            Composite composite = new Composite(scroll, SWT.NONE);
            scroll.setContent(composite);
            scroll.setExpandVertical(true);
            scroll.setExpandHorizontal(true);

            final int nColumns = 4;
            composite.setLayoutData(new GridData(GridData.FILL_BOTH));
            composite.setLayout(new GridLayout(nColumns, false));

            // XDS home :    [path   ] [Browse]
            SWTFactory.createLabel(composite, Messages.EditSdkDialog_XdsHome+':', 2);
            textHomePath = SWTFactory.createSingleText(composite, 1);
            textHomePath.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    validate(false);
                }
            });
            textHomePath.setText(editedSdk.getSdkHomePath());
            
            btnBrowseHome = SWTFactory.createPushButton(composite, Messages.Common_Browse, null);
            btnBrowseHome.addListener(SWT.Selection, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    String s = SWTFactory.browseDirectory(btnBrowseHome.getShell(),
                            Messages.Common_DirectorySelection,
                            Messages.EditSdkDialog_XdsHomeBrowseText+':',
                            textHomePath.getText().trim());
                    if (s != null) {
                        textHomePath.setText(s);
                        validate(true);
                    }
                }
            });
            
            // XDS name :    [name   ]
            SWTFactory.createLabel(composite, Messages.EditSdkDialog_XdsName+':', 2);
            textName = SWTFactory.createSingleText(composite, 2);
            textName.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    validate(false);
                }
            });
            textName.setText(editedSdk.getName());

            SWTFactory.createVerticalSpacer(composite, 0.1);
            
            // Tab control:
            TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = nColumns;
            tabFolder.setLayoutData(gd);
            
            tabs.add(new EditSdkDialogTabComponents(tabFolder, EditSdkDialog.this));
            tabs.add(new EditSdkDialogTabTemplates(tabFolder, EditSdkDialog.this));
            tabs.add(new EditSdkDialogTabEnvironment(tabFolder, EditSdkDialog.this));
            
            tabFolder.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    validate(false);
                }
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }
                
            });

            scroll.setMinSize(composite.computeSize(SWT.DEFAULT,SWT.DEFAULT)); 
            composite.layout();

            setControl(scroll);
            pageCreated = true;
            validate(true);
        }
	    
	}


}	
	
