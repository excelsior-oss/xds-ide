package com.excelsior.xds.utils.updatesite;

public class FeatureEntry {
	private final String id;
	private final String version;
	
	public FeatureEntry(String id, String version) {
		this.id = id;
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public String getVersion() {
		return version;
	}
}
