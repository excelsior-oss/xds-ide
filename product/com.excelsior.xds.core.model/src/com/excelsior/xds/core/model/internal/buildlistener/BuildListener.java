package com.excelsior.xds.core.model.internal.buildlistener;

import org.eclipse.core.resources.IProject;

import com.excelsior.xds.builder.listener.IBuilderListener;
import com.excelsior.xds.core.builders.XdsBuildResult;
import com.excelsior.xds.core.model.XdsModelManager;

public class BuildListener implements IBuilderListener {
	public BuildListener() {
	}

	@Override
	public void onBuildStarted(IProject p) {
	}

	@Override
	public void onBuildFinished(IProject p, XdsBuildResult buildRes) {
         XdsModelManager.refreshProject(p);
	}
}
