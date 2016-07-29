package com.excelsior.xds.core.tool;

import com.excelsior.xds.core.sdk.SdkTool;

public class XToolFactory {
    
	public static ITool createFrom(SdkTool toolDesc) {
		return new XTool(toolDesc);
	}
	
}
