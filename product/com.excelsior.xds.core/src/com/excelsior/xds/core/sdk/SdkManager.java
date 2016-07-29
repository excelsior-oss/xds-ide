package com.excelsior.xds.core.sdk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.osgi.framework.Bundle;

import com.excelsior.xds.core.XdsCorePlugin;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.preferences.WorkspacePreferencesManager;
import com.excelsior.xds.core.resource.ResourceUtils;

/**
 * Loads/stories internal description of all registered XDS development systems.
 * NOT thread-safe
 */
public final class SdkManager {
	private static final String KEY_ACTIVE_SDK_NAME = XdsCorePlugin.PLUGIN_ID + ".SdkManager.ActiveSdk";     //$NON-NLS-1$
    private static final String KEY_ALL_SDKS_DATA   = XdsCorePlugin.PLUGIN_ID + ".SdkManager.SdksSettings";  //$NON-NLS-1$ // used in "org.eclipse.ui.preferenceTransfer" extension point
    private static final String RELATIVE_XDS_RESOURCES_BUNDLE_PATH = "../com.excelsior.xds.resources"; //$NON-NLS-1$
   	private static final String RELATIVE_XN_EXE_PATH = "resources\\comp_null\\xn.exe"; //$NON-NLS-1$

    private static final String TAG_SDK_COLLECTION  = "RegisteredSDKs";  //$NON-NLS-1$
	private static final String TAG_SDK = "SDK";                         //$NON-NLS-1$
	
	private static final String TAG_ENVIRONMENT_VARIABLE       = "EnvironmentVariable";  //$NON-NLS-1$
	private static final String TAG_ENVIRONMENT_VARIABLE_NAME  = "Name";                 //$NON-NLS-1$
	private static final String TAG_ENVIRONMENT_VARIABLE_VALUE = "Value";                //$NON-NLS-1$
	
    private static final String TAG_TOOL_COLLECTION = "RegisteredTools";  //$NON-NLS-1$
	private static final String TAG_TOOL  = "Tool";                       //$NON-NLS-1$

	private static final String TAG_XDS_FOLDERNAME  = "com.excelsior.xds";  //$NON-NLS-1$
	private static final String TAG_SDK_FILENAME    = "sdk.xml";            //$NON-NLS-1$
	
	private static SdkRegistry sdkRegistry;
	private static File        registryFile = null;
    private static Sdk         sdkSimulator = null;
    
    private final Set<ISdkListener> sdkListeners = new HashSet<>();
    private final Map<String, Sdk> name2DeletedSdk = new HashMap<>();
	
	private boolean is1stTime = true;
	
	private static volatile int offPropListener = 0;
	
	private static class SdkManagerHolder {
		static SdkManager INSTANCE = new SdkManager();
	}
	
	public synchronized void addListener(ISdkListener sdkListener) {
		sdkListeners.add(sdkListener);
	}
	
	public synchronized void removeListener(ISdkListener sdkListener) {
		sdkListeners.remove(sdkListener);
	}
	
	synchronized void notifySdkListenersOnChanged(SdkChangeEvent e){
		if (e.hasChanges()) {
			sdkListeners.stream().forEach(l -> l.sdkChanged(e));
		}
	}
	
	synchronized void notifySdkListenersOnRemoved(SdkRemovedEvent e){
		sdkListeners.stream().forEach(l -> l.sdkRemoved(e));
	}

	public static SdkManager getInstance() {
		return SdkManagerHolder.INSTANCE;
	}
	
	public static Sdk createSdk(String name, String sdkHomePath) {
		return new Sdk(getInstance(), name, sdkHomePath);
	}
	
	public static Sdk createSdk() {
		return new Sdk(getInstance());
	}
	
	public SdkRegistry loadSdkRegistry() {
	    if (is1stTime) {
	        is1stTime = false;
            final IPreferenceStore store = XdsCorePlugin.getDefault().getPreferenceStore();
            store.addPropertyChangeListener(new IPropertyChangeListener() {
                // used when SDK settings are imported via "org.eclipse.ui.preferenceTransfer" extension point
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    if (offPropListener > 0) return;
                    String prop = event.getProperty();
                    if (KEY_ALL_SDKS_DATA.equals(prop)) {
                        importSdks(store.getString(KEY_ALL_SDKS_DATA));
                    }
                }
            });
	    }

