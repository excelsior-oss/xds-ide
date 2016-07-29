package com.excelsior.xds.core.sdk;

public abstract class SdkEvent {
	private final Sdk source;
	
	SdkEvent(Sdk source) {
		this.source = source;
	}
	
	public Sdk getSource() {
		return source;
	}
}
