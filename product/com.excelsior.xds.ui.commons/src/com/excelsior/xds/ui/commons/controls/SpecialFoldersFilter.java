package com.excelsior.xds.ui.commons.controls;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class SpecialFoldersFilter extends ViewerFilter {
    boolean hideFiles;
    
    public SpecialFoldersFilter(boolean hideFiles) {
        super();
        this.hideFiles = hideFiles;
    }
    
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof IFolder) {
            if (((IFolder)element).getName().startsWith(".")) { //$NON-NLS-1$
                return false; // exclude ".externals", ".settings" and other special folders started with '.' 
            }
        } else if (element instanceof IFile) {
            if (hideFiles) {
                return false;
            }
        }
        return true;
    }
}