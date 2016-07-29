package com.excelsior.xds.ui.editor.internal.preferences.formatter;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TreeItem;

import com.excelsior.xds.parser.modula.XdsSourceType;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.internal.preferences.ModifyDialog.IModifyDialogTabPage;
import com.excelsior.xds.ui.editor.internal.preferences.formatter.FormatterProfile.NewlineSetting;
import com.excelsior.xds.ui.editor.internal.preferences.formatter.FormatterProfile.NewlineSettingCategory;

public class TabPageNewLines implements IModifyDialogTabPage {
    private FormatterProfile fp;
    private FormatterPreview fPreview;
    private TreeViewer fTreeViewer;
    private Group fPropsGroup;
    private ArrayList<Control> optsControls = new ArrayList<Control>();
    private static String treeInitialSelectionText;


    public TabPageNewLines(FormatterProfile fp) {
        this.fp = fp;
    }

    @Override
    public Composite createContents(Composite parent) {
        Font font = parent.getFont();
        
        final SashForm sashForm= new SashForm(parent, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        
        Composite compL = new Composite(sashForm, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        compL.setLayout(layout);
        compL.setFont(font);
        GridData gd = new GridData(GridData.FILL_BOTH);
        compL.setLayoutData(gd);

        Composite compR = new Composite(sashForm, SWT.NONE);
        layout = new GridLayout(1, true);
        compR.setLayout(layout);
        compR.setFont(font);
        gd = new GridData(GridData.FILL_BOTH);
        compR.setLayoutData(gd);

        SWTFactory.createLabel(compL, Messages.TabPageNewLines_InsertNewLine + ':', 1);

        final SashForm sashFormL= new SashForm(compL, SWT.VERTICAL);
        sashFormL.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        fTreeViewer= new TreeViewer(sashFormL, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
        gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
        gd.heightHint= SWTFactory.getCharHeight(parent) * 3;
        fTreeViewer.getControl().setLayoutData(gd);

        fTreeViewer.setContentProvider(new TreeContentProvider());
        fTreeViewer.setLabelProvider(new LabelProvider());
        fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                handleTreeSelection();
            }
        });
        fTreeViewer.setInput(fp);
        
        fPropsGroup = SWTFactory.createGroup(sashFormL, "", 2, 1, GridData.FILL_BOTH); //$NON-NLS-1$
        fPropsGroup.setFont(font);
        gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
        gd.heightHint= SWTFactory.getCharHeight(parent) * 3;
        fPropsGroup.setLayoutData(gd);
        
        sashFormL.setWeights(new int[]{2,1});
        
        configurePreview(compR, 1);
        
        try {
            TreeItem its[] = fTreeViewer.getTree().getItems();
            for (TreeItem it : its) {
                if (it.getData().toString().equals(treeInitialSelectionText)) {
                    fTreeViewer.getTree().setSelection(it);
                    handleTreeSelection();
                    break;
                }
            }
            
        } catch (Exception e) {}

        return sashForm;
    }
    
    
    private void configurePreview(Composite composite, int numColumns) {
        SWTFactory.createLabel(composite, Messages.IndentationTabPage_Preview+':', numColumns);
        fPreview = new FormatterPreview(composite, "newlines_preview.mod", XdsSourceType.Modula); //$NON-NLS-1$

        final GridData gd = new GridData(GridData.FILL_VERTICAL | GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = numColumns;
        gd.widthHint = 0;
        gd.heightHint = 0;
        fPreview.getTextWidget().setLayoutData(gd);
        fPreview.setProfile(fp);
    }

    
    private void handleTreeSelection() {
        for (int i = optsControls.size() - 1; i >= 0; --i) {
            optsControls.get(i).dispose();
        }
        optsControls.clear();

        TreeItem sel[] = fTreeViewer.getTree().getSelection();
        if (sel.length > 0) {
            Object selObj = (sel[0].getData());
            String title = null;
            
            if (selObj instanceof NewlineSetting) {
//                optsControls.add(SWTFactory.createLabel(fPropsGroup, "Preferred number of lines to insert", 2));
//                optsControls.add(SWTFactory.createLabel(fPropsGroup, "", 2));
                NewlineSetting ss = (NewlineSetting)selObj;
                int v = fp.getInsNewLinesBefore(ss);
                mkChkbox(ss, v, true);
                v = fp.getInsNewLinesAfter(ss);
                mkChkbox(ss, v, false);
                title = ((NewlineSetting)selObj).toSettingsString();
            } else if (selObj instanceof NewlineSettingCategory) {
                title = ((NewlineSettingCategory)selObj).toSettingsString();
            }
            fPropsGroup.setText(title == null ? "" : ' ' + title + ' '); //$NON-NLS-1$
        }
        
        fPropsGroup.layout(); // to redraw it
    }

    private void mkChkbox(final NewlineSetting ss, int initValue, final boolean isBefore) {
    // initValue: 0 - don't insert, remove if any
    //            1,2.. lines to insert, 
    //            -1 - do nothing
    //            -2 - don't show control
    // Now we use only -1, 1 or -2 (but formatter may support all)         
        if (initValue == -1 || initValue == 1) {
            final Button cb = SWTFactory.createCheckbox(fPropsGroup,
                    isBefore ? Messages.TabPageNewLines_InsNewlineBefore
                             : Messages.TabPageNewLines_InsNewlineAfter, 2);
            optsControls.add(cb);
            cb.setSelection(initValue == 1);
            cb.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    int val = cb.getSelection() ? 1 : -1;
                    if (isBefore) {
                        fp.setInsNewLineBefore(ss, val);
                    } else {
                        fp.setInsNewLineAfter(ss, val);
                    }
                    fPreview.setProfile(fp);
                }
            });
        }
    }
  

