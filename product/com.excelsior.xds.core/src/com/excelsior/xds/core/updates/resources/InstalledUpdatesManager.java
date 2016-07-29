package com.excelsior.xds.core.updates.resources;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.osgi.framework.Version;

/**
 * Loads/stories internal description of all installed resource updates for the XDS IDE.
 * NOT thread-safe
 */
public class InstalledUpdatesManager {
	
	private static final String TAG_VERSION = "version"; //$NON-NLS-1$
	private static final String TAG_FILE_LOCATION = "fileLocation"; //$NON-NLS-1$
	private static final String TAG_XDS_FOLDERNAME  = "com.excelsior.xds"; //$NON-NLS-1$
	private static final String TAG_INSTALLED_UPDATES_FILENAME    = "installedUpdates.xml"; //$NON-NLS-1$
	private static final String TAG_INSTALLED_UPDATES = "InstalledUpdates"; //$NON-NLS-1$
	private static final String TAG_INSTALLED_UPDATE = "InstalledUpdate"; //$NON-NLS-1$
	
	private static File registryFile;
	private static InstalledUpdatesRegistry installedUpdatesRegistry;
	
	private static class InstalledUpdatesManagerHolder {
		static InstalledUpdatesManager INSTANCE = new InstalledUpdatesManager();
	}
	
	public static InstalledUpdatesManager getInstance() {
		return InstalledUpdatesManagerHolder.INSTANCE;
	}
	
	public InstalledUpdatesRegistry loadInstalledUpdatesRegistry() throws IOException, WorkbenchException {
		if (installedUpdatesRegistry == null) {
			File installedUpdatesRegistryFile = getInstalledUpdatesRegistryFile();
			if (installedUpdatesRegistryFile.exists()) {
				Reader reader = new FileReader(installedUpdatesRegistryFile);
				XMLMemento memento = XMLMemento.createReadRoot(reader);
				installedUpdatesRegistry = loadInstalledUpdatesRegistry(memento);
			}
			else {
				installedUpdatesRegistry = new InstalledUpdatesRegistry(new HashMap<String, InstalledUpdate>());
			}
		}
		return installedUpdatesRegistry;
	}
	
	public void saveInstalledUpdatesRegistry(InstalledUpdatesRegistry installedUpdatesRegistry) throws IOException {
		XMLMemento memento = XMLMemento.createWriteRoot(TAG_INSTALLED_UPDATES);
		saveInstalledUpdatesRegistry(memento, installedUpdatesRegistry);
		
		FileWriter writer = new FileWriter(getInstalledUpdatesRegistryFile());
	    memento.save(writer);
	}
	
	private void saveInstalledUpdatesRegistry(XMLMemento memento, InstalledUpdatesRegistry installedUpdatesRegistry) {
		Set<Entry<String, InstalledUpdate>> installedUpdateEntries = installedUpdatesRegistry.getInstalledFile2Descriptor().entrySet();
		for (Entry<String, InstalledUpdate> installedUpdateEntry : installedUpdateEntries) {
			final InstalledUpdate installedUpdate = installedUpdateEntry.getValue();
			if (!getActualFile(installedUpdate).exists()) continue;
			IMemento installedUpdateChild = memento.createChild(TAG_INSTALLED_UPDATE);
			installedUpdateChild.putString(TAG_FILE_LOCATION, installedUpdate.getFileLocation());
			installedUpdateChild.putString(TAG_VERSION, installedUpdate.getFileVersion().toString());
		}
	}

	private static InstalledUpdatesRegistry loadInstalledUpdatesRegistry(XMLMemento memento) {
		Map<String, InstalledUpdate> installedFile2Descriptor = new HashMap<String, InstalledUpdate>();
		IMemento[] installedUpdateChildren = memento.getChildren(TAG_INSTALLED_UPDATE);
		for (IMemento installedUpdateChild : installedUpdateChildren) {
			InstalledUpdate installedUpdate = createInstalledUpdateFor(installedUpdateChild);
			File file = getActualFile(installedUpdate);
			if (file.exists()) {
				installedFile2Descriptor.put(installedUpdate.getFileLocation(), installedUpdate);
			}
		}
		return new InstalledUpdatesRegistry(installedFile2Descriptor);
	}
	
	private static File getActualFile(InstalledUpdate installedUpdate) {
		return new File(installedUpdate.getFileLocation());
	}

	private static InstalledUpdate createInstalledUpdateFor(IMemento installedUpdateChild) {
		String fileLocation = installedUpdateChild.getString(TAG_FILE_LOCATION);
		String version = installedUpdateChild.getString(TAG_VERSION);
		Version fileVersion = new Version(version);
		return new InstalledUpdate(fileLocation, fileVersion);
	}

	private static File getInstalledUpdatesRegistryFile() throws IOException {
		if (registryFile == null) {
			File dir = null;
			Location location = Platform.getConfigurationLocation();
			if (location != null) {
				URL configURL = location.getURL();
				dir = new File(configURL.getFile());
			}
			
			registryFile = new File(new File(dir, TAG_XDS_FOLDERNAME), TAG_INSTALLED_UPDATES_FILENAME);
			if (!registryFile.exists()) {
				registryFile.getParentFile().mkdirs();
			}
		}
		
		return registryFile;
	}
}
