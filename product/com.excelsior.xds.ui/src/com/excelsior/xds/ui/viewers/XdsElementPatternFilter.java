package com.excelsior.xds.ui.viewers;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

import com.excelsior.xds.core.model.IXdsSyntheticElement;

public class XdsElementPatternFilter extends PatternFilter
{
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isLeafMatch(Viewer viewer, Object element) {
        return !(element instanceof IXdsSyntheticElement)
            && super.isLeafMatch(viewer, element);  
    }

}
