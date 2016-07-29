package com.excelsior.xds.ui.preferences.sdk;

import org.apache.commons.lang.text.StrTokenizer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.internal.nls.Messages;

public abstract class EditSdkDialogTabAbstract extends Composite implements Listener {
    
    private static final String NOT_SUPPORTED_TEXT = Messages.EditSdkDialog_NotSupportedText;
    protected final int nColumns = 4;

    protected EditSdkDialog editSdkDialog;
    protected TabItem tabItem;

    
    protected XdsComponent xdsComponents[] = new XdsComponent[] {
            
            // This array enumerates all items of each tab in the dialog
            // (see createTabControls())
            
            // Element type depends on element class:
            //   XdsComponent        - checkbox + text field + browse button
            //   XdsComponentDirList - checkbox + text field (for directories to create)
            //   XdsComponentExeExt  - gray checkbox + text field (for executable extension)
            //   XdsEnvironmentBlock - control with environment variables table
            //   XdsToolsBlock       - control with tools 
            //   XdsSeparator        - (optional) separator line + (optional) bold text line
            
            // The tab where element will be shown depends on TAB ID:
            //   EditSdkDialogTabComponents.TAB_NAME
            //   EditSdkDialogTabTemplates.TAB_NAME
            //   EditSdkDialogTabEnvironment.TAB_NAME
            
            new XdsSeparator(EditSdkDialogTabComponents.TAB_NAME,
                             false, false,
                             Messages.EditSdkDialog_Components),                                 

            new XdsComponent(EditSdkDialogTabComponents.TAB_NAME,
                             Sdk.Property.XDS_COMPILER, 
                             Messages.EditSdkDialog_Compiler+':', 
                             Messages.EditSdkDialog_EnterCompilerPath, 
                             Messages.EditSdkDialog_InvalidCompilerPath,
                             Messages.EditSdkDialog_CompilerBrowseText, 
                             "*.exe", "*.bat"),  //$NON-NLS-1$  //$NON-NLS-2$
                             
            new XdsComponent(EditSdkDialogTabComponents.TAB_NAME,
                             Sdk.Property.XDS_DEBUGGER, 
                             Messages.EditSdkDialog_Debugger+':', 
                             Messages.EditSdkDialog_EnterDebuggerPath, 
                             Messages.EditSdkDialog_InvalidDebuggerPath,
                             Messages.EditSdkDialog_DebuggerBrowseText, 
                             "*.exe", "*.bat"),  //$NON-NLS-1$  //$NON-NLS-2$

            new XdsComponent(EditSdkDialogTabComponents.TAB_NAME,
                             Sdk.Property.XDS_PROFILER, 
                             Messages.EditSdkDialogTabAbstract_Profiler+':', 
                             Messages.EditSdkDialogTabAbstract_EnterProfilerPath, 
                             Messages.EditSdkDialogTabAbstract_InvalidProfilerPath,
                             Messages.EditSdkDialogTabAbstract_ProfilerBrowseText, 
                             "*.exe", "*.bat"),  //$NON-NLS-1$  //$NON-NLS-2$

            new XdsComponent(EditSdkDialogTabComponents.TAB_NAME,
                             Sdk.Property.XDS_SIMULATOR, 
                             Messages.EditSdkDialog_Simulator+':', 
                             Messages.EditSdkDialog_EnterSimulatorPath, 
                             Messages.EditSdkDialog_InvalidSimulatorPath,
                             Messages.EditSdkDialog_SimulatorBrowseText, 
                             "*.exe", "*.bat"),  //$NON-NLS-1$  //$NON-NLS-2$

            new XdsComponent(EditSdkDialogTabComponents.TAB_NAME,
                             Sdk.Property.XDS_LIB_DEFS_PATH, 
                             Messages.EditSdkDialog_LibraryDefs+':', 
                             Messages.EditSdkDialog_EnterLibraryDefsPath, 
                             Messages.EditSdkDialog_InvalidLibraryDefsPath,
                             Messages.EditSdkDialog_LibraryDefsBrowseText+':'),
            
            new XdsSeparator(EditSdkDialogTabComponents.TAB_NAME,
                             false, true,
                             Messages.EditSdkDialog_ExtraTools),                                 
                            
            new XdsToolsBlock(EditSdkDialogTabComponents.TAB_NAME),                                 

//            new XdsSeparator(EditSdkDialogTabComponents.TAB_NAME, true, null),
//            
//            new XdsComponent(EditSdkDialogTabComponents.TAB_NAME,
//                             Sdk.Property.XDS_UPDATE_MANIFEST, 
//                             Messages.EditSdkDialog_UpdateManifest+':', 
//                             Messages.EditSdkDialog_EnterUpdateManifestPath, 
//                             Messages.EditSdkDialog_InvalidUpdateManifestPath, 
//                             Messages.EditSdkDialog_UpdateManifestBrowseText, 
//                              "*.xml"),  //$NON-NLS-1$                 
         
            new XdsSeparator(EditSdkDialogTabComponents.TAB_NAME, false, null),
                  
            
            // Templates tab:
            new XdsSeparator(EditSdkDialogTabTemplates.TAB_NAME,
                    false, false,
                    Messages.EditSdkDialog_FilesTypes),
                    
            new XdsComponentExeExt(EditSdkDialogTabTemplates.TAB_NAME,
                    Sdk.Property.XDS_EXE_EXTENSION, 
                    Messages.EditSdkDialog_ExeFileExtension+':', 
                    Messages.EditSdkDialog_EnterExeFileExtension, 
                    Messages.EditSdkDialog_InvalidExeFileExtension),
                    
            new XdsComponentExtLst(EditSdkDialogTabTemplates.TAB_NAME,
                    Sdk.Property.XDS_PRIM_EXTENSIONS, 
                    Messages.EditSdkDialog_PrimaryFilesExtensions+':', 
                    null, // warning when empty: null => no warn 
                    Messages.EditSdkDialog_PrimExtListExpected),

            new XdsSeparator(EditSdkDialogTabTemplates.TAB_NAME,
                             false, true,
                             Messages.EditSdkDialog_ProjectTemplates),                                 

            new XdsComponent(EditSdkDialogTabTemplates.TAB_NAME,
                             Sdk.Property.XDS_TPR_FILE, 
                             Messages.EditSdkDialog_PrjTemplate+':', 
                             Messages.EditSdkDialog_EnterPrjTemplateName, 
                             Messages.EditSdkDialog_InvalidPrjTemplate,
                             Messages.EditSdkDialog_PrjTemplateBrowseTxt, 
                             "*.tpr"),  //$NON-NLS-1$
                             
            new XdsComponent(EditSdkDialogTabTemplates.TAB_NAME,
                             Sdk.Property.XDS_TRD_FILE, 
                             Messages.EditSdkDialog_RedTemplate+':', 
                             Messages.EditSdkDialog_EnterRedTemplateName, 
                             Messages.EditSdkDialog_InvalidRedTemplate,
                             Messages.EditSdkDialog_RedTemplateBrowseTxt, 
                             "*.trd"),  //$NON-NLS-1$
                             
            new XdsComponent(EditSdkDialogTabTemplates.TAB_NAME,
                    Sdk.Property.XDS_MAIN_MOD_FILE, 
                    Messages.EditSdkDialog_MainTemplate+':', 
                    Messages.EditSdkDialog_EnterMainTemplateName, 
                    Messages.EditSdkDialog_InvalidMainTemplate,
                    Messages.EditSdkDialog_MainTemplateBrowseTxt, 
                    "*.tmd"),  //$NON-NLS-1$
                    
            new XdsComponent(EditSdkDialogTabTemplates.TAB_NAME,
                    Sdk.Property.XDS_TDEF_FILE, 
                    Messages.EditSdkDialog_DefTemplate+':', 
                    Messages.EditSdkDialog_EnterDefTemplateName, 
                    Messages.EditSdkDialog_InvalidDefTemplate,
                    Messages.EditSdkDialog_DefTemplateBrowseTxt, 
                    "*.tmd"),  //$NON-NLS-1$  
                    
            new XdsComponent(EditSdkDialogTabTemplates.TAB_NAME,
                    Sdk.Property.XDS_TMOD_FILE, 
                    Messages.EditSdkDialog_ModuleTemplate+':', 
                    Messages.EditSdkDialog_EnterModuleTemplateName, 
                    Messages.EditSdkDialog_InvalidmoduleTemplate,
                    Messages.EditSdkDialog_ModuleTemplateBrowseTxt, 
                    "*.tmd"),  //$NON-NLS-1$        

            new XdsComponent(EditSdkDialogTabTemplates.TAB_NAME,
                    Sdk.Property.XDS_TOB2_FILE, 
                    Messages.EditSdkDialogTabAbstract_Ob2ModuleTemplate+':', 
                    Messages.EditSdkDialogTabAbstract_EnterLocationOfModue, 
                    Messages.EditSdkDialogTabAbstract_InvalidLocation,
                    Messages.EditSdkDialogTabAbstract_SelectOb2ModuleTemplate, 
                    "*.tmd"),  //$NON-NLS-1$        

            new XdsSeparator(EditSdkDialogTabTemplates.TAB_NAME,
                    false, true,
                    Messages.EditSdkDialogTabAbstract_FoldersLayout),
                    

            new XdsComponentDirsLst(EditSdkDialogTabTemplates.TAB_NAME,
                    Sdk.Property.XDS_DIRS_TO_CREATE, 
                    Messages.EditSdkDialog_DirsToCreate+':', 
                    null, // warning when empty: null => no warn 
                    Messages.EditSdkDialog_DirsListExpected),

            new XdsComponentSubfolderName(EditSdkDialogTabTemplates.TAB_NAME,
                    Sdk.Property.XDS_FOLDER_PRJ_FILE, 
                    Messages.EditSdkDialogTabAbstract_ProjectFileFolder+':', 
                    null, // warning when empty: null => no warn 
                    Messages.EditSdkDialogTabAbstract_InvalidPrjFileFolderName),

            new XdsComponentSubfolderName(EditSdkDialogTabTemplates.TAB_NAME,
                    Sdk.Property.XDS_FOLDER_MAIN_MODULE, 
                    Messages.EditSdkDialogTabAbstract_MainModuleFolder+':', 
                    null, // warning when empty: null => no warn 
                    Messages.EditSdkDialogTabAbstract_IvvalidMainModuleFolder),

            
            // Environment tab:
                    
            new XdsSeparator(EditSdkDialogTabEnvironment.TAB_NAME,
                    false, false,
                    Messages.EditSdkDialog_EnvironmentVariables),                                 

            new XdsEnvironmentBlock(EditSdkDialogTabEnvironment.TAB_NAME),                                 

            new XdsSeparator(EditSdkDialogTabEnvironment.TAB_NAME, false, null),

    };
    

