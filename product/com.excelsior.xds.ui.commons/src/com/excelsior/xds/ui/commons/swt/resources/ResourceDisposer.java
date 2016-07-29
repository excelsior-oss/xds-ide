package com.excelsior.xds.ui.commons.swt.resources;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Resource;

import com.excelsior.xds.core.utils.IDisposable;
import com.excelsior.xds.ui.commons.utils.UiUtils;

/**
 * Manages a bunch of resources (i.e. {@link Color}, {@link Font} etc) and dispose them at once
 * 
 * @author lsa80
 */
public class ResourceDisposer implements IDisposable {
	private boolean isDisposed;
	private List<Resource> resources = new ArrayList<Resource>();
	
	/**
	 * Registers resource for the future disposal
	 */
	public <T extends Resource> T register(T r) {
		resources.add(r);
		return r;
	}
	
	@Override
	public void dispose() {
		if (!isDisposed) {
			for (Resource r : resources) {
				UiUtils.dispose(r);
			}
			isDisposed = true;
		}
	}
}