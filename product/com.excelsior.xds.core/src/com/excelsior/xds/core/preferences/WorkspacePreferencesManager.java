package com.excelsior.xds.core.preferences;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

import com.excelsior.xds.core.XdsCorePlugin;
import com.excelsior.xds.core.log.LogHelper;

public final class WorkspacePreferencesManager 
{
    private static final String XDS_QUALIFIER = XdsCorePlugin.PLUGIN_ID;
    
    private static final String TAG_SERIALIZED_COMPILATION_SET = "SerializedCompilationSet"; //$NON-NLS-1$
    private static final String TAG_SERIALIZED_LIBRARY_FILE_SET = "SerializedLibraryFileSet"; //$NON-NLS-1$
//    private static final String TAG_DEFAULT_VALUES_INITIALIZED = "DEFAULT_VALUES_INITIALIZED"; //$NON-NLS-1$
    
    private IScopeContext workspaceScope = InstanceScope.INSTANCE;

    /**
     * Thread-safe singleton support.
     */
    public static WorkspacePreferencesManager getInstance(){
        return WorkspacePreferencesManagerHolder.INSTANCE;
    }
    
//    public void initializeDefaultValues() {
//        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_DEFAULT_VALUES_INITIALIZED);
//        if (!preferenceKey.getStoredBoolean(workspaceScope, null)) {
//            preferenceKey.setStoredBoolean(workspaceScope, true, null);
//            flush();
//            
//            turnOnRefreshOnAccess();
//            turnOffAutobuilding();
//        }
//    }

    protected void turnOnRefreshOnAccess() {
        PreferenceKey preferenceKey;
        preferenceKey = new PreferenceKey(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH);
        preferenceKey.setStoredBoolean(workspaceScope, true, null);
        
        try {
            workspaceScope.getNode(ResourcesPlugin.PI_RESOURCES).flush();
        } catch (BackingStoreException e1) {
            LogHelper.logError(e1);
        }
    }

//    protected void turnOffAutobuilding() {
//        try {
//        	BuilderUtils.setAutoBuilding(false);
//        } catch (CoreException e) {
//            LogHelper.logError(e);
//        }
//    }
    
    public String getSerializedCompilationSet(){
        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_SERIALIZED_COMPILATION_SET);
        return preferenceKey.getStoredValue(workspaceScope, null);
    }
    
    public void setSerializedCompilationSet(String serialized){
        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_SERIALIZED_COMPILATION_SET);
        preferenceKey.setStoredValue(workspaceScope, serialized, null);
    }
    
    public String getSerializedLibraryFileSet(){
        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_SERIALIZED_LIBRARY_FILE_SET);
        return preferenceKey.getStoredValue(workspaceScope, null);
    }
    
    public void setSerializedLibraryFileSet(String serialized){
        PreferenceKey preferenceKey = new PreferenceKey(XDS_QUALIFIER, TAG_SERIALIZED_LIBRARY_FILE_SET);
        preferenceKey.setStoredValue(workspaceScope, serialized, null);
    }
    
    
    /**
     * Flushes all keys related to 'com.excelsior.xds.core' plugin.
     */
    public void flush(){
    	PreferenceCommons.flush(workspaceScope, XDS_QUALIFIER);
    }

    private static class WorkspacePreferencesManagerHolder{
        static WorkspacePreferencesManager INSTANCE = new WorkspacePreferencesManager();
    }
    
}