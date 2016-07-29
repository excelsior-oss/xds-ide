package com.excelsior.xds.ui.preferences.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.commons.utils.SwtUtils;
import com.excelsior.xds.ui.internal.nls.Messages;

public class SdkEnvironmentControl extends Composite {
    private Table table;
    private Button btnNew;
    private Button btnEdit;
    private Button btnDelete;
    private Sdk editedSdk;
    private TableViewer tableViewer;
    private TableColumn tblclmnName;
    private TableViewerColumn columnName;
    private TableColumn tblclmnLocation;
    private TableViewerColumn columnLocation;
    
    private String selectedVar;
    

    /**
     * Create the composite.
     * @param parent
     * @param style
     * @param editedSdk 
     */
    public SdkEnvironmentControl(Composite parent, int style, final Sdk editedSdk) {
        super(parent, style);
        this.editedSdk = editedSdk;
        selectedVar = null;
        
        setLayoutData(new GridData(GridData.FILL_BOTH));
        setLayout(SwtUtils.removeMargins(new GridLayout(2, false)));
        
        Composite tableComposite = new Composite(this, SWT.NONE);
        tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        tableComposite.setLayout(new GridLayout(1, false));
        
        tableComposite.setBackground(new Color(Display.getDefault(), 255,0,0));
        
        
        tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        TableColumnLayout tableColumnLayout = new TableColumnLayout();
        tableComposite.setLayout(tableColumnLayout);
        
        tableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION);
        table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        columnName = new TableViewerColumn(tableViewer, SWT.NONE);
        tblclmnName = columnName.getColumn();
        tableColumnLayout.setColumnData(tblclmnName, new ColumnPixelData(150, true, true));
        tblclmnName.setText(Messages.SdkEnvironmentControl_Variable);
        
