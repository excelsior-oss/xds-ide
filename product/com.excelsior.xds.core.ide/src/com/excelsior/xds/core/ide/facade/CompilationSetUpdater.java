package com.excelsior.xds.core.ide.facade;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.excelsior.xds.core.compiler.compset.CompilationSetManager;
import com.excelsior.xds.core.jobs.IJobListener;
import com.excelsior.xds.core.jobs.ListenableJob;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.core.progress.DelegatingProgressMonitor;
import com.excelsior.xds.core.progress.IListenableProgressMonitor;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.binding.IModulaSymbolCacheListener;
import com.excelsior.xds.parser.modula.symbol.binding.ModulaSymbolCache;
import com.google.common.collect.Lists;

public class CompilationSetUpdater {
	private final AtomicBoolean updateRequested = new AtomicBoolean(false);
	private final Set<IProject> projectsToUpdate = new HashSet<>();
	
	private final ModulaSymbolCacheListener modulaSymbolCacheListener = new ModulaSymbolCacheListener(updateRequested, projectsToUpdate);
	
	private final UpdaterJob updaterJob = new UpdaterJob(updateRequested, projectsToUpdate);
	
	public void install() {
		ModulaSymbolCache.instance().addListener(modulaSymbolCacheListener);
		updaterJob.schedule();
	}
	
	public void uninstall() {
		ModulaSymbolCache.instance().removeListener(modulaSymbolCacheListener);
		updaterJob.setActive(false);
	}
	
	public static CompilationSetUpdater instance() {
		return CompilationSetUpdaterHolder.INSTANCE;
	}
	
	private static class ModulaSymbolCacheListener implements IModulaSymbolCacheListener {
		private final  AtomicBoolean updateRequested;
		private final Set<IProject> projectsToUpdate;
		
		public ModulaSymbolCacheListener(AtomicBoolean updateRequested,
				Set<IProject> projectsToUpdate) {
			this.updateRequested = updateRequested;
			this.projectsToUpdate = projectsToUpdate;
		}

		@Override
		public void moduleSymbolAdded(IModuleSymbol oldSymbol,
				IModuleSymbol newSymbol) {
			if (!areImportsSame(oldSymbol, newSymbol)) {
				invokeGetCompilationSet(newSymbol);
			}
		}
		
		private boolean areImportsSame(IModuleSymbol oldSymbol,
				IModuleSymbol newSymbol) {
			if (oldSymbol == null) {
				return newSymbol == null;
			}
			else if (newSymbol == null) {
				return false;
			}
			
			// TODO : simplify this after getImports converted to Collection instead of Iterable
			return Objects.equals(Lists.newArrayList(oldSymbol.getImports()), Lists.newArrayList(newSymbol.getImports()));
		}

		private void invokeGetCompilationSet(IModuleSymbol symbol) {
			if (symbol == null) {
				return;
			}
			IWorkspaceRoot root = ResourceUtils.getWorkspaceRoot();
			IFileStore sourceFile = symbol.getSourceFile();
			if (sourceFile != null && root != null) {
				IResource[] fileResources = root.findFilesForLocationURI(sourceFile.toURI());
				Set<IProject> projects = new HashSet<>();
				for (IResource r : fileResources) {
					projects.add(r.getProject());
				}
				synchronized (projectsToUpdate) {
					projectsToUpdate.addAll(projects);
				}
				updateRequested.set(true);
			}
		}

		@Override
		public void moduleSymbolRemoved(IModuleSymbol symbol) {
			invokeGetCompilationSet(symbol);
		}
	}
	
	private static class UpdaterJob extends ListenableJob {
		private static final int RESCHEDULE_RATE = 1000; // reschedule after this amount of ms
		private final AtomicBoolean updateRequested;
		private final Set<IProject> projectsToUpdate;
		
		private volatile boolean isActive = true;

		public UpdaterJob(AtomicBoolean updateRequested, Set<IProject> projectsToUpdate) {
			super("Update compilation set");
			this.updateRequested = updateRequested;
			this.projectsToUpdate = projectsToUpdate;
		}
		
		public void setActive(boolean isActive) {
			this.isActive = isActive;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (updateRequested.compareAndSet(true, false)) {
				DelegatingProgressMonitor listenableMonitor = DelegatingProgressMonitor.wrap(monitor);
				addListener(new IJobListener() {
					@Override
					public void canceled() {
						listenableMonitor.setCanceled(true);
					}
				});
				IProject[] projects;
				synchronized (projectsToUpdate) {
					projects =  new IProject[projectsToUpdate.size()];
					projects = projectsToUpdate.toArray(projects);
					projectsToUpdate.clear();
				}
				for (IProject p : projects) {
					replaceCompilationSet(p, listenableMonitor);
				}
			}
			if (isActive) {
				schedule(RESCHEDULE_RATE);
			}
			return Status.OK_STATUS;
		}
		
		private void replaceCompilationSet(IProject p, IListenableProgressMonitor monitor) {
			if (CompilationSetManager.getInstance().updateCompilationSet(p, null, monitor)) {
				XdsModelManager.getInstance().enqueProjectForDecoratorRefresh(p);
			}
		}
	}
	
	private static class CompilationSetUpdaterHolder {
		static CompilationSetUpdater INSTANCE = new CompilationSetUpdater();
	}
}
