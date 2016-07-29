package com.excelsior.xds.core.model.internal.resources.mapping;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.excelsior.xds.core.model.plugin.ModelPlugin;

public class SimpleResourceMapping extends ResourceMapping {
	private final Object element;
	private final IResource r;

	public SimpleResourceMapping(Object element, IResource r) {
		this.element = element;
		this.r = r;
	}

	@Override
	public Object getModelObject() {
		return element;
	}

	@Override
	public String getModelProviderId() {
		return ModelPlugin.PLUGIN_ID;
	}

	@Override
	public IProject[] getProjects() {
		return new IProject[]{r.getProject()};
	}

	@Override
	public ResourceTraversal[] getTraversals(
			ResourceMappingContext context,
			IProgressMonitor monitor) throws CoreException {
		return new ResourceTraversal[]{new ResourceTraversal(new IResource[]{r}, IResource.DEPTH_ONE, IResource.NONE)};
	}
}