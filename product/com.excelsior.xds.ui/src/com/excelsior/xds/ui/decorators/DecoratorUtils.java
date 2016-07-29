package com.excelsior.xds.ui.decorators;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IDecoratorManager;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;

public abstract class DecoratorUtils {
    public static void refreshXdsDecorators(IProject project, String decoratorId){
        IDecoratorManager decoratorManager = WorkbenchUtils.getWorkbench().getDecoratorManager();
        if (decoratorManager != null && decoratorManager.getEnabled(decoratorId)) {
            final IXdsDecorator decorator = (IXdsDecorator)decoratorManager.getBaseLabelProvider(decoratorId);
            if (decorator != null && project.exists()) {
            	try {
            		project.accept(new IResourceVisitor() {
            			@Override
            			public boolean visit(IResource resource) throws CoreException {
            				decorator.refresh(new IResource[]{resource});
            				return true;
            			}
            		});
            	} catch (CoreException e) {
            		LogHelper.logError(e);
            	}
            }
        }
    }
}
