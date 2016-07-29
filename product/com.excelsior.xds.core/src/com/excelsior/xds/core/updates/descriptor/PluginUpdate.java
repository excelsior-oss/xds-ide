package com.excelsior.xds.core.updates.descriptor;

public class PluginUpdate {
	public final String pluginLocation;
	public final String description;
	
	PluginUpdate(String pluginLocation, String description) {
		this.pluginLocation = pluginLocation;
		this.description = description;
	}
}
