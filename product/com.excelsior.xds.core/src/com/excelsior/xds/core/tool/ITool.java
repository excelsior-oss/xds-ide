package com.excelsior.xds.core.tool;

import java.util.List;

import org.eclipse.core.resources.IResource;

import com.excelsior.xds.core.console.IXdsConsole;

public interface ITool {
    
    String getName();
    
    String getLocation();

    boolean isEnabled(List<IResource> resources);
	
	void invoke(List<IResource> resources, IXdsConsole console);
	
	void addListener(IToolListener listener);
}