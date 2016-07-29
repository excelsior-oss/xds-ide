package com.excelsior.xds.launching.commons.internal.console;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.console.ConsoleColorProvider;
import org.eclipse.swt.graphics.Color;

import com.excelsior.xds.core.console.ColorStreamType;
import com.excelsior.xds.ui.commons.swt.resources.ResourceRegistry;

public class ProcessConsoleColorProvider extends ConsoleColorProvider {
	private ResourceRegistry resourceRegistry;
	
	public ProcessConsoleColorProvider() {
		this.resourceRegistry = new ResourceRegistry(false);
		resourceRegistry.createColor(ColorStreamType.NORMAL.getRgb());
		resourceRegistry.createColor(ColorStreamType.ERROR.getRgb());
		resourceRegistry.createColor(ColorStreamType.USER_INPUT.getRgb());
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public Color getColor(String streamIdentifer) {
		if (IDebugUIConstants.ID_STANDARD_INPUT_STREAM.equals(streamIdentifer)) {
			return resourceRegistry.createColor(ColorStreamType.USER_INPUT.getRgb());
		}
		else if (IDebugUIConstants.ID_STANDARD_ERROR_STREAM.equals(streamIdentifer)) {
			return resourceRegistry.createColor(ColorStreamType.ERROR.getRgb());
		}
		else if (IDebugUIConstants.ID_STANDARD_OUTPUT_STREAM.equals(streamIdentifer)) {
			return resourceRegistry.createColor(ColorStreamType.NORMAL.getRgb());
		}
		return null;
	}
}
