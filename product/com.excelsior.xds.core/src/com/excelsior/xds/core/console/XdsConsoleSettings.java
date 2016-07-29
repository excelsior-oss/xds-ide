package com.excelsior.xds.core.console;

import org.eclipse.jface.preference.IPreferenceStore;
import com.excelsior.xds.core.XdsCorePlugin;

public class XdsConsoleSettings {

    public static void setShowOnBuild(boolean val) {
        setKey("ShowOnBuild", true, val); //$NON-NLS-1$
    }
    
    public static boolean getShowOnBuild() {
        return getKey("ShowOnBuild", true); //$NON-NLS-1$
    }

    public static void setClearBeforeBuild(boolean val) {
        setKey("ClearBeforeBuild", true, val); //$NON-NLS-1$
    }
    
    public static boolean getClearBeforeBuild() {
        return getKey("ClearBeforeBuild", true); //$NON-NLS-1$
    }

    
    private static void setKey(String name, boolean defVal, boolean val) {
        String key = KEYPREFIX + name;
        IPreferenceStore store = XdsCorePlugin.getDefault().getPreferenceStore();
        store.putValue(key, val ? STR_TRUE : STR_FALSE);
    }

    private static boolean getKey(String name, boolean defVal) {
        String key = KEYPREFIX + name;
        IPreferenceStore store = XdsCorePlugin.getDefault().getPreferenceStore();
        boolean res;
        if (!store.contains(key)) {
            store.putValue(key, defVal ? STR_TRUE : STR_FALSE);
            res = defVal;
        } else {
            res = STR_TRUE.equals(store.getString(key));
        }
        return res;
    }

    private static final String KEYPREFIX = XdsCorePlugin.PLUGIN_ID + ".ConsoleSetting."; //$NON-NLS-1$
    private static final String STR_TRUE  = ".T."; //$NON-NLS-1$
    private static final String STR_FALSE = ".F."; //$NON-NLS-1$
}