    protected EditSdkDialogTabAbstract(TabFolder tabFolder, EditSdkDialog editSdkDialog) {
        super(tabFolder, SWT.NONE);
        this.editSdkDialog = editSdkDialog;
        tabItem = new TabItem(tabFolder, SWT.NULL);
        tabItem.setText(getTabName());
        tabItem.setControl(this);
        createTabControls();
    }
    
    public boolean isCurrentTab() {
        TabItem sel[] = tabItem.getParent().getSelection();
        return (sel.length>0 && sel[0]==tabItem);
    }
    
    protected void createTabControls() {
        //---- Create control: 
        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        setLayout(layout);
        setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Control ctr;
        
        // XDS components :
        for (XdsComponent xc : xdsComponents) {
            if (!getTabName().equals(xc.tabId)) {
                continue;
            }

            // Create (next) control row in the current tab:
            if (xc instanceof  XdsToolsBlock) {
                // XDS Tools block:
                SdkToolsControl sdkToolsControl = new SdkToolsControl(this, SWT.NONE, editSdkDialog.getEditedSdk());
                GridData sdkToolsControlGridData = new GridData(GridData.FILL_HORIZONTAL);
                sdkToolsControlGridData.horizontalSpan = nColumns;
                sdkToolsControl.setLayoutData(sdkToolsControlGridData);
                ((XdsToolsBlock)xc).setCtrl(sdkToolsControl);
                
                
            } else if (xc instanceof  XdsEnvironmentBlock) {
                // XDS Environment block:
                SdkEnvironmentControl sdkEnvControl = new SdkEnvironmentControl(this, SWT.NONE, editSdkDialog.getEditedSdk());
                GridData sdkEnvControlGridData = new GridData(GridData.FILL_BOTH);
                sdkEnvControlGridData.horizontalSpan = nColumns;
                sdkEnvControl.setLayoutData(sdkEnvControlGridData);
                
            } else if (xc.isItem()) {
                //   [v] ComponentName: [                     ] [Browse]
                String val = editSdkDialog.getEditedSdk().getPropertyValue(xc.propId);
                if (val == null) val = ""; //$NON-NLS-1$
                xc.setSupported(!xc.getSdkUnsupportedValue().equals(val));
                
                int labelhspan = 1;
                if (xc.isCBoxRequired()) {
                    xc.cbSupported  = SWTFactory.createCheckbox(this,  null, 1);
                    xc.cbSupported.setSelection(xc.isSupported());
                    xc.cbSupported.addSelectionListener(new SelectionAdapter(){
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            reenableAll(getTabName(), false);
                        }
                    });
                } else {
                    // no create checkbox
                    labelhspan = 2; 
                }
                xc.labelLabel   = SWTFactory.createLabel(this, xc.label, labelhspan);
                if (xc.cbSupported != null) {
                    xc.labelLabel.addMouseListener(new LabelMouseListener(xc.cbSupported));
                }
                if (xc.isBrowsable()) {
                    xc.textPath     = SWTFactory.createSingleText(this, 1);
                    xc.btnBrowse    = SWTFactory.createPushButton(this, Messages.Common_Browse, null);
                    xc.btnBrowse.addListener(SWT.Selection, this);
                } else {
                    xc.textPath     = SWTFactory.createSingleText(this, 2);
                }
                xc.textPath.addModifyListener(new ValidateModifyListener(xc));
                xc.textPath.setText(val);
            } else {
                // XdsSeparator:
                XdsSeparator xs = (XdsSeparator)xc;
                Composite sepComposite = new Composite(this, SWT.NONE);
                GridLayout sepLayout = new GridLayout();
                sepLayout.numColumns = 1;
                sepLayout.verticalSpacing = 0;
                sepLayout.marginWidth = 0;
                sepComposite.setLayout(sepLayout);
                GridData gd = new GridData(GridData.FILL_HORIZONTAL);
                gd.horizontalSpan = nColumns;
                gd.grabExcessHorizontalSpace = false;
                sepComposite.setLayoutData(gd);
                
                int cyLine = SWTFactory.getCharHeight(sepComposite);
                if (xs.drawLine) {
                    // ------------------------------
                    ctr = SWTFactory.createSeparator(sepComposite, 1, cyLine);
                    ((GridData)ctr.getLayoutData()).grabExcessHorizontalSpace = true;
                } else if (xs.insertFreeSpace) {
                    SWTFactory.createFreeSpace(sepComposite, 1, cyLine/2, 1);
                }

                String txt = ((XdsSeparator)xc).separatorText;
                if (txt != null) {
                    // Separator text:
                    ctr = SWTFactory.createLabel(sepComposite, txt, 1);
                    ((GridData)ctr.getLayoutData()).grabExcessHorizontalSpace = true;
                    FontData fd = ctr.getFont().getFontData()[0];
                    ctr.setFont(new Font(Display.getDefault(), fd.getName(), fd.getHeight(), SWT.BOLD));
                }
                
                xs.separatorComposite = sepComposite;
                 
            }
        }
        
