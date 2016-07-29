package com.excelsior.xds.core.updates.resources;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.osgi.framework.Version;

public class InstalledUpdatesRegistry {
	private Map<String, InstalledUpdate> installedFile2Descriptor = new HashMap<String, InstalledUpdate>();

	InstalledUpdatesRegistry(Map<String, InstalledUpdate> installedFile2Descriptor) {
		this.installedFile2Descriptor = installedFile2Descriptor;
	}
	
	public boolean isNewerThanInstalled(String filePath, Version updateVersion) {
		InstalledUpdate installedUpdate = installedFile2Descriptor.get(filePath);
		if (installedUpdate == null) return true;
		return installedUpdate.getFileVersion().compareTo(updateVersion) < 0;
	}
	
	void add(String fileLocation, Version updateVersion) {
		add(new InstalledUpdate(fileLocation, updateVersion));
	}
	
	private void add(InstalledUpdate installedUpdate) {
		installedFile2Descriptor.put(installedUpdate.getFileLocation(), installedUpdate);
	}

	@SuppressWarnings("unchecked")
	public Map<String, InstalledUpdate> getInstalledFile2Descriptor() {
		return UnmodifiableMap.decorate(installedFile2Descriptor);
	}
}
