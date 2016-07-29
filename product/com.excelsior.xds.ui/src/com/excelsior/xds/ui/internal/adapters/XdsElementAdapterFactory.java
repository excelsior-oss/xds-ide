package com.excelsior.xds.ui.internal.adapters;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IContributorResourceAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.ResourcePropertySource;

import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsResource;
import com.excelsior.xds.core.model.utils.XdsElementUtils;

public class XdsElementAdapterFactory implements IAdapterFactory, IContributorResourceAdapter {
    
    private static Class<?>[] ADAPTER_LIST= new Class[] {
        IPropertySource.class,
        IResource.class,
        IFile.class,
        IProject.class,
        IWorkbenchAdapter.class,
        IContributorResourceAdapter.class
    };
    
    private static XdsWorkbenchAdapter xdsWorkbenchAdapter;

	@Override
    public Object getAdapter(Object element, @SuppressWarnings("rawtypes") Class key) {
		IXdsElement e = (IXdsElement)element;
    	if (IResource.class.isAssignableFrom(key)) {
    		return XdsElementUtils.adaptToResource(e, key);
    	}
    	
        if (IPropertySource.class.equals(key)) {
            return getProperties(XdsElementUtils.adaptToResource(e, IResource.class));
        }
        else if (IWorkbenchAdapter.class.equals(key)) {
            if (xdsWorkbenchAdapter == null) {
                xdsWorkbenchAdapter = new XdsWorkbenchAdapter();
            }
            return xdsWorkbenchAdapter;
        }
        else if (IContributorResourceAdapter.class.equals(key)) {
            return this;
        }
        
        return null;
    }

    private IPropertySource getProperties(IResource resource) {
    	if (resource == null) {
    		return null;
    	}
        if (resource instanceof IFile) {
            return new FilePropertySource((IFile) resource);
        }
        else {
            return new ResourcePropertySource(resource);
        }
    }

    @Override
    public Class<?>[] getAdapterList() {
        return ADAPTER_LIST;
    }

    @Override
    public IResource getAdaptedResource(IAdaptable adaptable) {
    	IXdsResource xdsElement = (IXdsResource) adaptable;
        return xdsElement.getResource();
    }
}