        reenableAll(getTabName(), true);
        editSdkDialog.validate(false);
    }
    
    public void reenableAll(String tabId, boolean forAll) {
        for (XdsComponent xc : xdsComponents) {
            if (tabId.equals(xc.tabId) && xc.isItem()) {
                boolean supp = true;
                if (xc.cbSupported!=null) {
                    if (Sdk.Property.XDS_COMPILER.equals(xc.propId) ||
                        Sdk.Property.XDS_EXE_EXTENSION.equals(xc.propId) ||
                        Sdk.Property.XDS_PRIM_EXTENSIONS.equals(xc.propId)) 
                    {
                        xc.cbSupported.setSelection(true);
                        xc.cbSupported.setEnabled(false);
                    }
                    supp = xc.cbSupported.getSelection();
                }
                if (supp != xc.isSupported() || forAll) {
                    xc.setSupported(supp);
                    xc.showSupportedState();
                }
            }
        }
        editSdkDialog.validate(false);
    }

    // Tab unical readable name (used as tab identifier, as tab title and used to show in [] in error messages) 
    public abstract String getTabName();
    
    public boolean validate(String pErr[], int pErrType[], Text pBadTxt[]) {
        for (XdsComponent xc : xdsComponents) {
            if (!xc.isItem() || !xc.tabId.equals(this.getTabName())) continue;

            pErr[0] = xc.validate(pErrType, pBadTxt);
            if (pErr[0] != null) {
                tabItem.setImage(ImageUtils.getImage(ImageUtils.ERROR_16x16));
                return false;
            }
        }
        tabItem.setImage(null);
        return true;
    }

    public void performFinish() {
        for (XdsComponent xc : xdsComponents) {
            xc.performFinish(); // required to commit changes in tools control
        }
    }

    @Override
    public void handleEvent(Event e) {
        for (XdsComponent xc : xdsComponents) {
            if (!xc.isItem()) continue;
            if (xc.isSupported() && e.widget == xc.btnBrowse) {
                if (xc.doBrowse()) {
                    editSdkDialog.validate(true);
                }
            }
        }
    }
    
    

    //---  XDS component items:
    //
    protected class XdsComponent {
        
        public final Sdk.Property propId;
        public final String tabId;
        public final String label;
        public final String warningEmpty;  // null when empty value is legal
        public final String errorInvalid;
        public final String browseText;    // null when no browsable
        public final String[] browseExt;   // array of file extensions OR empty array for directories OR null to disable browse capability
        
        protected boolean  supported;
        protected String   notSupportedText;
        public String  strPath;
        public Button  cbSupported;
        public Label   labelLabel;
        public Text    textPath;
        public Button  btnBrowse;

        public XdsComponent(String tabId, Sdk.Property propId, String label, String warningEmpty, 
                            String warningInvalid, String browseText, String... browseExt) {
            this.tabId = tabId; // use tab name as ID
            this.propId = propId;
            this.label = label;
            this.warningEmpty = warningEmpty;
            this.errorInvalid = warningInvalid;
            this.browseText = browseText;
            this.browseExt = browseExt;
            strPath = ""; //$NON-NLS-1$
            notSupportedText = NOT_SUPPORTED_TEXT;
        }

        public boolean isItem() {
            return true;
        }
        
        public void setSupported(boolean b) {
            supported = b;
        }
        
        public boolean isSupported() {
            return supported;
        }

        public void showSupportedState() {
            textPath.setText(supported ? strPath : notSupportedText);
            //labelLabel.setEnabled(supported);
            textPath.setEnabled(supported);
            if (btnBrowse != null) {
                btnBrowse.setEnabled(supported);
            }
        }
        
        public String getSdkUnsupportedValue() {
            return Sdk.NOT_SUPPORTED;
        }

        public String isValid(String value) {
            if (!EditSdkDialog.validatePath(value, isDirectory())) {
                return errorInvalid;
            }
            return null;
        }
        
        public String validate(int errType[], Text badText[]) {
            if (!isItem()) {
                return null;
            }
            String res = null;
            if (isSupported()) {
                badText[0] = textPath;
                String val = textPath.getText().trim();
                if (val.length() == 0) {
                    res = warningEmpty; // null when empty is legal
                    errType[0] = WizardPage.WARNING; 
                } else {
                    res = isValid(val);
                    errType[0] = WizardPage.ERROR; 
                }
                if (res == null){
                    editSdkDialog.getEditedSdk().setPropertyValue(propId, val); // TODO: move the fuck out of this method
                }
            } else {
                editSdkDialog.getEditedSdk().setPropertyValue(propId, getSdkUnsupportedValue());  // TODO: move the fuck out of this method
            }
            return res;
        }
        
        public boolean isBrowsable() {
            return browseExt != null;
        }
        
        public boolean isDirectory() {
            return (browseExt.length == 0);
        }
        public boolean doBrowse() {
            String iniPath = textPath.getText().trim();
            if (iniPath.isEmpty()) {
                iniPath = editSdkDialog.getHomePathText();
            }
            
            String s = null;
            if (isDirectory()) { // it is directory
                s = SWTFactory.browseDirectory(editSdkDialog.getShell(),
                    Messages.Common_DirectorySelection,
                    browseText,
                    iniPath);
            } else { // it is file
                s = SWTFactory.browseFile(editSdkDialog.getShell(), false,
                    browseText, browseExt, iniPath);
            }
            if (s != null) {
                textPath.setText(s);
                return true;
            }
            return false;
        }
        
        public boolean isCBoxRequired() {
            return true;
        }
        
        public void performFinish() {
        }
        
    }


    protected class XdsComponentWithoutBrowse extends XdsComponent {
        
        public XdsComponentWithoutBrowse( String tabId, Sdk.Property propId
                                        , String label
                                        , String warningEmpty 
                                        , String warningInvalid) 
        {
            super(tabId, propId, label, warningEmpty, warningInvalid, null, (String[])null);
        }
    }
    
    
    protected class XdsComponentExtLst extends XdsComponentWithoutBrowse {

        public XdsComponentExtLst(String tabId, Sdk.Property propId, String label, String warningEmpty, String warningInvalid) 
        {
            super(tabId, propId, label, warningEmpty, warningInvalid);
            notSupportedText = ""; //$NON-NLS-1$
        }
        
        public void setSupported(boolean b) {
            supported = b;
        }

        public String getSdkUnsupportedValue() {
            return ""; //$NON-NLS-1$
        }
        
        public String isValid(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            String arr[] = new StrTokenizer(value, ';', '"').getTokenArray();
            for (String d : arr) {
                d = d.trim();
                if (!d.isEmpty()) {
                    final String ILLEGAL_CHARS = " /\\*:?<>|\""; //$NON-NLS-1$
                    for (int i=0; i<ILLEGAL_CHARS.length(); ++i) {
                        if (d.indexOf(ILLEGAL_CHARS.charAt(i))>= 0) {
                            return errorInvalid;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public boolean isCBoxRequired() {
            return false;
        }
    }


    // The same as XdsComponentExtLst but with checkbox on the left (not checked means empty string, not "not supported")
    protected class XdsComponentDirsLst extends XdsComponentExtLst {
        public void showSupportedState() {
            super.showSupportedState();
        }

        public XdsComponentDirsLst(String tabId, Sdk.Property propId, String label, String warningEmpty, String warningInvalid) 
        {
            super(tabId, propId, label, warningEmpty, warningInvalid);
        }

        @Override
        public boolean isCBoxRequired() {
            return true;
        }
    }

    protected class XdsComponentExeExt extends XdsComponentWithoutBrowse {
        
        public XdsComponentExeExt(String tabId, Sdk.Property propId, String label, String warningEmpty, String warningInvalid) 
        {
            super(tabId, propId, label, warningEmpty, warningInvalid);    
        }
        
        public String isValid(String value) {
            value = value.trim();
            if (value.startsWith(".")) value = value.substring(1); // ".exe" -> "exe"   //$NON-NLS-1$
            if (value.isEmpty()) {
                return warningEmpty;
            }
            final String ILLEGAL_CHARS = ". /\\*:?<>|\""; //$NON-NLS-1$
            for (int i=0; i<ILLEGAL_CHARS.length(); ++i) {
                if (value.indexOf(ILLEGAL_CHARS.charAt(i))>= 0) {
                    return errorInvalid;
                }
            }
            return null;
        }

        @Override
        public boolean isCBoxRequired() {
            return false;
        }
    }
    
    protected class XdsComponentSubfolderName extends XdsComponentWithoutBrowse {
        
        public XdsComponentSubfolderName(String tabId, Sdk.Property propId, String label, String warningEmpty, String warningInvalid) 
        {
            super(tabId, propId, label, warningEmpty, warningInvalid);    
        }
        
        public String isValid(String value) {
            value = value.trim();
            if (value.isEmpty()) {
                return warningEmpty;
            }
            final String ILLEGAL_CHARS = ". /\\*:?<>|\""; //$NON-NLS-1$
            for (int i=0; i<ILLEGAL_CHARS.length(); ++i) {
                if (value.indexOf(ILLEGAL_CHARS.charAt(i))>= 0) {
                    return errorInvalid;
                }
            }
            return null;
        }

        @Override
        public boolean isCBoxRequired() {
            return false;
        }
    }
    
    protected class XdsSeparator extends XdsComponentWithoutBrowse {
        public XdsSeparator(String tabId, boolean drawLine, String separatorText) {
            this(tabId, drawLine, true, separatorText);
        }

        public XdsSeparator( String tabId
                           , boolean drawLine
                           , boolean insertFreeSpace
                           , String separatorText) 
        {
            super(tabId, null, null, null, null);
            this.drawLine   = drawLine;
            this.insertFreeSpace = insertFreeSpace;
            this.separatorText = separatorText;
        }
        public boolean isItem() {
            return false;
        }
        
        public boolean drawLine;
        public boolean insertFreeSpace;
        public String  separatorText;
        public Composite separatorComposite;
    }

    
    protected class XdsToolsBlock extends XdsComponentWithoutBrowse {
        private SdkToolsControl ctrl;
        public XdsToolsBlock(String tabId) 
        {
            super(tabId, null, null, null, null);
        }
        public boolean isItem() {
            return false;
        }
        public void setCtrl(SdkToolsControl ctrl) {
            this.ctrl = ctrl;
        }
        @Override
        public void performFinish() {
            if (ctrl != null) {
                ctrl.applyChangesToSdk();
            }
        }
    }

    
    protected class XdsEnvironmentBlock extends XdsComponentWithoutBrowse {
        public XdsEnvironmentBlock(String tabId) 
        {
            super(tabId, null, null, null, null);
        }
        public boolean isItem() {
            return false;
        }
    }

    
    private class ValidateModifyListener implements ModifyListener {
        private XdsComponent xc;
        
        public ValidateModifyListener(XdsComponent xc) {
            this.xc = xc;
        }
        
            @Override
            public void modifyText(ModifyEvent e) {
                if (xc != null && xc.isSupported()) {
                    xc.strPath = xc.textPath.getText();
                }
                editSdkDialog.validate(false);
            }
    }

    private class LabelMouseListener implements MouseListener {
        private Button cbox;
        
        public LabelMouseListener(Button cbox) {
            this.cbox = cbox;
        }
        
        @Override
        public void mouseDown(MouseEvent e) {
            if (cbox.isEnabled()) {
                cbox.setSelection(!cbox.getSelection());
                reenableAll(getTabName(), false);
            }
        }

        @Override
        public void mouseDoubleClick(MouseEvent e) {
        }

        @Override
        public void mouseUp(MouseEvent e) {
        }
    }
        
}
