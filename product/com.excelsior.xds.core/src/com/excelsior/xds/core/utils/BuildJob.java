package com.excelsior.xds.core.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.excelsior.xds.core.jobs.ListenableJob;

public abstract class BuildJob extends ListenableJob {
	private final IProject project;
		
	BuildJob(IProject project, String name) {
		super(name);
		this.project = project;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected final IStatus run(IProgressMonitor monitor) {
		try {
			doBuild(project, monitor);
		} 
		catch (CoreException e) {
			return e.getStatus();
		} 
		finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}
	
	@Override
	public boolean belongsTo(Object family) {
		return BuilderUtils.BUILD_JOB_FAMILY.equals(family);
	}

	protected abstract void doBuild(IProject project, IProgressMonitor monitor) throws CoreException;
}