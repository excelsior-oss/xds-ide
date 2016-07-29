package com.excelsior.xds.core.updates.descriptor;

import java.util.List;

import org.osgi.framework.Version;

public class InstanceDirectoryUpdate {
	public final String name;
	public final List<FileUpdate> fileUpdates;
	
	public InstanceDirectoryUpdate(String name, List<FileUpdate> fileUpdates) {
		this.name = name;
		this.fileUpdates = fileUpdates;
	}

	public static class FileUpdate{
		public String name;
		public final String source;
		public final String description;
		public final Version version;
		
		FileUpdate(String name, String source, String description, Version version) {
			this.name = name;
			this.source = source;
			this.description = description;
			this.version = version;
		}
	}
}
