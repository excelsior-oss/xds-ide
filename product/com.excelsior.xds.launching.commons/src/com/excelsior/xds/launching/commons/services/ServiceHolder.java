package com.excelsior.xds.launching.commons.services;

import javax.inject.Inject;

import com.excelsior.xds.core.console.IXdsConsoleFactory;
import com.excelsior.xds.core.dependency.injection.DependencyInjectionHelper;
import com.excelsior.xds.launching.commons.internal.plugin.LaunchingCommonsPlugin;

public final class ServiceHolder {
	@Inject
	private IXdsConsoleFactory consoleFactory;
	
	private ServiceHolder() {
		DependencyInjectionHelper.inject(this, LaunchingCommonsPlugin.getContext());
	}
	
	public static ServiceHolder getInstance() {
		return ServiceHolderHolder.instance;
	}

	public IXdsConsoleFactory getConsoleFactory() {
		return consoleFactory;
	}
	
	private static class ServiceHolderHolder {
		static ServiceHolder instance = new ServiceHolder();
	}
}