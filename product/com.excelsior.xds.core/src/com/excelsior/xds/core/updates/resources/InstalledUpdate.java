package com.excelsior.xds.core.updates.resources;

import org.osgi.framework.Version;

public class InstalledUpdate {
	private String fileLocation;
	private Version fileVersion;
	
	InstalledUpdate(String fileLocation, Version fileVersion) {
		this.fileLocation = fileLocation;
		this.fileVersion = fileVersion;
	}

	public String getFileLocation() {
		return fileLocation;
	}

	public Version getFileVersion() {
		return fileVersion;
	}
}