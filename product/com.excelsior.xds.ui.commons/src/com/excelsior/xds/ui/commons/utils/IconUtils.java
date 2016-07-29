package com.excelsior.xds.ui.commons.utils;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;


public class IconUtils {

    private static ResourceManager resourceManager;
    
    /**
     * To use in LabelProvider.getImage() to get icon for resource.  
     */
    public static Image getImage(Object o) {
        if (resourceManager == null) {
            resourceManager = new LocalResourceManager(JFaceResources.getResources());
        }

        IWorkbenchAdapter adapter = (IWorkbenchAdapter)getAdapter(o, IWorkbenchAdapter.class);
        if (adapter != null) {
            ImageDescriptor descriptor = adapter.getImageDescriptor(o);
            if (descriptor != null) {
                //add any annotations to the image descriptor
                return (Image) resourceManager.get(descriptor);
            }
        }
        return null;
    }

    private static Object getAdapter(Object sourceObject, Class<IWorkbenchAdapter> adapterType) {
        if (sourceObject == null) {
            return null;
        }
        if (adapterType.isInstance(sourceObject)) {
            return sourceObject;
        }

        if (sourceObject instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) sourceObject;

            Object result = adaptable.getAdapter(adapterType);
            if (result != null) {
                return result;
            }
        } 
        
        if (!(sourceObject instanceof PlatformObject)) {
            Object result = Platform.getAdapterManager().getAdapter(sourceObject, adapterType);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

}
