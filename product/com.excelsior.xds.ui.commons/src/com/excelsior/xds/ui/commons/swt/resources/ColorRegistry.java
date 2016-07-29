package com.excelsior.xds.ui.commons.swt.resources;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.excelsior.xds.core.utils.IDisposable;
import com.excelsior.xds.ui.commons.utils.UiUtils;

/**
 * Factory for the {@link Color}, create color by {@link RGB} (reusing existing color for such {@link RGB}). On disposal - dispose them at once.
 * 
 * @author lsa80
 */
public class ColorRegistry implements IDisposable{
	private final Map<RGB, Color> rgb2Color = new HashMap<RGB, Color>();
	private final boolean isUseCurrentDisplay;
	
	public ColorRegistry(boolean isUseCurrentDisplay) {
		this.isUseCurrentDisplay = isUseCurrentDisplay;
	}
	
	public ColorRegistry() {
		this(true);
	}

	public Color createColor(RGB rgb) {
		if (rgb == null) {
			return null;
		}
		Color color = rgb2Color.get(rgb);
		if (color == null) {
			color = new Color(isUseCurrentDisplay? Display.getCurrent() : Display.getDefault() , rgb);
			rgb2Color.put(rgb, color);
		}
		return color;
	}

	@Override
	public void dispose() {
		for (Map.Entry<RGB, Color> entry : rgb2Color.entrySet()) {
			Color c = entry.getValue();
			UiUtils.dispose(c);
		}
		rgb2Color.clear();
	}
}