//    private void mkCbox(final NewlineSetting ss, int initValue, final boolean isBefore) {
//        String text = isBefore ? "Before" + ':' : "After" + ':';
//        optsControls.add(SWTFactory.createLabel(fPropsGroup, text, 1));
//                
//        final Combo combo = SWTFactory.createCombo(fPropsGroup, 1, SWT.DROP_DOWN | SWT.READ_ONLY);
//        optsControls.add(combo);
//        combo.add("No matter"); // -1
//        combo.add("No new line"); // 0
//        combo.add("One new line symbol"); // 1
//        combo.add("One blank line"); // 2
//        combo.add("Two blank lines"); // 3
//        combo.add("Three blank lines"); // 4
//        combo.add("Four blank lines"); // 5
//        combo.select(1 + Math.min(Math.max(initValue, -1), 5));
//        combo.addSelectionListener(new SelectionAdapter() {
//            @Override public void widgetSelected(SelectionEvent e) {
//                int val = combo.getSelectionIndex() - 1;
//                if (isBefore) {
//                    fp.setInsNewLineBefore(ss, val);
//                } else {
//                    fp.setInsNewLineAfter(ss, val);
//                }
//                fPreview.setProfile(fp);
//            }
//        });
//    }
        
    
    @Override
    public void makeVisible() {
        fPreview.setProfile(fp);
    }

    @Override
    public void setInitialFocus() {
        //cmbTabPolicy.setFocus();
    }


    private static class TreeContentProvider implements ITreeContentProvider {
        
        public Object[] getElements(Object inputElement) {
            return getChildren(NewlineSettingCategory.NlscRoot);
        }
        
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof NewlineSettingCategory) {
                return ((NewlineSettingCategory)parentElement).getChildren().toArray();
            }
            return new Object[0];
        }
        
        public Object getParent(Object element) {
            if (element instanceof NewlineSettingCategory) {
                return ((NewlineSettingCategory)element).getParent();
            } else if (element instanceof NewlineSetting) {
                return ((NewlineSetting)element).getCategory();
            }
            return null;
        }
        
        public boolean hasChildren(Object element) {
            return (element instanceof NewlineSettingCategory);
        }
        
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
        
        public void dispose() {}
    }

}
