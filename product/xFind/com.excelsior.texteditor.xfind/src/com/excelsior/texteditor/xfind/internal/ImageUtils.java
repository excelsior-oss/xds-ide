package com.excelsior.texteditor.xfind.internal;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

import com.excelsior.texteditor.xfind.XFindPlugin;

public abstract class ImageUtils 
{
    public static final String FIND_NEXT    = "find_next.gif"; //$NON-NLS-1$
    public static final String FIND_PREV    = "find_prev.gif"; //$NON-NLS-1$
    public static final String FIND_HISTORY = "find_history.gif"; //$NON-NLS-1$
    public static final String FIND_STATUS  = "find_status.gif"; //$NON-NLS-1$

    private static String ICONS_PATH = "$nl$/icons/"; //$NON-NLS-1$

    private static ImageRegistry imageRegistry;
    private static Object monitor = new Object();

    public static Image getImage(String name) {
        return getImageRegistry().get(name);
    }

    private static void declareImages() {
        String[] imagesFileNames = new String[] {
                FIND_NEXT,
                FIND_PREV,
                FIND_HISTORY,
                FIND_STATUS,
        };
        for (String s : imagesFileNames) {
            declareRegistryImage(s, ICONS_PATH + s);
        }
    }

    public static ImageRegistry getImageRegistry() {
        synchronized (monitor) {
            if (imageRegistry == null) {
                initializeImageRegistry();
            }
            return imageRegistry;
        }
    }

    public synchronized static ImageRegistry initializeImageRegistry() {
        if (imageRegistry == null) {
            imageRegistry = new ImageRegistry(Display.getDefault());
            declareImages();
        }
        return imageRegistry;
    }

    private final static void declareRegistryImage(String key, String path) {
        ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
        Bundle bundle = Platform.getBundle(XFindPlugin.PLUGIN_ID);
        URL url = null;
        if (bundle != null){
            url = FileLocator.find(bundle, new Path(path), null);
            if(url != null) {
                desc = ImageDescriptor.createFromURL(url);
            }
        }
        imageRegistry.put(key, desc);
    }
}
