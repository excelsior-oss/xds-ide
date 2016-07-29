package com.excelsior.xds.ui.commons.swt.resources;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import com.excelsior.xds.core.utils.CompositedDisposable;
import com.excelsior.xds.core.utils.IDisposable;

public class ResourceRegistry implements IDisposable{
	private final ColorRegistry colorRegistry;
	private final FontRegistry fontRegistry;
	private final CompositedDisposable compositedDisposable;
	
	public ResourceRegistry(boolean isUseCurrentDisplay) {
		colorRegistry = new ColorRegistry(isUseCurrentDisplay);
		fontRegistry = new FontRegistry(isUseCurrentDisplay);
		compositedDisposable = new CompositedDisposable(colorRegistry, fontRegistry);
	}
	
	public ResourceRegistry() {
		this(true);
	}
	
	public Color createColor(RGB rgb) {
		return colorRegistry.createColor(rgb);
	}
	
	public Font createFont(FontData[] fontDatas) {
		return fontRegistry.createFont(fontDatas);
	}

	@Override
	public void dispose() {
		compositedDisposable.dispose();
	}
}
