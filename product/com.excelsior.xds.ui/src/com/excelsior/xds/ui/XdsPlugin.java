package com.excelsior.xds.ui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.InjectorFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.excelsior.xds.core.console.ColorStreamType;
import com.excelsior.xds.core.console.IXdsConsoleFactory;
import com.excelsior.xds.core.ide.facade.IdeCore;
import com.excelsior.xds.core.resource.EncodingUtils;
import com.excelsior.xds.core.text.TextEncoding;
import com.excelsior.xds.ui.console.XdsConsoleManager;
import com.excelsior.xds.ui.decorators.RefreshXdsDecoratorJob;
import com.excelsior.xds.ui.swt.resources.SharedResourceManager;
import com.excelsior.xds.ui.todos.TodoTaskMarkerBuilder;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class XdsPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.excelsior.xds.ui"; //$NON-NLS-1$

	// The shared instance
	private static XdsPlugin plugin;
	
	private static BundleContext context;

	/**
	 * The constructor
	 */
	public XdsPlugin() {
	}
	
	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		XdsPlugin.context = context;
		
		// to force load com.excelsior.core.ide plugin
		IdeCore.instance();
		
		TodoTaskMarkerBuilder.install();
		
		// Set default encoding to cp866 if it exists in jvm:
        if (TextEncoding.isCodepageSupported(TextEncoding.CodepageId.CODEPAGE_866.charsetName)) {
            InstanceScope.INSTANCE.getNode(ResourcesPlugin.PI_RESOURCES).put(ResourcesPlugin.PREF_ENCODING, EncodingUtils.DOS_ENCODING); //$NON-NLS-1$
        }
        
        InjectorFactory.getDefault().
		addBinding(IXdsConsoleFactory.class).implementedBy(XdsConsoleManager.class);
		
		new RefreshXdsDecoratorJob().schedule();
		
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				for (ColorStreamType colorStreamType : ColorStreamType.values()) {
					SharedResourceManager.createColor(colorStreamType.getRgb());
				}
				
				// force loading of the editor plugin, because it contributes the IModulaSyntaxColorer service.
				loadPlugins(Arrays.asList("com.excelsior.xds.ui.editor")); //$NON-NLS-1$
			}
		});
	}
	
	/**
	 * Force loading of the certain plugins.
	 * @param ids
	 */
	@SuppressWarnings("deprecation")
	private static void loadPlugins(List<String> ids){
		for (String id : ids) {
			Platform.getPlugin(id);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		SharedResourceManager.dispose();
		TodoTaskMarkerBuilder.uninstall();
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static XdsPlugin getDefault() {
		return plugin;
	}
}
