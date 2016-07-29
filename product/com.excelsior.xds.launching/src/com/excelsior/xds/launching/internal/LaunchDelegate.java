package com.excelsior.xds.launching.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.excelsior.xds.core.project.NatureUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.launching.commons.delegate.AbstractLaunchDelegate;
import com.excelsior.xds.launching.nls.Messages;

/**
 * A launch delegate for launching Modula-2 applications.
 */
public class LaunchDelegate extends AbstractLaunchDelegate {
    @Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException 
	{
    	try {
			monitor.beginTask(configuration.getName() + "...", 2); //$NON-NLS-1$
			if (monitor.isCanceled()) {
				return;
			}
			monitor.subTask(Messages.LaunchDelegate_CheckingLaunchCfg);

			IProject project = getProject(configuration);
			if (!NatureUtils.hasModula2Nature(project)) {
				abort(Messages.LaunchDelegate_IncorrectProject + ": " + project.getName()); //$NON-NLS-1$
		    }
			Sdk sdk = getProjectSdk(project);
			if (sdk == null) {
				abort(Messages.LaunchDelegate_SdkNotConfigured);
			}
			runModulaApplication(mode, configuration, launch, project, sdk, monitor);
		} 
		finally {
            monitor.done();
		}
	}
}