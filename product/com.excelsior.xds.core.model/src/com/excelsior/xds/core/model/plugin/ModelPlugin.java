package com.excelsior.xds.core.model.plugin;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.excelsior.xds.builder.buildsettings.BuildSettingsCache;
import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.compiler.compset.CompilationSetManager;
import com.excelsior.xds.core.compiler.compset.ExternalResourceManager;
import com.excelsior.xds.core.jobs.IJobListener;
import com.excelsior.xds.core.jobs.ListenableJob;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.core.model.internal.nls.Messages;
import com.excelsior.xds.core.progress.DelegatingProgressMonitor;
import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.resource.ResourceUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class ModelPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.excelsior.xds.core.model"; //$NON-NLS-1$

	// The shared instance
	private static ModelPlugin plugin;
	
	/**
	 * The constructor
	 */
	public ModelPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		List<IProject> projects = ProjectUtils.getXdsProjects();
		final CountDownLatch latch = new CountDownLatch(projects.size());
		
		for (final IProject p : projects) {
			ResourceUtils.scheduleWorkspaceRunnable(new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
	                try{
	                	BuildSettings buildSettings = BuildSettingsCache.createBuildSettings(p);
						List<File> lookupDirs = buildSettings.getLookupDirs();
						ExternalResourceManager.linkExternals(p, lookupDirs, true, true, monitor);
	                }
	                finally{
	                	latch.countDown();
	                }
				}
			}, p, Messages.Activator_LinkingExtFilesToResources, null, false);
		}
		
		new ListenableJob("Refreshing model") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					latch.await();
					DelegatingProgressMonitor listenableMonitor = DelegatingProgressMonitor.wrap(monitor);
					// add cancelation listener to explicitly cancel the listenable monitor.
					addListener(new IJobListener() {
						@Override
						public void canceled() {
							listenableMonitor.setCanceled(true);
						}
					});
					
					for (IProject p : projects) {
						XdsModelManager.refreshExternals(p);
						
						if (CompilationSetManager.getInstance().updateCompilationSet(p, null, listenableMonitor)) {
							XdsModelManager.getInstance().enqueProjectForDecoratorRefresh(p);
						}
					}
				} catch (InterruptedException e1) {
					LogHelper.logError(e1);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
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
	public static ModelPlugin getDefault() {
		return plugin;
	}

}
