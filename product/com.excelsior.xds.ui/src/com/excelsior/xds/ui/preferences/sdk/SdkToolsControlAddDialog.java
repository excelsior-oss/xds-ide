package com.excelsior.xds.ui.preferences.sdk;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkTool;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.internal.nls.Messages;

public class SdkToolsControlAddDialog extends Wizard {
    
    public enum Result {
        TOOL, SEPARATOR, GROUP, ERROR;
    };
    private Result dlgResult;
    
    private SdkTool editedTool;
    private String groupName;
    private Sdk sdk;
    private List<String> menuGroups;
    private IWizardPage editSdkToolPage;
    private IWizardPage editGroupNamePage;

    
    public SdkToolsControlAddDialog(SdkTool editedTool, Sdk sdk, List<String> menuGroups) {
        super();
        this.editedTool = editedTool;
        this.groupName = ""; //$NON-NLS-1$
        this.sdk = sdk;
        this.menuGroups = menuGroups;
        setWindowTitle(Messages.SdkToolsControlAddDialog_Add);
        setForcePreviousAndNextButtons(true);
    }
    
    @Override
    public void addPages() {
        addPage(new SdkToolsControlAddDialogPage());
        editSdkToolPage = new EditSdkToolPage(editedTool, sdk, menuGroups);
        editGroupNamePage = new EditGroupNamePage(Messages.SdkToolsControlAddDialog_NewGroupName, "", menuGroups); //$NON-NLS-1$
   }
    
    @Override
    public boolean performFinish() {
        return true;
    }
    
    public Result getResult() {
        return dlgResult;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    //----------- Page -------------------------------------------------
    
    private class SdkToolsControlAddDialogPage extends WizardPage implements SelectionListener {
        private static final String ID = "com.excelsior.xds.ui.preferences.sdk.SdkToolsControlAddDialogPage"; //$NON-NLS-1$
        
        private Button rbTool; 
        private Button rbSeparator;
        private Button rbGroup;

        protected SdkToolsControlAddDialogPage() {
            super(ID);
        }

        @Override
        public void createControl(Composite parent) {
            initializeDialogUnits(parent);
            setTitle(Messages.SdkToolsControlAddDialog_AddToToolMenu);
            setDescription(Messages.SdkToolsControlAddDialog_Description);
            Composite container = new Composite(parent, SWT.NULL);
            setControl(container);
            container.setLayout(new GridLayout(1, false));

            Composite rbblock = new Composite(container, SWT.NULL);
            GridData gridData = new GridData(GridData.CENTER, GridData.CENTER, true, true);
            rbblock.setLayoutData(gridData);
            GridLayout grid = new GridLayout(1, false);
            grid.verticalSpacing = 20;
            grid.marginRight = 100; 
            rbblock.setLayout(grid);
            
            rbTool      = SWTFactory.createRadiobutton(rbblock, Messages.SdkToolsControlAddDialog_AddTool, 1);
            rbSeparator = SWTFactory.createRadiobutton(rbblock, Messages.SdkToolsControlAddDialog_AddSeparator, 1);
            rbGroup  = SWTFactory.createRadiobutton(rbblock, Messages.SdkToolsControlAddDialog_AddGroup, 1);
            rbSeparator.addSelectionListener(this);
            rbTool.addSelectionListener(this);
            rbGroup.addSelectionListener(this);
            
            rbTool.setSelection(true);
            widgetSelected(null);
        }
        
        // Listeners:
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (rbTool.getSelection()) {
                dlgResult = Result.TOOL;
            } else if (rbSeparator.getSelection()) {
                dlgResult = Result.SEPARATOR;
            } else if (rbGroup.getSelection()) {
                dlgResult = Result.GROUP;
            }
            IWizardContainer cont = getContainer();
            if (cont != null && cont.getCurrentPage()!=null) {
                cont.updateButtons();
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // none
        }
        
        private void addPageOnce(IWizardPage p) {
            IWizardPage[] all = getPages();
            for (IWizardPage x : all) {
                if (p.equals(x)) {
                    return;
                }
            }
            addPage(p);
        }
        @Override 
        public IWizardPage getNextPage() {
            // lazy page adding allows to increase dialog size only when need
            if (rbTool.getSelection()) {
                addPageOnce(editSdkToolPage);
                return editSdkToolPage;
            } else if (rbGroup.getSelection()) {
                addPageOnce(editGroupNamePage);
                return editGroupNamePage;
            } else {
                return null;
            }
        }
        
        @Override 
        public boolean canFlipToNextPage() {
            return rbTool.getSelection() || rbGroup.getSelection();
        }
        
        @Override public boolean isPageComplete() {
            return rbSeparator.getSelection() || !isCurrentPage();
        }
    }
    
    private class EditGroupNamePage extends WizardPage {
        private static final String ID = "com.excelsior.xds.ui.preferences.sdk.SdkToolsControlAddDialog.EditGroupNamePage"; //$NON-NLS-1$
        private Collection<String> usedNames;
        private Text text;

        protected EditGroupNamePage(String title, String initialName, Collection<String> usedNames) {
            super(ID);
            setTitle(title);
            setDescription(Messages.SdkToolsControlAddDialog_EnterGroupName);
            this.usedNames = usedNames;
        }

        @Override
        public void createControl(Composite parent) {
            initializeDialogUnits(parent);
            Composite container = new Composite(parent, SWT.NULL);
            setControl(container);

            container.setLayout(new GridLayout(2, false));

            SWTFactory.createVerticalSpacer(container, 2.0);
            SWTFactory.createLabel(container, Messages.SdkToolsControlAddDialog_GroupName + ':', 1);
            text = SWTFactory.createSingleText(container,  1);
            text.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    validatePage();
                }
            });
            
            text.setText(groupName);
            text.setFocus();
            validatePage();
        }
        
        protected boolean validatePage() {
            String err = null;
            groupName = text.getText().trim();
            if (groupName.isEmpty()) {
                err = Messages.SdkToolsControlAddDialog_NameIsEmpty;
            } else if (usedNames.contains(groupName)) {
                err = Messages.SdkToolsControlAddDialog_NameIsUsed;
            }
            setMessage(err, ERROR);

            setPageComplete(err == null);
            return err == null;
        }
        
        @Override
        public boolean isPageComplete() {
            return !isCurrentPage() || super.isPageComplete();
        }

    }

}
