package com.excelsior.xds.core.sdk;


public interface ISdkListener {
	void sdkChanged(SdkChangeEvent e);
	void sdkRemoved(SdkRemovedEvent e);
}