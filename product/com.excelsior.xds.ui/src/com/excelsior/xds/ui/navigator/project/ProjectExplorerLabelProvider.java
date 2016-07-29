package com.excelsior.xds.ui.navigator.project;

import java.util.Objects;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.navigator.IDescriptionProvider;

import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.core.utils.AdapterUtilities;
import com.excelsior.xds.ui.viewers.XdsElementImages;

public class ProjectExplorerLabelProvider implements ILabelProvider, IDescriptionProvider {
    private static final WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider();

	@Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public Image getImage(Object o) {
        IXdsElement element = getXdsElement(o);
        Image image = XdsElementImages.getProjectElementImage(element);
        if (image == null){
        	IResource r = AdapterUtilities.getAdapter(o, IResource.class);
        	if (r != null){
        		image = workbenchLabelProvider.getImage(r);
        	}        	
        }
        return image;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof IXdsElement) {
            return ((IXdsElement)element).getElementName();
        }
        return null;
    }
    
    @Override
	public String getDescription(Object o) {
    	IResource r = AdapterUtilities.getAdapter(o, IResource.class);
    	if (r != null){
    		return Objects.toString(r.getLocation());
    	}
		return null;
	}

	private IXdsElement getXdsElement(Object o) {
        if (o instanceof IXdsElement) {
            return (IXdsElement)o;
        }
        if (o instanceof IResource) {
            return XdsModelManager.getModel().getXdsElement((IResource)o);
        }
        return null;
    }
}