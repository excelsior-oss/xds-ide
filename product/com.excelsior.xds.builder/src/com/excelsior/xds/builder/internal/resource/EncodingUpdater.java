package com.excelsior.xds.builder.internal.resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import com.excelsior.xds.builder.buildsettings.BuildSettingsCache;
import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.resource.EncodingUtils;
import com.excelsior.xds.core.resource.IResourceAttributes;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.resource.XdsResourceChangeListener;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;

/**
 * Sets charset property of the changed {@link IFile}`s if resource extension doesnot belongs to {@link BuildSettings#getDerivedResourcesExtensions()}
 * @author lsa80
 * @see {@link IResourceChangeListener}
 * @see {@link IWorkspace#addResourceChangeListener(IResourceChangeListener)}
 * @see {@link IFile#setCharset(String)}
 */
public final class EncodingUpdater extends XdsResourceChangeListener {
	private final Set<IResource> resourcesToUpdateEncoding = CollectionsUtils.newConcurentHashSet();
	
	@Override
	protected void beginDeltaProcessing(IResourceDelta delta) {
        resourcesToUpdateEncoding.clear();
	}
	
	@Override
	protected boolean handleResourceAdded(IResourceDelta rootDelta, IResourceDelta delta, IResource affectedResource) {
		resourcesToUpdateEncoding.add(affectedResource);
		return super.handleResourceAdded(rootDelta, delta, affectedResource);
	}
	
	@Override
	protected boolean handleResourceChanged(IResourceDelta rootDelta, IResourceDelta delta, IResource affectedResource) {
        resourcesToUpdateEncoding.add(affectedResource);
		return super.handleResourceChanged(rootDelta, delta, affectedResource);
	}
	
	@Override
	protected void endDeltaProcessing(IResourceDelta delta) {
		// TODO : 2016.01.11 fix it
		scheduleUpdateResourcesEncodings();
	}
	
	private void scheduleUpdateResourcesEncodings() {
		final IResource[] resources = resourcesToUpdateEncoding.toArray(new IResource[0]);
		resourcesToUpdateEncoding.clear();
		ISchedulingRule rule = ResourceUtils.createRule(Arrays.asList(resources));
		ResourceUtils.scheduleWorkspaceRunnable(new IWorkspaceRunnable(){
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask("Fix encoding job", resources.length); //$NON-NLS-1$
				for (IResource r : resources) {
					if (monitor.isCanceled()) {
						break;
					}
					if (r instanceof IFile && r.exists()) {
						BuildSettings settings = BuildSettingsCache.createBuildSettings(r.getProject());
						boolean isDetermineEncoding = false;
						if (settings != null && !settings.getDerivedResourcesExtensions().contains(r.getFileExtension())){
							isDetermineEncoding = true;
						}
						else if (settings == null){
							isDetermineEncoding = true; // if SDK is not set for the project - unconditionally determine encoding
						}
						if (isDetermineEncoding) {
							try {
								if (!Boolean.TRUE.toString().equals(r.getPersistentProperty(IResourceAttributes.ENCODING_DETERMINED))) {
									EncodingUtils.determineAndSetEncoding((IFile) r, monitor);
									r.setPersistentProperty(IResourceAttributes.ENCODING_DETERMINED, Boolean.TRUE.toString());
								}
							} 
							catch (IOException e) {
								// just ignore. See KIDE-394
							}
							catch (CoreException e) {
								LogHelper.logError(e);
							}
						}
					}
					monitor.internalWorked(1);
				}
				monitor.done();
			}
			
		}, rule, "Fix encoding job", false); //$NON-NLS-1$
	}
	
	public static void install(){
		instance(); // instantiates the EncodingUpdater - this adds resource change listener in the super`s constructor
	}
	
	public static void uninstall(){
		instance().stop();
	}
	
	private static EncodingUpdater instance(){
		return EncodingUpdaterHolder.INSTANCE;
	}
	
	private static class EncodingUpdaterHolder {
		static EncodingUpdater INSTANCE = new EncodingUpdater(); 
	}
}
