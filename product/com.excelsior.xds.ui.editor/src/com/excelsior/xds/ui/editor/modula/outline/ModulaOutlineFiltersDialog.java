package com.excelsior.xds.ui.editor.modula.outline;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.modula.outline.ModulaOutlineFilter.OutlineElementFilter;

public class ModulaOutlineFiltersDialog extends SelectionDialog {
    
    /**
     * 
     * @param shell
     * @param filters array of filters to show
     * @return 
     *     true - Ok, filters checkStates are set to user selection
     *     false - cancel, filters checkStates are not changed
     */
    public static boolean playDialog(Shell shell, OutlineElementFilter filters[]) {
        // save for 'Cancel':
        for (OutlineElementFilter f : filters) {
            f.setSavedCheckState(f.getCheckState());
        }

        ModulaOutlineFiltersDialog dlg = new ModulaOutlineFiltersDialog(shell, filters);
        if (dlg.open() == Window.OK) {
            return true;
        } else {
            // 'Cancel' => restore:
            for (OutlineElementFilter f : filters) {
                f.setCheckState(f.getSavedCheckState());
            }
            return false;
        }
    }
    

    private OutlineElementFilter filters[];
    

    protected ModulaOutlineFiltersDialog(Shell parentShell, OutlineElementFilter filters[]) {
        super(parentShell);
        this.filters = filters;
        setTitle(Messages.XdsOutlineFiltersDialog_ViewFilters);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        initializeDialogUnits(parent);
        // create a composite with standard margins and spacing
        Composite composite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setFont(parent.getFont());

        Label info= new Label(composite, SWT.LEFT);
        info.setText(Messages.XdsOutlineFiltersDialog_SelectToExclude);

        final CheckboxTableViewer fCheckBoxList= CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
        GridData data= new GridData(GridData.FILL_BOTH);
        data.heightHint= fCheckBoxList.getTable().getItemHeight() * 10;
        fCheckBoxList.getTable().setLayoutData(data);

        fCheckBoxList.setLabelProvider(new LabelProvider() {
            @Override
            public Image getImage(Object element) {
                return null;
            }
            @Override
            public String getText(Object element) {
                if (element instanceof OutlineElementFilter)
                    return ((OutlineElementFilter)element).getName();
                else
                    return null;
            }
        });
        fCheckBoxList.setContentProvider(new ArrayContentProvider());
        fCheckBoxList.setInput(filters);

        // Description
        info= new Label(composite, SWT.LEFT);
        info.setText(Messages.XdsOutlineFiltersDialog_FilterDescription);
        final Text description= new Text(composite, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint= convertHeightInCharsToPixels(3);
        description.setLayoutData(data);

        fCheckBoxList.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection= event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object selectedElement= ((IStructuredSelection)selection).getFirstElement();
                    if (selectedElement instanceof OutlineElementFilter)
                        description.setText(((OutlineElementFilter)selectedElement).getDescription());
                }
            }
        });
        
        fCheckBoxList.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                Object element= event.getElement();
                if (element instanceof OutlineElementFilter) {
                    ((OutlineElementFilter)element).setCheckState(event.getChecked());
                }
            }});
        
        Composite buttonComposite= new Composite(composite, SWT.RIGHT);
        layout= new GridLayout();
        layout.numColumns= 2;
        buttonComposite.setLayout(layout);
        data= new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
        data.grabExcessHorizontalSpace= true;
        composite.setData(data);

        // Select All button
        Button selectButton = SWTFactory.createPushButton(buttonComposite, Messages.XdsOutlineFiltersDialog_SelectAll, null);
        selectButton.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fCheckBoxList.setAllChecked(true);
                for (OutlineElementFilter f : filters) {
                    f.setCheckState(true);
                }
            }
        });

        // De-select All button
        Button deselectButton = SWTFactory.createPushButton(buttonComposite, Messages.XdsOutlineFiltersDialog_DeselectAll, null);
        deselectButton.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fCheckBoxList.setAllChecked(false);
                for (OutlineElementFilter f : filters) {
                    f.setCheckState(false);
                }
            }
        });
        
        // Set initial checkboxes state:
        for (OutlineElementFilter f : filters) {
            fCheckBoxList.setChecked(f, f.getCheckState());
        }

        applyDialogFont(parent);
        return parent;
    }

}
