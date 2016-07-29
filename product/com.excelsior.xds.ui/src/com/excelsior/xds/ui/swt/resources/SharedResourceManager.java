package com.excelsior.xds.ui.swt.resources;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import com.excelsior.xds.ui.commons.swt.resources.ResourceRegistry;

/**
 * Share resource holder. Necessary resource are initialized at plugin startup.
 * @author lsa80
 */
public final class SharedResourceManager {
	private ResourceRegistry resourceRegistry = new ResourceRegistry();

	private static class SharedResourceManagerHolder{
		static SharedResourceManager instance = new SharedResourceManager();
	}
	
	public static Color createColor(RGB rgb) {
		return instance().resourceRegistry.createColor(rgb);
	}
	
	public static Color getColor(RGB rgb) {
		return instance().resourceRegistry.createColor(rgb);
	}
	
	public static void dispose() {
		instance().resourceRegistry.dispose();
	}
	
	private static SharedResourceManager instance() {
		return SharedResourceManagerHolder.instance;
	}

	private SharedResourceManager(){
	}
}
