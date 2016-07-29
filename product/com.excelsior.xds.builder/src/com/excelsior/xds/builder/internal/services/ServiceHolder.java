package com.excelsior.xds.builder.internal.services;

import javax.inject.Inject;

import com.excelsior.xds.builder.BuilderPlugin;
import com.excelsior.xds.core.console.IXdsConsoleFactory;
import com.excelsior.xds.core.dependency.injection.DependencyInjectionHelper;

public final class ServiceHolder {
	@Inject
	private IXdsConsoleFactory consoleFactory;
	
	private ServiceHolder() {
		DependencyInjectionHelper.inject(this, BuilderPlugin.getContext());
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