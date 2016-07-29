package com.excelsior.xds.ui.decorators;

import java.util.Collection;

import org.eclipse.core.resources.IProject;

import com.excelsior.xds.builder.listener.IBuilderListener;
import com.excelsior.xds.core.builders.XdsBuildResult;
import com.excelsior.xds.core.compiler.compset.ICompilationSetListener;

/**
 * TODO : 2016.11.01: implement
 * @author lsa80
 */
public class UpdateDecoratorListener implements IBuilderListener, ICompilationSetListener {
	@Override
	public void onBuildStarted(IProject p) {
	}

	@Override
	public void onBuildFinished(IProject p, XdsBuildResult buildRes) {
		// TODO : should only be invoked when compilation set was affected
//		XdsModelManager.getInstance().enqueProjectForDecoratorRefresh(p);
	}

	@Override
	public void added(String projectName, Collection<String> pathes) {
//		IProject p = ProjectUtils.getProject(projectName);
//		XdsModelManager.getInstance().enqueProjectForDecoratorRefresh(p);
	}

	@Override
	public void removed(String projectName, Collection<String> compilationSet) {
//		IProject p = ProjectUtils.getProject(projectName);
//		XdsModelManager.getInstance().enqueProjectForDecoratorRefresh(p);
	}
}