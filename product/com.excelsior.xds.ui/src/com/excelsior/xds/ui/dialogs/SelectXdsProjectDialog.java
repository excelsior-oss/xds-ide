package com.excelsior.xds.ui.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.resource.ResourceUtils;

public class SelectXdsProjectDialog extends ElementTreeSelectionDialog {

    public SelectXdsProjectDialog(Shell parentShell, String message) {
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

    public IProject getProject() {
        return (IProject) getResult()[0];
    }

    private static class ContentProvider extends BaseWorkbenchContentProvider {
        @Override
        public Object[] getChildren(Object element) {
            Object[] children = super.getChildren(element);
            return ProjectUtils.filterXdsProjects(children).toArray();
        }
    }
}
