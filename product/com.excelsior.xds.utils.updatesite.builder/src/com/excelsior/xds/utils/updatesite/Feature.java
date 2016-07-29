package com.excelsior.xds.utils.updatesite;

public class Feature {
	private final String id;
	private final String version;
	private final String label;
	private final String image;
	private final String providerName;
	private final String copyright;
	private final String licenseURL;
	private final String license;
	private final String description;
	private final FeatureEntry[] entries;
	
	public Feature(String id, String version, String label, String image,
			String providerName, String copyright, String licenseURL,
			String license, String description, FeatureEntry[] entries) {
		this.id = id;
		this.version = version;
		this.label = label;
		this.image = image;
		this.providerName = providerName;
		this.copyright = copyright;
		this.licenseURL = licenseURL;
		this.license = license;
		this.entries = entries;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public String getVersion() {
		return version;
	}

	public String getLabel() {
		return label;
	}

	public String getImage() {
		return image;
	}

	public String getProviderName() {
		return providerName;
	}

	public String getCopyright() {
		return copyright;
	}

	public String getLicenseURL() {
		return licenseURL;
	}

	public String getLicense() {
		return license;
	}
	
	public String getDescription() {
		return description;
	}

	public FeatureEntry[] getEntries() {
		return entries;
	}
}
