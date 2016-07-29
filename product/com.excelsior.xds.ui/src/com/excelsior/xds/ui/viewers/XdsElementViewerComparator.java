package com.excelsior.xds.ui.viewers;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.excelsior.xds.core.model.ISourceBound;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsImportSection;
import com.excelsior.xds.core.text.ITextRegion;

public abstract class XdsElementViewerComparator extends ViewerComparator {
    
    private ILabelProvider labelProvider;

    public XdsElementViewerComparator(ILabelProvider labelProvider) {
        this.labelProvider = labelProvider;
    }
    

    public abstract boolean isSortAlphabetically();

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerSorter#compare(null, null, null)
     */
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        try {
        	// special treatment for the Import Section
        	boolean isFirstElementImportSection  =  e1 instanceof IXdsImportSection;
        	boolean isSecondElementImportSection =  e2 instanceof IXdsImportSection;
        	if (isFirstElementImportSection) {
        		return isSecondElementImportSection? 0 : -1;
        	}
        	if (isSecondElementImportSection) {
        		return isFirstElementImportSection? 0 : 1;
        	}
        	
            if (isSortAlphabetically()) {
            	IXdsElement xe1 = (IXdsElement)e1;
            	IXdsElement xe2 = (IXdsElement)e2;
                String s1 = labelProvider.getText(xe1);
                String s2 = labelProvider.getText(xe2);
                return s1.toUpperCase().compareTo(s2.toUpperCase());
            } else {
            	if (e1 instanceof ISourceBound && e2 instanceof ISourceBound) {
            		ISourceBound sb1 = (ISourceBound)e1;
            		ISourceBound sb2 = (ISourceBound)e2;
            		ITextRegion textRegion1 = sb1.getSourceBinding().getDeclarationTextRegion();
					int pos1 = textRegion1 != null ? textRegion1.getOffset() : 0;
            		ITextRegion textRegion2 = sb2.getSourceBinding().getDeclarationTextRegion();
					int pos2 = textRegion2 != null ? textRegion2.getOffset() : 0;
            		return pos1 - pos2;
            	}
            }
            
        } catch (Exception e) {
        }
        return 0;
    }
}
