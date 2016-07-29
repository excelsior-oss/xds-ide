package com.excelsior.xds.core.updates.descriptor;

import org.osgi.framework.Version;

public class Update {
	public final String targetPluginName;
	public final String newResourceLocation;
	public final String existingResourceLocation;
	public final String xmlSchemaLocation;
	public final Version version;
	
	public Update(String targetPluginName, String newResourceLocation,
			String existingResourceLocation, String xmlSchemaLocation, Version version) {
		this.targetPluginName = targetPluginName;
		this.newResourceLocation = newResourceLocation;
		this.existingResourceLocation = existingResourceLocation;
		this.xmlSchemaLocation = xmlSchemaLocation;
		this.version = version;
	}
}