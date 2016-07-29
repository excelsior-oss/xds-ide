package com.excelsior.xds.core.ide.plugin;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.excelsior.xds.core.ide.facade.CompilationSetUpdater;
import com.excelsior.xds.core.ide.symbol.SymbolModelManager;
import com.excelsior.xds.parser.modula.XdsParserManager;

public class IdeCorePlugin implements BundleActivator {
	public static final String PLUGIN_ID = "com.excelsior.xds.core.ide"; //$NON-NLS-1$
	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		IdeCorePlugin.context = bundleContext;
		SymbolModelManager.instance().startup();
		CompilationSetUpdater.instance().install();
		XdsParserManager.turnOnDebugPrint(SymbolModelManager.IS_DEBUG_MODEL_MODIFICATIONS);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		IdeCorePlugin.context = null;
		CompilationSetUpdater.instance().uninstall();
		SymbolModelManager.instance().shutdown();
	}

}
