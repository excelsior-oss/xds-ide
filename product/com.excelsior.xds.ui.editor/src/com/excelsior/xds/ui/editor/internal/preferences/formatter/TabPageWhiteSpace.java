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
import com.excelsior.xds.ui.editor.internal.preferences.formatter.FormatterProfile.WhiteSpaceCategory;
import com.excelsior.xds.ui.editor.internal.preferences.formatter.FormatterProfile.WhiteSpaceSetting;

public class TabPageWhiteSpace implements IModifyDialogTabPage {
    private FormatterProfile fp;
    private FormatterPreview fPreview;
    private TreeViewer fTreeViewer;
    private Group fPropsGroup;
    private ArrayList<Control> optsControls = new ArrayList<Control>();
    private static String treeInitialSelectionText;


    public TabPageWhiteSpace(FormatterProfile fp) {
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

        SWTFactory.createLabel(compL, Messages.TabPageWhiteSpace_InsertSpace + ':', 1);

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
        
        fPropsGroup = SWTFactory.createGroup(sashFormL, "", 1, 1, GridData.FILL_BOTH); //$NON-NLS-1$
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
        fPreview = new FormatterPreview(composite, "indent_preview.mod", XdsSourceType.Modula); //$NON-NLS-1$

        final GridData gd = new GridData(GridData.FILL_VERTICAL | GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = numColumns;
        gd.widthHint = 0;
        gd.heightHint = 0;
        fPreview.getTextWidget().setLayoutData(gd);
        fPreview.setProfile(fp);
    }

    
    private void handleTreeSelection() {
        for (int i = optsControls.size()-1; i>=0; --i) { 
            optsControls.get(i).dispose();
        }
        optsControls.clear();
        
        TreeItem sel[] = fTreeViewer.getTree().getSelection();
        if (sel.length > 0) {
            Object selObj = (sel[0].getData());
            ArrayList<WhiteSpaceSetting> alWss = new ArrayList<FormatterProfile.WhiteSpaceSetting>();
            String caption = ""; //$NON-NLS-1$
            if (selObj instanceof WhiteSpaceSetting) {
                alWss.add((WhiteSpaceSetting)selObj);
                caption = ((WhiteSpaceSetting)selObj).toSettingsString();
            } else if (selObj instanceof WhiteSpaceCategory) {
                caption = ((WhiteSpaceCategory)selObj).toSettingsString();
                if (WhiteSpaceCategory.BinaryOperations.equals(selObj) || WhiteSpaceCategory.UnaryOperations.equals(selObj)) {
                    for (WhiteSpaceSetting wss : WhiteSpaceSetting.values()) {
                       if (wss.getCategory().equals(selObj)) {
                           alWss.add(wss);
                       }
                    }
                }
            }
                    
            fPropsGroup.setText(caption.isEmpty() ? "" : (" " + caption + " ") ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            
            if (alWss.size() > 0) {
                mkCbox(alWss, true);
                mkCbox(alWss, false);
            }
        }
        fPropsGroup.layout(); // to redraw it
    }
    
    private void mkCbox(final ArrayList<WhiteSpaceSetting> alWss, final boolean isBefore) {
        final Button cb = SWTFactory.createCheckbox(fPropsGroup, isBefore ? Messages.TabPageWhiteSpace_InsSpaceBefore : Messages.TabPageWhiteSpace_InsSpaceAfter, 1);
        optsControls.add(cb);
        cb.setSelection(isBefore ? fp.isInsSpaceBefore(alWss.get(0)) : fp.isInsSpaceAfter(alWss.get(0)));
        cb.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent e) {
                boolean ins = cb.getSelection();
                for (WhiteSpaceSetting wss : alWss) {
                    if (isBefore) {
                        fp.setInsSpaceBefore(wss, ins);
                    } else {
                        fp.setInsSpaceAfter(wss, ins);
                    }
                }
                fPreview.setProfile(fp);
            }
        });
    }
    
    
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
            return getChildren(WhiteSpaceCategory.RootCategory);
        }
        
        public Object[] getChildren(Object parentElement) {
            ArrayList<Object> al = null;
            if (parentElement instanceof WhiteSpaceCategory) {
                WhiteSpaceCategory wsc = (WhiteSpaceCategory)parentElement;
                if (!wsc.equals(WhiteSpaceCategory.BinaryOperations) && !wsc.equals(WhiteSpaceCategory.UnaryOperations)) {
                    al = wsc.getChildren();
                    // don't manage "." :
                    int idx = al.indexOf(WhiteSpaceSetting.WSpaceDot);
                    if (idx >= 0) {
                        al.remove(idx);
                    }
                    // don't manage ";" :
                    idx = al.indexOf(WhiteSpaceSetting.WSpaceSemicolon);
                    if (idx >= 0) {
                        al.remove(idx);
                    }
                }
            }
            return al==null ? new Object[0] : al.toArray();
        }
        
        public Object getParent(Object element) {
            if (element instanceof WhiteSpaceCategory) {
                return ((WhiteSpaceCategory)element).getParent();
            } else if (element instanceof WhiteSpaceSetting) {
                return ((WhiteSpaceSetting)element).getCategory();
            }
            return null;
        }
        
        public boolean hasChildren(Object element) {
            if (element instanceof WhiteSpaceCategory) {
                WhiteSpaceCategory wsc = (WhiteSpaceCategory)element;
                if (!wsc.equals(WhiteSpaceCategory.BinaryOperations) && !wsc.equals(WhiteSpaceCategory.UnaryOperations)) {
                    return true;
                }
            }
            return false;
        }
        
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
        
        public void dispose() {}
    }

}