	    if (sdkRegistry == null) {
			File sdkRegistryFile = getSdkRegistryFile();
			if (sdkRegistryFile.length() > 0) {
				FileReader reader = null;
				try {
					reader = new FileReader(sdkRegistryFile);
					sdkRegistry = loadSdkRegistry(XMLMemento.createReadRoot(reader));
				} catch (FileNotFoundException e) {
					LogHelper.logError(e);
				} catch (WorkbenchException e) {
					LogHelper.logError(e);
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
							LogHelper.logError(e);
						}
					}
				}
			}
			else{
				sdkRegistry = new SdkRegistry(null);
			}
		} 

		return sdkRegistry;
	}
	
    private void importSdks(String xml) {
        try {
            SdkRegistry sr = null;
            StringReader reader = new StringReader(xml);
            sr = loadSdkRegistry(XMLMemento.createReadRoot(reader));
            
            // Megre new SDKs with old, replace old SDKs with the same names,
            // defauld sdk name (if any) will not be changed
            List<Sdk> newList = new ArrayList<Sdk>();
            HashSet<String> newNames = new HashSet<String>();
            final HashSet<String> changedSdks = new HashSet<String>();

            if (sdkRegistry == null) {
                sdkRegistry = new SdkRegistry(null);
            }

            for (Sdk sdk : sr.getRegisteredSDKs()) {
                newNames.add(sdk.getName());
                newList.add(sdk);
                // This SDK is 'changed' if there is no equal SDK in sdkRegistry:
                Sdk sOld = sdkRegistry.findSdk(sdk.getName());
                if (sOld == null || !sOld.equals(sdk)) {
                    changedSdks.add(sdk.getName());
                }
            }
            for (Sdk sdk : sdkRegistry.getRegisteredSDKs()) {
                if (!newNames.contains(sdk.getName())) {
                    newList.add(sdk);
                }
            }
            sdkRegistry.setRegisteredSDKs(newList);
            saveSdkRegistry(sdkRegistry, false);
        } catch (Exception e) {
            LogHelper.logError(e);
        }
    }

	
	/**
	 * re-reads contents of SdkRegistry even it is already in memory
	 */
	public SdkRegistry reloadSdkRegistry() {
		unloadSdkRegistry();
		return loadSdkRegistry();
	}
	
	public void removeSdk(Sdk sdk) {
		if (sdk != null){
			name2DeletedSdk.put(sdk.getName(), sdk);
			sdkRegistry.removeSdk(sdk);
		}
	}
	
	public void unloadSdkRegistry(){
		if (sdkRegistry != null){
			sdkRegistry.getRegisteredSDKs().stream().filter(Sdk::isBeingEdited).forEach(Sdk::cancelEdit);
			sdkRegistry = null;
		}
		name2DeletedSdk.clear();
	}
	
    public void saveSdkRegistry(SdkRegistry sdkRegistry) {
    	sdkRegistry.getRegisteredSDKs().stream().filter(Sdk::isBeingEdited).forEach(Sdk::endEdit);
        saveSdkRegistry(sdkRegistry, true);
        name2DeletedSdk.values().stream().map(SdkRemovedEvent::new).forEach(this::notifySdkListenersOnRemoved);
        name2DeletedSdk.clear();
    }

    private void saveSdkRegistry(SdkRegistry sdkRegistry, boolean dupToPreferenceStore) {
		if (sdkRegistry == null) return;
		XMLMemento memento = XMLMemento.createWriteRoot(TAG_SDK_COLLECTION);
		saveSdkRegistry(memento, sdkRegistry);
		
		FileWriter fwriter = null;
	    try {
	        StringWriter sw = new StringWriter();
            memento.save(sw);
            String xml = sw.toString();
	        fwriter = new FileWriter(getSdkRegistryFile());
	        fwriter.append(xml);
	        if (dupToPreferenceStore) {
    	        IPreferenceStore store = XdsCorePlugin.getDefault().getPreferenceStore();
    	        try {
                    ++offPropListener;
    	            store.setValue(KEY_ALL_SDKS_DATA, xml);
    	        }
    	        finally {
    	            --offPropListener;
    	        }
	        }
	    }
	    catch (IOException e) {
	       LogHelper.logError(e);
	    }
	    finally {
	       try {
	          if (fwriter != null)
	             fwriter.close();
	       }
	       catch (IOException e) {
	      	 LogHelper.logError(e);
	       }
	    }
	}
	
	private void saveSdkRegistry(XMLMemento memento, SdkRegistry sdkRegistry) {
        String actSdk = (sdkRegistry.getDefaultSdk() != null) ? sdkRegistry.getDefaultSdk().getName() : ""; //$NON-NLS-1$
        IPreferenceStore store = XdsCorePlugin.getDefault().getPreferenceStore();
        store.setValue(KEY_ACTIVE_SDK_NAME, actSdk);
        WorkspacePreferencesManager.getInstance().flush();

		for (Sdk sdk : sdkRegistry.getRegisteredSDKs()) {
			IMemento mementoSdk = memento.createChild(TAG_SDK);
			for (Sdk.Property property: Sdk.Property.values()) {
				mementoSdk.putString(property.xmlKey, sdk.getPropertyValue(property));
				for (Sdk.Tag tag : property.possibleTags) {
				    String val = sdk.getTag(property, tag);
				    if (val != null) {
				        String key = property.xmlKey + "_" + tag.xmlTagName; //$NON-NLS-1$
		                mementoSdk.putString(key, val);
				    }
				}
			}
			 Map<String, String> environmentVariables = sdk.getEnvironmentVariablesRaw();
			if (!environmentVariables.isEmpty()) {
				for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
					IMemento mementoEnvVar = mementoSdk.createChild(TAG_ENVIRONMENT_VARIABLE);
					mementoEnvVar.putString(TAG_ENVIRONMENT_VARIABLE_NAME,  entry.getKey());
					mementoEnvVar.putString(TAG_ENVIRONMENT_VARIABLE_VALUE, entry.getValue());
				}
			}
			saveTools(mementoSdk, sdk);
		}
	}

	private SdkRegistry loadSdkRegistry(XMLMemento memento) {
		List<Sdk> registeredSDKs  = new ArrayList<Sdk>();
		
		IMemento[] children = memento.getChildren(TAG_SDK);
		
		IPreferenceStore store = XdsCorePlugin.getDefault().getPreferenceStore();
		String actSdk = store.getString(KEY_ACTIVE_SDK_NAME);
		if (StringUtils.isEmpty(actSdk)) {
		    actSdk = null;
		}
		SdkRegistry sdkRegistry = new SdkRegistry(actSdk);
		for (IMemento settingsMemento : children) {
			Sdk settings = createSdkFor(settingsMemento);
			if (settings != null) {
				registeredSDKs.add(settings);
			}
		}
		sdkRegistry.setRegisteredSDKs(registeredSDKs);
		
		return sdkRegistry;
	}

	private Sdk createSdkFor(IMemento memento) {
		Sdk sdk = SdkManager.createSdk();
		for (Sdk.Property property: Sdk.Property.values()) {
			final String value = memento.getString(property.xmlKey);
			if (value == null)
				return null;
			sdk.setPropertyValue(property, value);
			for (Sdk.Tag tag : property.possibleTags) {
                String key = property.xmlKey + "_" + tag.xmlTagName; //$NON-NLS-1$
			    String val = memento.getString(key);
			    if (val != null) {
			        sdk.setTag(property, tag, val);
			    }
			}
		}		
		IMemento[] mementoEnvironmentVariables = memento.getChildren(TAG_ENVIRONMENT_VARIABLE);
		if (mementoEnvironmentVariables.length > 0) {
			for (IMemento mementoEnvVar : mementoEnvironmentVariables) {
				final String name  = mementoEnvVar.getString(TAG_ENVIRONMENT_VARIABLE_NAME);
				final String value = mementoEnvVar.getString(TAG_ENVIRONMENT_VARIABLE_VALUE);
				sdk.putEnvironmentVariable(name, value);
			}
			
		}
		loadTools(memento, sdk);
		return sdk;
	}
	
	private static void loadTools(IMemento memento, Sdk sdk) {
		IMemento toolsChild = memento.getChild(TAG_TOOL_COLLECTION);
		if (toolsChild != null) {
			IMemento[] toolsChildren = toolsChild.getChildren(TAG_TOOL);
			for (IMemento toolChild : toolsChildren) {
			    SdkTool tool;
			    if (toolChild.getBoolean("isSeprator") != null) { //$NON-NLS-1$
			        tool = new SdkTool(); // new SdkTool() makes separator, not a tool
                    final String value = toolChild.getString(SdkTool.Property.MENU_GROUP.tag);
                    if (value != null) {
                        tool.setPropertyValue(SdkTool.Property.MENU_GROUP, value);
                    }

			    } else {
    			    tool = new SdkTool(sdk);
    			    for (SdkTool.Property property: SdkTool.Property.values()) {
    		            final String value = toolChild.getString(property.tag);
    		            if (value != null) {
    		                tool.setPropertyValue(property, value);
    		            }
    			    }
			    }
                sdk.addTool(tool);
			}
		}
	}
	
	private static void saveTools(IMemento memento, Sdk sdk) {
		List<SdkTool> tools = sdk.getTools();
		IMemento toolsMemento = memento.createChild(TAG_TOOL_COLLECTION);
		for (SdkTool tool : tools) {
			IMemento toolMemento = toolsMemento.createChild(TAG_TOOL);
			if (tool.isSeparator()) {
                toolMemento.putBoolean("isSeprator", true); //$NON-NLS-1$
                toolMemento.putString(SdkTool.Property.MENU_GROUP.tag, tool.getPropertyValue(SdkTool.Property.MENU_GROUP));
			} else {
                for (SdkTool.Property property: SdkTool.Property.values()) {
                    toolMemento.putString(property.tag, tool.getPropertyValue(property));
                }
			}
		}
	}

	static File getSdkRegistryFile() {
		if (registryFile == null) {
			try {
				// 1st call:
				
				File dir = null;
				Location location = Platform.getConfigurationLocation();
				if (location != null) {
					URL configURL = location.getURL();
					if (configURL != null && configURL.getProtocol().startsWith("file")) { //$NON-NLS-1$
						dir = new File(configURL.getFile());
					}
				}
				
				if (dir == null) {
					// If the configuration directory is read-only,
					// then return an alternate location
					// rather than null or throwing an Exception.
					dir = XdsCorePlugin.getDefault().getStateLocation().toFile();
				}
				
				registryFile = new File(new File(dir, TAG_XDS_FOLDERNAME), TAG_SDK_FILENAME);
	
				if (!registryFile.exists()) {
					registryFile.getParentFile().mkdirs();
					registryFile.createNewFile();
					
				}
			} catch (Exception ex) {
				LogHelper.logError(ex);
			}
		}
		
		return registryFile;
	}
	
    /**
     * Returns the SDK based on internal 'xn' compiler from "com.excelsior.xds.resources" plug-in.
     * The 'xn' compiler has not code generator. This compiler is designed to extract 
     * options, equations and lookups from XDS configuration and project files.  
     * 
     * @return SDK based on internal 'xn' compiler or 
     *         <code>null</code> if "com.excelsior.xds.resources" plug-in is not available.
     */
    public Sdk getSdkSimulator() throws IOException {
        if (sdkSimulator == null) {
            File xnLocation = getXnCompiler();
            sdkSimulator = SdkManager.createSdk("XDS Modula-2 SDK Simulator", xnLocation.getParent());   //$NON-NLS-1$
            sdkSimulator.setCompilerExecutablePath(xnLocation.getAbsolutePath());
        }
        return sdkSimulator;
    }

    /**
     * Returns location of the internal xn.exe compiler.
     * 
     * @return location of xn.exe compiler
     * @throws IOException
     */
    private static File getXnCompiler() throws IOException {
        File xnLocation;

        Bundle bundle = ResourceUtils.getXdsResourcesPluginBundle(); //$NON-NLS-1$
        if (bundle != null) {
            xnLocation = new File(FileLocator.getBundleFile(bundle), RELATIVE_XN_EXE_PATH);
        }
        else {
            xnLocation = new File(RELATIVE_XDS_RESOURCES_BUNDLE_PATH, RELATIVE_XN_EXE_PATH).getAbsoluteFile();
        }
        
        return xnLocation;
    }
	
}