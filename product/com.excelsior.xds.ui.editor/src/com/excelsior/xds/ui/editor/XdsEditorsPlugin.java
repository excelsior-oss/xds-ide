package com.excelsior.xds.ui.editor;

import java.io.IOException;

import org.eclipse.e4.core.di.InjectorFactory;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.osgi.framework.BundleContext;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.ui.commons.utils.SwtUtils;
import com.excelsior.xds.ui.editor.internal.manager.EditorManager;
import com.excelsior.xds.ui.editor.modula.ModulaDocumentProvider;
import com.excelsior.xds.ui.editor.modula.ModulaSyntaxColorer;
import com.excelsior.xds.ui.editor.modula.template.SourceCodeTemplateContextType;
import com.excelsior.xds.ui.editor.oberon.OberonDocumentProvider;
import com.excelsior.xds.ui.tools.colorers.IModulaSyntaxColorer;

/**
 * The activator class controls the "com.excelsior.xds.ui.editor" plug-in life cycle
 */
@SuppressWarnings("restriction")
public class XdsEditorsPlugin extends AbstractUIPlugin 
{
	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "com.excelsior.xds.ui.editor"; //$NON-NLS-1$

    /**
     * The key to store customized templates.
     */
    private static final String TEMPLATES_KEY = "com.excelsior.xds.ui.editor.templates.key"; //$NON-NLS-1$
    

    /**
     * The key to store customized templates.
     */
    public static final String HYPERLINK_TARGET_MODULA_CODE = "com.excelsior.xds.ui.ModulaCode"; //$NON-NLS-1$
    
    
	// The shared instance
	private static XdsEditorsPlugin plugin;
	
    /**
     * The template store for XDS editors.
     */
	private ContributionTemplateStore templateStore;

    /**
     * The template context type registry for XDS editors.
     */
    private ContributionContextTypeRegistry templateContextTypeRegistry;
    
    /**
     * The shared Modula-2 source file document provider.
     */
    private IDocumentProvider modulaDocumentProvider;
    
    /**
     * The shared Oberon-2 source file document provider.
     */
    private IDocumentProvider oberonDocumentProvider;
    
	/**
	 * The constructor
	 */
	public XdsEditorsPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		SwtUtils.executeInUiThread(new Runnable() {
			@Override
			public void run() {
				EditorManager.getInstance().initialize();
			}
		});
		
		 InjectorFactory.getDefault().
			addBinding(IModulaSyntaxColorer.class).implementedBy(ModulaSyntaxColorer.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static XdsEditorsPlugin getDefault() {
		return plugin;
	}

	/**
     * Returns the template store for XDS editors templates.
     * 
     * @return  the template store
     */
    public synchronized TemplateStore getTemplateStore() {
        if (templateStore == null) {
            templateStore = new ContributionTemplateStore(getContextTypeRegistry(), getPreferenceStore(), TEMPLATES_KEY);
            try {
                templateStore.load();
            } catch (IOException e) {
                LogHelper.logError(e);
            }
        }
        return templateStore;
    }

    /**
     * Return the context type registry.
     * 
     * @return  the context type registry
     */
    public synchronized ContextTypeRegistry getContextTypeRegistry() {
        if (templateContextTypeRegistry == null) {
            templateContextTypeRegistry = new ContributionContextTypeRegistry();
            templateContextTypeRegistry.addContextType(SourceCodeTemplateContextType.MODULA_CONTEXTTYPE);
        }
        return templateContextTypeRegistry;
    }
    
    
    /**
     * Returns the shared document provider for Modula-2 source files
     * used by this plug-in instance.
     *
     * @return the shared document provider for Modula-2 source files
     */
    public synchronized IDocumentProvider getModulaDocumentProvider() {
        if (modulaDocumentProvider == null) {
            modulaDocumentProvider = new ModulaDocumentProvider();
        }
        return modulaDocumentProvider;
    }

    /**
     * Returns the shared document provider for oberon-2 source files
     * used by this plug-in instance.
     *
     * @return the shared document provider for Oberon-2 source files
     */
    public synchronized IDocumentProvider getOberonDocumentProvider() {
        if (oberonDocumentProvider == null) {
            oberonDocumentProvider = new OberonDocumentProvider();
        }
        return oberonDocumentProvider;
    }
}
