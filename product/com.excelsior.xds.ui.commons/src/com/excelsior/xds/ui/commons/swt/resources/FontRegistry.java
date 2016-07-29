package com.excelsior.xds.ui.commons.swt.resources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import com.excelsior.xds.core.utils.IDisposable;
import com.excelsior.xds.ui.commons.utils.UiUtils;

/**
 * Factory for the {@link Font}, create color by {@link FontData} (reusing existing Font for such {@link FontData}). On disposal - dispose them at once.
 * 
 * @author lsa80
 */
public class FontRegistry implements IDisposable {
	private final Map<List<FontData>, Font> fontDatas2Font = new HashMap<List<FontData>, Font>();
	private final boolean isUseCurrentDisplay;
	
	public FontRegistry(boolean isUseCurrentDisplay) {
		this.isUseCurrentDisplay = isUseCurrentDisplay;
	}
	
	public FontRegistry() {
		this(true);
	}

	public Font createFont(FontData[] fontDatas) {
		List<FontData> key = Arrays.asList(fontDatas);
		Font font = fontDatas2Font.get(key);
		if (font == null) {
			font = new Font(isUseCurrentDisplay? Display.getCurrent() : Display.getDefault(), fontDatas);
			fontDatas2Font.put(key, font);
		}
		return font;
	}

	@Override
	public void dispose() {
		for (Entry<List<FontData>, Font> entry : fontDatas2Font.entrySet()) {
			Font f = entry.getValue();
			UiUtils.dispose(f);
		}
		fontDatas2Font.clear();
	}
}
