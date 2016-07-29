package com.excelsior.xds.ui.preferences.sdk;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.excelsior.xds.core.help.IXdsHelpContextIds;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkIniFileReader;
import com.excelsior.xds.core.sdk.SdkIniFileWriter;
import com.excelsior.xds.core.sdk.SdkManager;
import com.excelsior.xds.core.sdk.SdkRegistry;
import com.excelsior.xds.ui.commons.utils.HelpUtils;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.commons.utils.SwtUtils;
import com.excelsior.xds.ui.internal.nls.Messages;

/**
 * The Registered SDKs preference page.
 */
public class SDKsPreferencePage extends    PreferencePage 
                                implements IWorkbenchPreferencePage 
{
        /**
         * ID for the page
         */
        public static final String ID = "com.excelsior.xds.ui.preferences.SDKsPreferencePage"; //$NON-NLS-1$
        
        private CheckboxTableViewer sdkTableViewer;
        private SdkRegistry sdkRegistry;
        private Button addSdkButton;
        private Button editSdkButton;
        private Button removeSdkButton;
        private Button exportSdkButton;
        
        @SuppressWarnings("unused")
		private String initialActiveSdkName;

        /**
         * @wbp.parser.constructor
         */
        public SDKsPreferencePage() {
        }

        public SDKsPreferencePage(String title) {
                super(title);
        }

        public SDKsPreferencePage(String title, ImageDescriptor image) {
                super(title, image);
        }

        @Override
        public void init(IWorkbench workbench) {
        }

        @Override
        protected Control createContents(Composite parent) {
        	sdkRegistry = SdkManager.getInstance().loadSdkRegistry();
            HelpUtils.setHelp(parent, IXdsHelpContextIds.MODULA2_SDKS_PREFERENCE_PAGE);
            
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayoutData(new GridData(GridData.FILL_BOTH));
            composite.setLayout(SwtUtils.removeMargins(new GridLayout(2, false)));
            
            SWTFactory.createWrapLabel(composite, Messages.SDKsPreferencePage_Description, 2, 300);
            SWTFactory.createVerticalSpacer(composite, 1);
            SWTFactory.createLabel(composite, Messages.SDKsPreferencePage_RegisteredSDKs+':', 2);
            
            Composite sdkTableComposite = new Composite(composite, SWT.NONE);
            sdkTableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            sdkTableComposite.setLayout(new GridLayout(1, false));
            sdkTableViewer = CheckboxTableViewer.newCheckList(sdkTableComposite, SWT.FULL_SELECTION | SWT.BORDER);
            final Table table = sdkTableViewer.getTable();
            table.setLayoutData(new GridData(GridData.FILL_BOTH));
            sdkTableViewer.setContentProvider(new SdkTableContentProvider());
            sdkTableViewer.setLabelProvider(new SdkTableLabelProvider());
            sdkTableViewer.addCheckStateListener(new SdkTableCheckStateListener());
            table.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                            manageEnableState();
                    }

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                            manageEnableState();
                    }
            });
            
            TableColumn tc = new TableColumn(table, SWT.LEFT);
            tc.setText("WWWWWWWW"); //$NON-NLS-1$
            SwtUtils.resizeColumnByCaption(table, 0);
            tc.setText(Messages.SDKsPreferencePage_SdkName);
            
            tc = new TableColumn(table, SWT.LEFT);
            tc.setText("WWWWWWWWWWWWWW"); //$NON-NLS-1$
            SwtUtils.resizeColumnByCaption(table, 1);
            tc.setText(Messages.SDKsPreferencePage_SdkLocation);
            
            // Turn on the header and the lines
            table.setHeaderVisible(true);
            table.setLinesVisible(true);
        
            sdkTableViewer.setInput(sdkRegistry);
            //UiUtils.resizeColumsByContent(table);
            
            checkActiveSdk();
            
            Composite buttonsComposite = new Composite(composite, SWT.NONE);
            buttonsComposite.setLayout(SwtUtils.removeMargins(new GridLayout(1, false)));
            buttonsComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING));
            addSdkButton = new Button(buttonsComposite, SWT.PUSH);
            addSdkButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            addSdkButton.setText(Messages.Common_Add);
            addSdkButton.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected(SelectionEvent e) {
                    String sdkHomePath = SWTFactory.browseDirectory(getShell(), Messages.Common_DirectorySelection, Messages.SDKsPreferencePage_SelectXdsHome+':');
                    if (sdkHomePath == null)
                        return; // Cancelled

                    SdkIniFileReader sdkReader = new SdkIniFileReader(sdkHomePath);
                    Sdk    aSdk[] = sdkReader.getSdk();
                    String errs   = sdkReader.getError();
                    SdkManager.getInstance().loadSdkRegistry().makeSdkNameUnique(aSdk);
                    boolean sdkFromIni = false;
                    Sdk sdkToEdit = SdkManager.createSdk(FilenameUtils.getName(sdkHomePath), sdkHomePath);
                    sdkToEdit.beginEdit();
                    if (errs.length() > 0) {
                        // Sdk.ini found but can't be parsed:
                        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
                        messageBox.setText(Messages.Common_Warning);
                        messageBox.setMessage(String.format(Messages.SDKsPreferencePage_CantReadSdk_EditManualy, errs));
                        if (SWT.YES != messageBox.open()) {
                            sdkToEdit = null;
                        }
                    }
                    if (sdkToEdit != null)  {
                        boolean isRegChanged = false;
                        boolean sdkNameInRegistry = false;
                        if (aSdk.length > 0) {
                            sdkToEdit = null;
                            // Sdk.ini found and parsed. Add SDK[s] from here:
                            for (Sdk s : aSdk) {
                                sdkRegistry.addSdk(s);
                                isRegChanged = true;
                                sdkNameInRegistry = true;
                                if (sdkToEdit == null && !s.isValid()) {
                                    sdkToEdit = s; // Start edit for first invalid SDK
                                    sdkFromIni = true;
                                }
                            }
                            setInput(); // update list
                        }
                        
                        if (sdkToEdit != null) {
                            if (!sdkFromIni) {
                                // Add what we can find. For sdkFromIni it is already added.
                                SdkIniFileReader.addSettingsFromLocation(sdkToEdit);
                            }
                            Sdk editedSdk = EditSdkDialog.playDialog(getShell(), !sdkNameInRegistry, sdkToEdit, sdkRegistry); // return null when it is valid and 'Ok' clicked
                            if (editedSdk != null) {
                                if (sdkNameInRegistry) {
                                    sdkRegistry.editSdk(sdkToEdit, editedSdk);
                                } else {
                                    sdkRegistry.addSdk(editedSdk);
                                    isRegChanged = true;
                                }
                            }
                        }

                        if (isRegChanged) {
                            setInput(); 
                            getApplyButton().setEnabled(true);
                            manageEnableState();
                        }
                    }
                }
            });
            

            editSdkButton = new Button(buttonsComposite, SWT.PUSH);
            editSdkButton.setText(Messages.Common_Edit);
            editSdkButton.setEnabled(false);
            editSdkButton.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Sdk oldSdk    = getSelectedSdk();
                    Sdk editedSdk = EditSdkDialog.playDialog(getShell(), false, oldSdk, sdkRegistry); // sdk = not null when valid Sdk and 'Ok' clicked
                    if (editedSdk != null) { 
                    	if (!oldSdk.isBeingEdited()) {
                    		oldSdk.beginEdit();
                    	}
                        sdkRegistry.editSdk(oldSdk, editedSdk);
                        setInput();
                        getApplyButton().setEnabled(true);
                    }
                    manageEnableState();
                }
            });
            
            removeSdkButton = new Button(buttonsComposite, SWT.PUSH);
            removeSdkButton.setText(Messages.Common_Remove);
            removeSdkButton.setEnabled(false);
            removeSdkButton.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Sdk sdk = getSelectedSdk();
                    if (sdk != null) {
                    	sdk.cancelEdit();
                    	SdkManager.getInstance().removeSdk(sdk);
                        setInput();
                        manageEnableState();
                        getApplyButton().setEnabled(true);
                    }
                }
            });

            SWTFactory.createVerticalSpacer(buttonsComposite, 1.0);

            exportSdkButton = new Button(buttonsComposite, SWT.PUSH);
            exportSdkButton.setText(Messages.Common_Export);
            exportSdkButton.setEnabled(false);
            exportSdkButton.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Sdk sdk = getSelectedSdk();
                    if (sdk != null) {
                        exportSdk();
                    }
                }
            });

            { // Set buttons sizes :
                int widthHint = Math.max(addSdkButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x, convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH));
                widthHint     = Math.max(editSdkButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x, widthHint);
                
                widthHint     = Math.max(removeSdkButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x, widthHint);
                
                GridData gd = new GridData(GridData.FILL_HORIZONTAL);
                gd.widthHint = widthHint;
                addSdkButton.setLayoutData(gd);
                
                gd = new GridData(GridData.FILL_HORIZONTAL);
                gd.widthHint = widthHint;
                editSdkButton.setLayoutData(gd);
                
                gd = new GridData(GridData.FILL_HORIZONTAL);
                gd.widthHint = widthHint;
                removeSdkButton.setLayoutData(gd);

                gd = new GridData(GridData.FILL_HORIZONTAL);
                gd.widthHint = widthHint;
                exportSdkButton.setLayoutData(gd);
            }

            {
                Sdk sdk = sdkRegistry.getDefaultSdk();
                initialActiveSdkName = sdk == null ? "" : sdk.getName();  //$NON-NLS-1$
            }
            
            return composite;
        }
        
        private void manageEnableState(){
                boolean isSdkSelected = sdkTableViewer.getTable().getItemCount() > 0;
                isSdkSelected = isSdkSelected && sdkTableViewer.getTable().getSelectionCount() > 0;
                
                editSdkButton.setEnabled(isSdkSelected);
                removeSdkButton.setEnabled(isSdkSelected);
                exportSdkButton.setEnabled(isSdkSelected);
        }
        
        private void setInput(){
                sdkTableViewer.setInput(sdkRegistry);
                checkActiveSdk();
        }
        
        private Sdk getSelectedSdk(){
                int selected = sdkTableViewer.getTable().getSelectionIndex();
                if (selected > -1) {
                        TableItem item = sdkTableViewer.getTable()
                                        .getItem(selected);
                        return (Sdk) item.getData();
                }
                return null;
        }

        private void checkActiveSdk() {
                Sdk activeSdk = sdkRegistry.getDefaultSdk();
                if (activeSdk != null) sdkTableViewer.setChecked(activeSdk, true);
        }
        
        private void exportSdk() {
            Sdk sdk = getSelectedSdk();
            if (sdk != null) {
                String fname = SWTFactory.browseFile(this.getShell(), true, Messages.SDKsPreferencePage_SaveSdkAs, new String[]{"*.ini"}, null);  //$NON-NLS-1$
                if (fname != null) {
                    if (!FilenameUtils.isExtension(fname.toLowerCase(), "ini")) {  //$NON-NLS-1$
                        fname = fname + ".ini";  //$NON-NLS-1$
                    }
                    if (new File(fname).exists()) {
                        if (!SWTFactory.YesNoQuestion(getShell(), Messages.SDKsPreferencePage_AskOverwriteTitle, String.format(Messages.SDKsPreferencePage_AskOverwriteQuestion, fname))) {
                            return;
                        }
                    }
                    SdkIniFileWriter.exportSdk(sdk, fname);
                }
            }
        }

        
        public class SdkTableCheckStateListener implements ICheckStateListener {
            private boolean isCancelEvents = false;
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (isCancelEvents) return;
                try{
                    isCancelEvents = true;

                    Sdk activeSdk = sdkRegistry.getDefaultSdk();
                    if (activeSdk == event.getElement() && !event.getChecked()) {
                        sdkTableViewer.setChecked(activeSdk, true);
                    }
                    else if (activeSdk != event.getElement()) {
                        if (activeSdk != null) {
                            sdkTableViewer.setChecked(activeSdk, false);
                        }
                        activeSdk = (Sdk)event.getElement();
                        sdkRegistry.setDefaultSdk(activeSdk.getName());
                        getApplyButton().setEnabled(true);

                    }
                }
                finally{
                    isCancelEvents = false;
                }
            }
        }

        public class SdkTableLabelProvider implements ITableLabelProvider, ITableColorProvider {

                @Override
                public void addListener(ILabelProviderListener listener) {
                }

                @Override
                public void dispose() {
                }

                @Override
                public boolean isLabelProperty(Object element, String property) {
                        return false;
                }

                @Override
                public void removeListener(ILabelProviderListener listener) {
                }

                @Override
                public Image getColumnImage(Object element, int columnIndex) {
                        return null;
                }

                @Override
                public String getColumnText(Object element, int columnIndex) {
                        Sdk sdk = (Sdk)element;
                        String text = null;
                        switch(columnIndex){
                        case 0:
                                text = sdk.getName();
                                break;
                        case 1:
                                text = sdk.getSdkHomePath();
                                break;
                        }
                        return text;
                }
                
                @Override
             public Color getBackground(Object element, int columnIndex) {
                   return null;
             }

             @Override
             public Color getForeground(Object element, int columnIndex) {
                 if (!((Sdk)element).isValid()) 
                         return new Color(Display.getDefault(), new RGB(255,0,0));
                 return null; 
             }
        }

        public class SdkTableContentProvider implements IStructuredContentProvider {
                @Override
                public void dispose() {
                }

                @Override
                public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                }

                @Override
                public Object[] getElements(Object inputElement) {
                        SdkRegistry sdkRegistry = (SdkRegistry)inputElement;
                        if (sdkRegistry != null) {
                                return sdkRegistry.getRegisteredSDKs().toArray();
                        }
                        return null;
                }
        }

        @Override
        public void createControl(Composite parent) {
                super.createControl(parent);
                
                getDefaultsButton().setVisible(false);
                getApplyButton().setEnabled(false);
                getApplyButton().addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                                getApplyButton().setEnabled(false);
                        }
                });
        }

        @Override
    public boolean performOk() {
        savePreferences();
        return super.performOk();
    }
        
	@Override
    public boolean performCancel() {
        SdkManager.getInstance().unloadSdkRegistry();
        return super.performCancel();
    }
        
    private void savePreferences() {
    	SdkManager.getInstance().saveSdkRegistry(sdkRegistry);
    }
}