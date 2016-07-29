package com.excelsior.xds.builder.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.excelsior.xds.builder.internal.nls.Messages;
import com.excelsior.xds.core.builders.XdsSourceBuilderConstants;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.natures.NatureIdRegistry;
import com.excelsior.xds.core.utils.BuilderUtils;

public class Modula2SourceProjectNature implements IProjectNature {
	
	public static final String ID = NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID;  //$NON-NLS-1$
	
	private IProject project;
	
	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}
	
	@Override
	public void configure() throws CoreException {
		BuilderUtils.addBuilderToProject(project, XdsSourceBuilderConstants.BUILDER_ID);
		new Job(Messages.Modula2SourceProjectNature_BuildingSources) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					BuilderUtils.applySdkToProject(project, false, monitor);
				} catch (CoreException e) {
					LogHelper.logError(e);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	@Override
	public void deconfigure() throws CoreException {
	}
}