        columnLocation = new TableViewerColumn(tableViewer, SWT.NONE);
        tblclmnLocation = columnLocation.getColumn();
        tableColumnLayout.setColumnData(tblclmnLocation, new ColumnPixelData(150, true, true));
        tblclmnLocation.setText(Messages.SdkEnvironmentControl_Value);
        tableViewer.setContentProvider(new SdkEnvContentProvider());
        tableViewer.setLabelProvider(new SdkEnvLabelProvider());
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                TableItem[] selectedItems = table.getSelection();
                boolean isEnabled = selectedItems.length > 0;
                btnEdit.setEnabled(isEnabled);
                btnDelete.setEnabled(isEnabled);
                selectedVar = isEnabled ? (String)selectedItems[0].getData() : null;
            }
        });
        
        Composite buttonsComposite = new Composite(this, SWT.NONE);
        buttonsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        buttonsComposite.setLayout(SwtUtils.removeMargins(new GridLayout(1, false)));
        
        btnNew = SWTFactory.createPushButton(buttonsComposite, Messages.SdkEnvironmentControl_New, null);
        btnNew.addSelectionListener(new AddVarButtonHandler());
        
        btnEdit = SWTFactory.createPushButton(buttonsComposite, Messages.SdkEnvironmentControl_Edit, null);
        btnEdit.addSelectionListener(new EditValueButtonHandler());
        btnEdit.setEnabled(false);
        
        btnDelete = SWTFactory.createPushButton(buttonsComposite, Messages.SdkEnvironmentControl_Delete, null);
        btnDelete.setEnabled(false);
        btnDelete.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (selectedVar != null) {
                    editedSdk.removeEnvironmentVariable(selectedVar);
                    tableViewer.setInput(editedSdk);
                }
            }
        });
        tableViewer.setInput(editedSdk);
    }
    
    private final class EditValueButtonHandler extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (selectedVar != null) {
                EditVarDlg dlg = new EditVarDlg(selectedVar, editedSdk.getEnvironmentVariableRaw(selectedVar));
                WizardDialog  dialog = new WizardDialog(getShell(), dlg);
                if (dialog.open() == WizardDialog.OK) {
                    editedSdk.putEnvironmentVariable(selectedVar, dlg.getVal());
                    tableViewer.setInput(editedSdk);
                }
            }
        }
    }

    private final class AddVarButtonHandler extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            EditVarDlg dlg = new EditVarDlg("", ""); //$NON-NLS-1$ //$NON-NLS-2$
            WizardDialog  dialog = new WizardDialog(getShell(), dlg);
            if (dialog.open() == WizardDialog.OK) {
                editedSdk.putEnvironmentVariable(dlg.getVar(), dlg.getVal());
                tableViewer.setInput(editedSdk);
            }
        }
    }
    
    private class SdkEnvLabelProvider extends LabelProvider implements ITableLabelProvider {
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
        public String getColumnText(Object element, int columnIndex) {
            String varName = (String)element;
            if (columnIndex == 0) {
                return varName;
            }
            else if (columnIndex == 1) {
                String val = editedSdk.getEnvironmentVariableRaw(varName);
                if (val != null) { // null => int. error?..
                    return val;  
                }
            }
            return ""; //$NON-NLS-1$
        }
    }
    
    private static class SdkEnvContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            Sdk sdk = (Sdk)inputElement;
            ArrayList<String> arr = new ArrayList<String>(sdk.getEnvironmentVariablesRaw().keySet());
            Collections.sort(arr, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            });
            return arr.toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    
    
    private static class EditVarDlg extends Wizard {
        private String var, val, title;
        private boolean editMode;

        protected EditVarDlg(String var, String val) {
            editMode = !var.isEmpty(); 
            title =  editMode ? Messages.SdkEnvironmentControl_EditEnvVar : Messages.SdkEnvironmentControl_NewEnvVar;
            setWindowTitle(title);
            this.var = var;
            this.val = val;
        }
        
        public String getVar() {
            return var;
        }
        
        public String getVal() {
            return val;
        }

        @Override
        public void addPages() {
            addPage(new EditVarPage(title));
        }

        @Override
        public boolean performFinish() {
            return true;
        }
        
        private class EditVarPage extends WizardPage {

            protected EditVarPage(String title) {
                super(""); //$NON-NLS-1$
                setTitle(title);
            }

            @Override
            public void createControl(Composite parent) {
                Composite container = new Composite(parent, SWT.NULL);
                setControl(container);
                container.setLayout(new GridLayout(3, false));
                
                SWTFactory.createLabel(container, Messages.SdkEnvironmentControl_VariableLabel+':', 1);
                final Text textVar = SWTFactory.createSingleText(container,  2);
                textVar.setText(var);
                if (editMode) {
                    textVar.setEditable(false);
                } else {
                    textVar.addModifyListener(new ModifyListener() {
                        @Override
                        public void modifyText(ModifyEvent e) {
                            var = textVar.getText().trim();
                            validatePage();
                        }
                    });
                }

                SWTFactory.createLabel(container, Messages.SdkEnvironmentControl_ValueLabel+':', 1);
                final Text textVal = SWTFactory.createSingleText(container,  1);
                textVal.setText(val);
                textVal.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent e) {
                        val = textVal.getText();
                        validatePage();
                    }
                });
                Button btnVariables = SWTFactory.createPushButton(container, Messages.SdkEnvironmentControl_Variables, 1, null);
                btnVariables.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
                        dialog.open();
                        String variable = dialog.getVariableExpression();
                        if (variable != null) {
                            textVal.insert(variable);
                        }
                    }
                });
                
                
                if (editMode) {
                    textVal.setFocus();
                }
                validatePage();
            }
            
            private void validatePage() {
                if (var.isEmpty()) {
                    setMessage(Messages.SdkEnvironmentControl_VarNameIsEmpty, WARNING);
                    setPageComplete(false);
                } else {
                    setMessage(null);
                    setPageComplete(true);
                }
            }
            
        }
        
    }
}
