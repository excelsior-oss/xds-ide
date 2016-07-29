package com.excelsior.xds.ui.editor.commons.internal.modula.ruler;

import com.excelsior.xds.core.extensionpoint.ExtensionRegistry;
import com.excelsior.xds.ui.editor.commons.plugin.EditorCommonsPlugin;
import com.excelsior.xds.ui.editor.commons.ruler.IRulerPainter;

public class LineNumberColumnPainterRegistry extends ExtensionRegistry<IRulerPainter> {
	public static LineNumberColumnPainterRegistry get() {
		return LineNumberColumnPainterRegistryHolder.instance;
	}
	
	private static class LineNumberColumnPainterRegistryHolder {
		static LineNumberColumnPainterRegistry instance = new LineNumberColumnPainterRegistry(EditorCommonsPlugin.PLUGIN_ID, "lineNumberColumnPainter", "class"); //$NON-NLS-1$ //$NON-NLS-2$
		static {
			instance.contributions();
		}
	}
	
	public LineNumberColumnPainterRegistry(String pluginId,
			String extensionPointName, String executableExtensionPropertyName) {
		super(pluginId, extensionPointName, executableExtensionPropertyName);
	}

}
