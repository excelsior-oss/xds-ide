package com.excelsior.xds.builder.listener;

import org.eclipse.core.resources.IProject;

import com.excelsior.xds.core.builders.XdsBuildResult;

public interface IBuilderListener {
	void onBuildStarted(IProject p);
	void onBuildFinished(IProject p, XdsBuildResult buildRes);
}
