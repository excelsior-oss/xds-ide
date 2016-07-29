package com.excelsior.xds.core.console;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;

import com.excelsior.xds.core.XdsCorePlugin;

public enum ColorStreamType {
    NORMAL          ("Normal",        new RGB(0x00, 0x00, 0x00)), //$NON-NLS-1$
    ERROR           ("Error",         new RGB(0xff, 0x00, 0x00)), //$NON-NLS-1$
    SYSTEM          ("System",        new RGB(0x80, 0x80, 0x80)), //$NON-NLS-1$
    USER_INPUT      ("UserInput",     new RGB(0x00, 0xa0, 0x40)), //$NON-NLS-1$
    XDS_LOG_ERROR   ("XdsLogError",   new RGB(0xd0, 0x00, 0x00)), //$NON-NLS-1$
    XDS_LOG_WARNING ("XdsLogWarning", new RGB(0x00, 0x00, 0xff)), // 0xfb, 0x73, 0x00 //$NON-NLS-1$
    BACKGROUND      ("Background",    new RGB(0xff, 0xff, 0xff)); //$NON-NLS-1$

    public RGB getRgb() {
        initColors();
        return rgb;
    }
    
    public RGB getDefaultRgb() {
        return defaultRgb;
    }
    
    public RGB getRgb(boolean getDefault) {
        return getDefault ? getDefaultRgb() : getRgb();
    }
    
    public void setRgb(RGB rgb) {
        initColors();
        this.rgb = rgb;
        IPreferenceStore store = XdsCorePlugin.getDefault().getPreferenceStore(); 
        PreferenceConverter.setValue(store, colorId, rgb);
    }


    // --- Private -----------------------------------------------------
    private final String colorId;
    private final RGB    defaultRgb;
    private RGB    rgb;
    private static boolean firstCall = true;
    
    private ColorStreamType (String name, RGB rgb) {
        // colorId is used in "org.eclipse.ui.preferenceTransfer" extension point:
        this.colorId    = XdsCorePlugin.PLUGIN_ID + ".XdsConsoleColor." + name + ".COLOR"; //$NON-NLS-1$ //$NON-NLS-2$    
        this.rgb        = rgb;
        this.defaultRgb = rgb;
        final IPreferenceStore store = XdsCorePlugin.getDefault().getPreferenceStore();
        store.addPropertyChangeListener(new IPropertyChangeListener() {
            // required to import settings via "org.eclipse.ui.preferenceTransfer" extension point
            @Override
            public void propertyChange(PropertyChangeEvent event) {
              if (colorId.equals(event.getProperty())) {
                  updateFromStore(store);
              }
            }
        });          
    }
    
    private void updateFromStore(IPreferenceStore store) {
        // see Note in initStore() below 
        if (store.contains(colorId)) {
            rgb = PreferenceConverter.getColor(store, colorId);
        } else {
            rgb = defaultRgb;
        }
    }
    
    private void initStore(IPreferenceStore store) {
        // Note: stupid PreferenceConverter may not create keys in store for items with 'default' values.
        // So we can't expect that store.contains(id) for RGB(0,0,0) will be TRUE and so on. 
        if (!store.contains(colorId)) {
            PreferenceConverter.setValue(store, colorId, rgb);
        }
    }
    
    /**
     * Init highlight colors in store if they are absent in it
     * 
     * @param store
     */
    private static void initColorsInStore(IPreferenceStore store) {
        for (ColorStreamType cs : ColorStreamType.values()) {
            cs.initStore(store);
        }
    }
    
    /**
     * Get colors from store,
     * 
     * @param store
     */
    private static void updateColorsFromStore(IPreferenceStore store) {
        for (ColorStreamType cs : ColorStreamType.values()) {
            cs.updateFromStore(store);
        }
    }

    
    private static void initColors() {
        if (firstCall) {
            IPreferenceStore store = XdsCorePlugin.getDefault().getPreferenceStore(); 
            ColorStreamType.initColorsInStore(store);
            ColorStreamType.updateColorsFromStore(store);
            firstCall = false;
        }
    }
    
};

