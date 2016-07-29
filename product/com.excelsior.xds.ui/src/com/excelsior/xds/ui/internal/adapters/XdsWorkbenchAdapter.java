package com.excelsior.xds.ui.internal.adapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.excelsior.xds.core.model.IXdsCompilationUnit;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.ui.images.ImageUtils;

public class XdsWorkbenchAdapter implements IWorkbenchAdapter{
    protected static final Object[] NO_CHILDREN= new Object[0];
    
    @Override
    public Object[] getChildren(Object o) {
        IXdsElement xe = getXdsElement(o);
        if (xe instanceof IXdsContainer ) {
            return ((IXdsContainer)xe).getChildren().toArray();
        }
        return NO_CHILDREN;
    }

    @Override
    public ImageDescriptor getImageDescriptor(Object o) {
        IXdsElement xe = getXdsElement(o);
        if (xe instanceof IXdsCompilationUnit ) {
            return ImageDescriptor.createFromImage(ImageUtils.getImage(ImageUtils.IMPLEMENTATION_MODULE_IMAGE_NAME));
        }
        else if (xe instanceof IXdsContainer) {
            return ImageDescriptor.createFromImage(ImageUtils.getImage(ImageUtils.PACKAGE_FRAGMENT_IMAGE_NAME));
        }
        return null;
    }

    @Override
    public String getLabel(Object o) {
        IXdsElement xe = getXdsElement(o);
        return xe.getElementName();
    }

    @Override
    public Object getParent(Object o) {
        IXdsElement xe = getXdsElement(o);
        return xe.getParent();
    }
    
    private IXdsElement getXdsElement(Object o) {
        return (IXdsElement)o;
    }
}
