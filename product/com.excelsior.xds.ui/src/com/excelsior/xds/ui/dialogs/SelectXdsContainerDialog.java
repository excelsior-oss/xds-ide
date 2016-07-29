package com.excelsior.xds.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.excelsior.xds.core.natures.NatureIdRegistry;
import com.excelsior.xds.core.project.NatureUtils;
import com.excelsior.xds.core.resource.ResourceUtils;

public class SelectXdsContainerDialog extends ElementTreeSelectionDialog {

    public SelectXdsContainerDialog(Shell parentShell, String message) {
        super(parentShell, new WorkbenchLabelProvider(), new ContentProvider());
        setTitle(message);
        setInput(ResourceUtils.getWorkspaceRoot());
        setAllowMultiple(false);
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                getOkButton().setEnabled(!getTreeViewer().getSelection().isEmpty());
            }
        });
        
        super.createButtonsForButtonBar(parent);
        getOkButton().setEnabled(false);
    }

    public IContainer getProject() {
        return (IContainer) getResult()[0];
    }

    private static class ContentProvider extends BaseWorkbenchContentProvider {
        @Override
        public Object[] getChildren(Object element) {
            Object[] children = super.getChildren(element);
            List<Object> filteredElements = new ArrayList<Object>();
            for (Object o : children) {
                if (o instanceof IProject) {
                    IProject p = (IProject)o;
                    if (NatureUtils.hasNature(p, NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID)) {
                        filteredElements.add(p);
                    }
                }
                else{
                    filteredElements.add(o);
                }
            }
            return filteredElements.toArray();
        }
    }
}
