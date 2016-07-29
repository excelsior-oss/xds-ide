package com.excelsior.xds.ui.decorators;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.ResourceUtil;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.marker.XdsMarkerConstants;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.core.resource.ResourceUtils;

public class ResourceProblemDecorator extends LabelProvider implements ILightweightLabelDecorator {
	private Map<Integer, ImageDescriptor> severity2DecorationImageDesc = new HashMap<Integer, ImageDescriptor>();
	
	@Override
	public void decorate(Object element, IDecoration decoration) {
		IResource res = null;
		IResource resPrj = null;
		if (element instanceof IProject) {
			IXdsProject xdsProject = XdsModelManager.getModel().getXdsProjectBy((IProject)element);
			if (xdsProject != null) {
                resPrj = (IResource)element; 
				element = xdsProject;
			}
		}
		else{
			res = (IResource) ResourceUtil.getAdapter(element, IResource.class, false);
		}
		
		IProblemSource problemSource = null;
		if (res != null) {
			if (res.getProject() != null && res.getProject().isOpen() && res.exists()) {
				problemSource = new ResourcesProblemSource(res);
			}
		}
		else {
			ResourceMapping mapping = (ResourceMapping) ResourceUtil.getAdapter(element, ResourceMapping.class, false);
			problemSource = new ResourcesProblemSource(mapping, resPrj);
		}
		
		if (problemSource != null) {
			try {
				int problemSeverity = problemSource.findMaxProblemSeverity(XdsMarkerConstants.BUILD_PROBLEM_MARKER_TYPE, true, IResource.DEPTH_ONE);
				Image image = getImage(problemSeverity);
				if (image == null) {
					problemSeverity = problemSource.findMaxProblemSeverity(XdsMarkerConstants.BUILD_PROBLEM_MARKER_TYPE, true, IResource.DEPTH_INFINITE);
					image = getImage(problemSeverity);
				}
				
				if (image != null) {
					ImageDescriptor imageDesc = severity2DecorationImageDesc.get(problemSeverity);
					if (imageDesc == null) {
						imageDesc = ImageDescriptor.createFromImageData(image.getImageData());
						severity2DecorationImageDesc.put(problemSeverity, imageDesc);
					}
					decoration.addOverlay(imageDesc);
				}
			} catch (CoreException e) {
				LogHelper.logError(e);
			}
		}
		
	}
	
	private interface IProblemSource {
		int findMaxProblemSeverity(String type, boolean includeSubtypes, int depth) throws CoreException;
	}
	
	private static class ResourcesProblemSource implements IProblemSource{
		
		private IResource[] resources;

		public ResourcesProblemSource(IResource[] resources) {
			this.resources = resources;
		}

		public ResourcesProblemSource(IResource res) {
			this(new IResource[]{res});
		}

		public ResourcesProblemSource(ResourceMapping mapping, IResource resPrj) {
			this(ResourceUtils.getResourcesFrom(mapping));
			if (resPrj != null) {
			    // add resPrj to array - it may have its own marker 
			    IResource[] rr = new IResource[resources.length + 1];
			    rr[0] = resPrj;
			    System.arraycopy(resources, 0, rr, 1, resources.length);
			    resources = rr;
			}
		}

		@Override
		public int findMaxProblemSeverity(String type, boolean includeSubtypes, int depth) throws CoreException {
			return ResourceUtils.findMaxProblemSeverity(resources, type, includeSubtypes, depth);
		}
	}

	private Image getImage(int problemSeverity) {
		Image image = null;
		if (problemSeverity == IMarker.SEVERITY_ERROR) {
			image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
		}
		else if (problemSeverity == IMarker.SEVERITY_WARNING) {
			image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_WARNING);
		}
		return image;
	}
}
