package com.excelsior.xds.ui.preferences.sdk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkTool;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.commons.utils.SwtUtils;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.internal.nls.Messages;

public class SdkToolsControl extends Composite {
    private TreeViewer treeViewer;
    private TreeColumn columnName;
    private TreeColumn columnPath;
    private Button btnAdd;
    private Button btnEdit;
    private Button btnRemove;
    private Button btnUp;
    private Button btnDn;
    private Sdk editedSdk;

    private ArrayList<ModelItem> treeModel;

    /**
     * Create the composite.
     * @param parent
     * @param style
     * @param editedSdk 
     */
    public SdkToolsControl(Composite parent, int style, final Sdk editedSdk) {
        super(parent, style);
        this.editedSdk = editedSdk;
        sdkToTreeModel(editedSdk);

        setLayout(SwtUtils.removeMargins(new GridLayout(2, false)));
        setLayout(SwtUtils.removeMargins(new GridLayout(2, false)));

        // Tree
        treeViewer= new TreeViewer(this, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        treeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        treeViewer.setLabelProvider(new ToolCellLabelProvider());
        treeViewer.setContentProvider(new ToolListContentProvider());
        treeViewer.setInput(editedSdk);
        treeViewer.getTree().addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleTreeSelection();
            }        
        });
        treeViewer.setAutoExpandLevel(2);
        Tree tree = treeViewer.getTree();
        columnName = new TreeColumn(tree, SWT.LEFT);
        columnName.setWidth(SwtUtils.getTextWidth(tree, "WWWWWWWWWWWW")); //$NON-NLS-1$
        columnName.setText(Messages.SdkToolsControl_ToolName);
        columnPath = new TreeColumn(tree, SWT.LEFT);
        columnPath.setWidth(SwtUtils.getTextWidth(tree, "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW")); //$NON-NLS-1$
        columnPath.setText(Messages.SdkToolsControl_Location);
        tree.setHeaderVisible(true);
        //tree.setLinesVisible(true);

        // Buttons column
        Composite buttonsComposite = new Composite(this, SWT.NONE);
        buttonsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        buttonsComposite.setLayout(SwtUtils.removeMargins(new GridLayout(1, false)));

        btnAdd = SWTFactory.createPushButton(buttonsComposite, Messages.Common_Add, null);
        btnAdd.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleAdd();
            }
        });

        btnEdit = SWTFactory.createPushButton(buttonsComposite, Messages.Common_Edit, null);
        btnEdit.setEnabled(false);
        btnEdit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleEdit();
            }
        });

        btnRemove = SWTFactory.createPushButton(buttonsComposite, Messages.Common_Remove, null);
        btnRemove.setEnabled(false);
        btnRemove.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleRemove();
            }
        });

        SWTFactory.createVerticalSpacer(buttonsComposite, 0.5);

        btnUp = SWTFactory.createPushButton(buttonsComposite, Messages.SdkToolsControl_Up, null);
        btnUp.setEnabled(false);
        btnUp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleMove(true);
            }
        });

        btnDn = SWTFactory.createPushButton(buttonsComposite, Messages.SdkToolsControl_Down, null);
        btnDn.setEnabled(false);
        btnDn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleMove(false);
            }
        });

        treeViewer.setInput(editedSdk);
        selectModelItem(0);
    }

    private ArrayList<ArrayList<ModelItem>> makeSplitModel(int curIdx, int chunk_offs[]) {
        ArrayList<ArrayList<ModelItem>> splitModel = new ArrayList<ArrayList<ModelItem>>();
        // Split model by top-level items
        for (int i=0; i<treeModel.size(); ++i) {
            ModelItem it = treeModel.get(i);
            if (it.isRoot() || StringUtils.isEmpty(it.tool.getMenuGroup())) {
                splitModel.add(new ArrayList<ModelItem>());
            }
            splitModel.get(splitModel.size()-1).add(it);
            if (i == curIdx && chunk_offs!=null) {
                chunk_offs[0] = splitModel.size()-1;
                chunk_offs[1]  = splitModel.get(chunk_offs[0]).size()-1;
            }
        }
        return splitModel;
    }

    private void handleMove(boolean up) {
        int curIdx = getSelectedIdx();
        if (curIdx < 0) {
            return; // int err?
        }
        ModelItem selectedItem = treeModel.get(curIdx);

        int chunk_offs[] = {0,0}; 
        ArrayList<ArrayList<ModelItem>> splitModel = makeSplitModel(curIdx, chunk_offs);
        int chunk = chunk_offs[0];
        int offs  = chunk_offs[1];

        int step = 0;
        if (offs == 0) {
            // Move whone top-level item:
            ArrayList<ModelItem> cut = null;
            if (chunk > 0 && up) {
                cut = splitModel.get(chunk);
                step = -1;
            } else if (chunk < splitModel.size() - 1 && !up) {
                cut = splitModel.get(chunk);
                step = 1;
            }

            if (cut != null) {
                splitModel.remove(chunk);
                chunk += step;
                splitModel.add(chunk, cut);
            }
        } else {
            // It is item inside chunk - move it here:
            ArrayList<ModelItem> al = splitModel.get(chunk);
            ModelItem cut = null;
            if (offs > 0 && up) {
                cut = al.get(offs);
                step = -1;
            } else if (offs < al.size() - 1 && !up) {
                cut = al.get(offs);
                step = 1;
            }

            if (cut != null) {
                al.remove(offs);
                offs += step;
                al.add(offs, cut);
            }
        }

        if (step != 0) {
            // It was real movement. Put results back to model and show it: 
            treeModel.clear();
            int sel = 0;
            for (ArrayList<ModelItem> al : splitModel) {
                for (ModelItem it : al) {
                    if (it.equals(selectedItem)) {
                        sel = treeModel.size();
                    }
                    treeModel.add(it);
                }
            }
            updateTree(sel);
        }
    }

    private void handleTreeSelection() {
        int curIdx = getSelectedIdx();
        boolean isSep = false;
        boolean isRoot = false;
        int[] curRZ = null;
        if (curIdx >= 0) {
            ModelItem it = treeModel.get(curIdx);
            isRoot = it.isRoot();
            isSep = !isRoot && it.tool.isSeparator();
            curRZ = getRootZone(treeModel.get(curIdx));
        }

        boolean enRem = false;
        boolean enUp  = (curRZ == null ? curIdx > 0 : curIdx != curRZ[0]+1 );
        boolean enDn  = false;
        boolean enEd  = false;
        if (curIdx >= 0) {
            enEd  = !isSep;
            enRem = !isRoot || (curRZ != null && curRZ[0]==curRZ[1]); // no root or empty root 
            if (isRoot) {
                enDn = curRZ[1] < treeModel.size() - 1;
            } else if (curRZ == null) {
                enDn = curIdx < treeModel.size() - 1;
            } else {
                enDn = curIdx < curRZ[1];
            }
        }
        btnRemove.setEnabled(enRem);
        btnUp.setEnabled(enUp);
        btnDn.setEnabled(enDn);
        btnEdit.setEnabled(enEd);
    }

    private int getSelectedIdx() {
        TreeItem[] sel = treeViewer.getTree().getSelection();
        if (sel.length > 0) {
            return treeModel.indexOf(sel[0].getData());
        }
        return -1;
    }

    private TreeItem searchTreeItemForModelItem(TreeItem[] arr, ModelItem it) {
        if (arr != null) {
            for (TreeItem ti : arr) {
                if (ti.getData().equals(it)) {
                    return ti;
                }
                TreeItem tichild = searchTreeItemForModelItem(ti.getItems(), it);
                if (tichild != null) {
                    return tichild;
                }
            }
        }
        return null;
    }

    private void selectModelItem(int idx) {
        try {
            treeViewer.getTree().deselectAll();
            TreeItem ti = searchTreeItemForModelItem(treeViewer.getTree().getItems(), treeModel.get(idx));
            if (ti != null) {
                treeViewer.getTree().select(ti);
                handleTreeSelection();
            }
        } catch (Exception e) {
        }
    }



    private void handleEdit(){
        int idx = getSelectedIdx();
        if (idx >= 0) {
            ModelItem it = treeModel.get(idx);
            if (it.isRoot()) {
                String oldGroup = it.rootName;
                HashSet<String> usedNames = new HashSet<String>();
                for (ModelItem x : treeModel) {
                    if (x.isRoot() && x != it) {
                        usedNames.add(x.rootName);
                    }
                }
                String s = editGroupName(it.rootName, usedNames);
                if (s != null && !s.equals(oldGroup)) {
                    it.rootName = s;
                    for (ModelItem x : treeModel) {
                        if (!x.isRoot() && oldGroup.equals(x.tool.getMenuGroup())) {
                            x.tool.setMenuGroup(s);
                        }
                    }
                    updateTree(idx);
                }
            } else {
                SdkTool tool = it.tool;
                String oldGroup = tool.getMenuGroup();
                SdkTool clonedTool = tool.clone();
                EditSdkToolDialog editSdkToolDialog = new EditSdkToolDialog(clonedTool, editedSdk, collectGroups());
                WizardDialog dialog = new WizardDialog(getShell(), editSdkToolDialog);
                if (dialog.open() == WizardDialog.OK) {
                    tool.copyFrom(clonedTool);
                    String newGroup = tool.getMenuGroup();

                    if (!(oldGroup == null ? "" : oldGroup).equals(newGroup)) { //$NON-NLS-1$

                        int newPos = 0;
                        for (int i=0; i<treeModel.size(); ++i) {
                            if (i != idx) {
                                ModelItem x = treeModel.get(i);
                                if (newGroup.equals(x.isRoot() ? x.rootName : x.tool.getMenuGroup())) {
                                    newPos = i+1; // position after last item of the selected group
                                }
                            }
                        }

                        if (newPos < idx) {
                            treeModel.remove(idx);
                            treeModel.add(newPos, it);
                            idx = newPos;
                        } else if (idx < newPos) {
                            treeModel.add(newPos, it);
                            treeModel.remove(idx);
                            idx = newPos-1;
                        }
                    }
                    updateTree(idx);
                }
            }
        }
    }

    private void handleAdd() {
        int curIdx = getSelectedIdx();
        int[] curRZ = null;
        String curGroup = ""; //$NON-NLS-1$
        if (curIdx >= 0) {
            curRZ = getRootZone(treeModel.get(curIdx));
            if (curRZ != null) {
                curGroup = treeModel.get(curRZ[0]).rootName;
            }
        }
        int newIdx = -1;

        SdkTool newTool = new SdkTool(editedSdk);
        newTool.setMenuGroup(curGroup);
        SdkToolsControlAddDialog addDlg = new SdkToolsControlAddDialog(newTool, editedSdk, collectGroups());
        WizardDialog  addWDlg = new WizardDialog(getShell(), addDlg);
        if (addWDlg.open() == WizardDialog.OK) {
            switch (addDlg.getResult()) {
            case TOOL:
                newIdx = curIdx + 1;
                treeModel.add(newIdx, new ModelItem(newTool));
                break;
            case SEPARATOR: {
                SdkTool sep = new SdkTool(); // new SdkTool() makes separator, not a tool
                sep.setMenuGroup(curGroup);
                newIdx = curIdx + 1;
                treeModel.add(newIdx, new ModelItem(sep)); 
                break;
            }
            case GROUP:
            {
                String name = addDlg.getGroupName();
                if (!StringUtils.isEmpty(name)) {
                    newIdx = curRZ == null ? curIdx + 1 : curRZ[1] + 1;
                    treeModel.add(newIdx, new ModelItem(name));
                    treeViewer.refresh();
                }
            }
			default:
				break;
            } // SWITCH

            updateTree(newIdx);
        }
    }


    private void handleRemove() {
        int curIdx = getSelectedIdx();
        int newIdx = -1;
        if (curIdx >= 0) {
            int[] curRZ = getRootZone(treeModel.get(curIdx));
            if (curRZ == null || curIdx > curRZ[0] || curRZ[0] == curRZ[1]) { 
                treeModel.remove(curIdx);
                newIdx = curIdx-1;
                if (curRZ != null && curRZ[0]+1==curRZ[1]) {
                    treeModel.remove(curRZ[0]); // group becomes empty - remove it
                    --newIdx;
                }
                if (newIdx < 0 && !treeModel.isEmpty()) {
                    newIdx = 0;
                }
            }
        }
        updateTree(newIdx);
    }


    private String editGroupName(String initialName, final Set<String> usedNames) {
        InputDialog dlg = new InputDialog(getShell(), Messages.SdkToolsControl_NewGroupName, 
                Messages.SdkToolsControl_EnterGroupName+':', initialName, 
                new IInputValidator() 
        {
            @Override
            public String isValid(String newText) {
                newText = newText.trim();
                if (newText.isEmpty()) {
                    return Messages.SdkToolsControl_NameIsEmpty;
                } else if (usedNames.contains(newText)) {
                    return Messages.SdkToolsControl_NameIsUsed;
                }
                return null;
            }
        });
        if (dlg.open() == Window.OK) {
            return dlg.getValue().trim();
        }
        return null;
    }

    //      public static void suppressBadSeparators(Sdk sdk) {
    //          while (true) {
    //              List<SdkTool> lst = sdk.getTools();
    //              int sz = lst.size(); 
    //              if (sz == 0) {
    //                  return;
    //              }
    //              if (lst.get(0).isSeparator()) {
    //                  sdk.removeTool(lst.get(0));
    //                  continue;
    //              }
    //            if (lst.get(sz-1).isSeparator()) {
    //                sdk.removeTool(lst.get(sz-1));
    //                continue;
    //            }
    //            for (int i=0; i<sz-1; ++i) {
    //                if (lst.get(i).isSeparator() && lst.get(i+1).isSeparator()) {
    //                    sdk.removeTool(lst.get(i));
    //                    continue;
    //                }
    //            }
    //            return;
    //          }
    //      }
    //      

    private List<String> collectGroups() {
        List<String> lst = new ArrayList<String>();
        Set<String> dups = new HashSet<String>();
        for (ModelItem it : treeModel) {
            String grp = it.isRoot() ? it.rootName : it.tool.getMenuGroup();
            if (!StringUtils.isEmpty(grp) && !dups.contains(grp)) {
                lst.add(grp);
                dups.add(grp);
            }
        }
        return lst;
    }

    private void updateTree(int selIdx) {
        treeViewer.setInput(editedSdk);
        treeViewer.expandAll();
        selectModelItem(selIdx);
    }
    // ----------------- ------------- Tree model processing -----------------------------------

    private void sdkToTreeModel(Sdk sdk) {
        treeModel = new ArrayList<ModelItem>();
        List<SdkTool> tools = sdk.getTools();
        String group = ""; //$NON-NLS-1$
        for (SdkTool t : tools) {
            String tgrp = t.getMenuGroup();
            if (StringUtils.isBlank(tgrp) || tgrp.equals(group)) {
                treeModel.add(new ModelItem(t));
            } else {
                treeModel.add(new ModelItem(tgrp));
                treeModel.add(new ModelItem(t));
            }
            group = tgrp;
        }

    }

    /**
     * For model like {* * R tr tr tr * * *} and root R (with 'tr' elements) or for one of 'tr' 
     * returns [2, 5] (root index, last child index)
     * 
     * Or null when no zone found
     */
    private int[] getRootZone(ModelItem it) {
        int beg = -1;
        String grp = ""; //$NON-NLS-1$
        if (it.isRoot()) {
            grp = it.rootName;
            beg = treeModel.indexOf(it);
        } else {
            grp = it.tool.getMenuGroup();
            beg = getRootIdxFor(it);
        }
        if (beg >= 0) {
            int end = beg;
            for (; end+1 < treeModel.size(); ++end) {
                ModelItem it2 = treeModel.get(end+1);
                if (it2.isRoot() || !grp.equals(it2.tool.getMenuGroup())) {
                    break;
                }
            }
            return new int[]{beg, end};
        }
        return null;
    }


    /**
     * If 'it' is tool and is child - returns its root index in the treeModer. In other case returns -1
     */
    private int getRootIdxFor(ModelItem it) {
        if (!it.isRoot() && !StringUtils.isBlank(it.tool.getMenuGroup())) {
            for (int idx = treeModel.indexOf(it); idx >= 0; --idx) {
                if (treeModel.get(idx).isRoot()) {
                    return idx;
                }
            }
        }
        return -1;
    }

    /**
     * Apply changes (if any) in internal tools model to SDK
     * NOTE: some tools in SDK may be changed at this moment: Edit tools, changing groups etc may 
     * affect to SDK tools immediately, but tools reordering, separators etc should be copyed to SDK here.  
     */
    public void applyChangesToSdk() {
        ArrayList<ModelItem> modelCp = new ArrayList<SdkToolsControl.ModelItem>();
        modelCp.addAll(treeModel);

        // Suppress extra separators:
        rescan: while (true) {
            int i;
            for (i=0; i<modelCp.size(); ++i) {
                ModelItem it = modelCp.get(i);
                if (!it.isRoot() && it.tool.isSeparator()) {
                    if (i == 0 || i == modelCp.size()-1) { // 1st or last in menu
                        break;
                    }
                    String grp = it.tool.getMenuGroup();
                    ModelItem itL = modelCp.get(i-1);
                    ModelItem itR = modelCp.get(i+1);
                    if (itR.tool != null && itR.tool.isSeparator()) { // duplicated separator
                        break;
                    }
                    if (itL.isRoot()) { // 1st inside root 
                        break;
                    }
                    if (!grp.isEmpty() && (itR.isRoot() || !grp.equals(itR.tool.getMenuGroup()))) { // last inside root
                        break;
                    }
                }
            }
            if (i < modelCp.size()) {
                modelCp.remove(i);
            } else {
                break rescan;
            }
        }

        // Re-create Sdk's tool list:
        editedSdk.removeAllTools();
        for (ModelItem it : modelCp) {
            if (!it.isRoot()) {
                editedSdk.addTool(it.tool);
            }
        }
    }


    private static class ToolCellLabelProvider extends CellLabelProvider {
        @Override
        public void update(ViewerCell cell) {
            String txt = ""; //$NON-NLS-1$
            Image img = null;
            ModelItem it = (ModelItem)cell.getElement();
            if (it.isRoot()) {
                if (cell.getColumnIndex() == 0) {
                    txt = it.rootName; 
                    img = ImageUtils.getImage(ImageUtils.PACKAGE_FRAGMENT_IMAGE_NAME);
                }
            } else {
                if (it.tool.isSeparator()) {
                    if (cell.getColumnIndex() == 0) {
                        txt = Messages.SdkToolsControl_SeparatorLine; 
                    }

                } else {
                    if (cell.getColumnIndex() == 0) {
                        txt = it.tool.getToolName();
                        if (!it.tool.isValid()) {
                            img = ImageUtils.getImage(ImageUtils.ERROR_16x16);
                        }
                    } else {
                        txt = it.tool.getLocation();
                    }
                }
            }
            cell.setText(txt);
            cell.setImage(img);
        }
    }

    private class ToolListContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            ArrayList<ModelItem> topLevel = new ArrayList<ModelItem>();
            for (ModelItem it : treeModel) {
                if (it.isRoot() || StringUtils.isEmpty(it.tool.getMenuGroup())) {
                    topLevel.add(it);
                }
            }
            return topLevel.toArray();
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            int[] rz = getRootZone((ModelItem)parentElement);
            if (rz != null) {
                return treeModel.subList(rz[0]+1, rz[1]+1).toArray();
            }
            return null;
        }

        @Override
        public Object getParent(Object element) {
            int ridx = getRootIdxFor((ModelItem)element);
            if (ridx >= 0) {
                return treeModel.get(ridx);
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            return ((ModelItem)element).isRoot();
        }
    }

    private class ModelItem {
        private final SdkTool tool;
        private String rootName;


        public ModelItem(SdkTool t) {
            this.tool = t;
            this.rootName = null;
        }

        public ModelItem(String rootName) {
            tool = null;
            this.rootName = rootName;
        }

        public boolean isRoot() {
            return tool == null;
        }

        @Override
        public String toString() {
            return isRoot() ? rootName : String.valueOf(tool);
        }
    }

}

