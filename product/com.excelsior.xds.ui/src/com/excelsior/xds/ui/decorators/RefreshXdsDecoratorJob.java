package com.excelsior.xds.ui.decorators;

import java.util.NoSuchElementException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.ui.internal.nls.Messages;

public class RefreshXdsDecoratorJob extends Job {
	public RefreshXdsDecoratorJob() {
		super(Messages.RefreshXdsDecoratorJob_RefreshXdsDecorators);
		setSystem(true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try{
			IProject p = XdsModelManager.getInstance().getNextProjectForDecoratorRefresh();
			DecoratorUtils.refreshXdsDecorators(p, InCompilationSetDecorator.ID);
		}
		catch(NoSuchElementException e) {
		}
		
		schedule(500);
        return Status.OK_STATUS;
	}
}
