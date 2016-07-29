package com.excelsior.xds.core.updates.descriptor;

import java.io.File;
import java.util.List;

public class UpdateDirDescriptor {
	public final File updateDescFile;
	public final String updateDirPath;
	
	public final List<Update> updates;
	public final List<UpdateSite> updateSites;
	
	public final List<PluginUpdate> pluginUpdates;
	
	public final List<InstanceDirectoryUpdate> instanceDirectoryUpdates;
	
	UpdateDirDescriptor(File updateDescFile, List<Update> updates,
			List<UpdateSite> updateSites, List<PluginUpdate> pluginUpdates, List<InstanceDirectoryUpdate> instanceDirectoryUpdates) {
		this.updateDescFile = updateDescFile;
		
		this.updates = updates;
		this.updateSites = updateSites;
		this.updateDirPath = updateDescFile.getParent();
		this.pluginUpdates = pluginUpdates;
		this.instanceDirectoryUpdates = instanceDirectoryUpdates;
	}
}
