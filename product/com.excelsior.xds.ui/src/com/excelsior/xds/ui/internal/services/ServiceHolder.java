package com.excelsior.xds.ui.internal.services;

import javax.inject.Inject;

import com.excelsior.xds.core.console.IXdsConsoleFactory;
import com.excelsior.xds.core.dependency.injection.DependencyInjectionHelper;
import com.excelsior.xds.ui.XdsPlugin;
import com.excelsior.xds.ui.tools.colorers.IModulaSyntaxColorer;

public final class ServiceHolder {
	@Inject
	private IXdsConsoleFactory consoleFactory;
	
	@Inject
 	private IModulaSyntaxColorer modulaColorer;
	
	private ServiceHolder() {
		DependencyInjectionHelper.inject(this, XdsPlugin.getContext());
	}

	public static ServiceHolder getInstance() {
		return ServiceHolderHolder.instance;
	}

	public IXdsConsoleFactory getConsoleFactory() {
		return consoleFactory;
	}
	
	public IModulaSyntaxColorer getModulaColorer() {
		return modulaColorer;
	}

	private static class ServiceHolderHolder {
		static ServiceHolder instance = new ServiceHolder();
	}
}