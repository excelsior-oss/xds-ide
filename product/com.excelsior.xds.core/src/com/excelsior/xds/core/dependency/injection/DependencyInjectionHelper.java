package com.excelsior.xds.core.dependency.injection;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.osgi.framework.BundleContext;

import com.excelsior.xds.core.log.LogHelper;

@SuppressWarnings("restriction")
public final class DependencyInjectionHelper {
	public static void inject(Object object, BundleContext context) {
		IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(context);
		if (serviceContext != null) {
			try{
				ContextInjectionFactory.inject(object, serviceContext);
			}
			catch(InjectionException e) {
				LogHelper.logError(e);
			}
		}
	}

	private DependencyInjectionHelper(){
	}
}
