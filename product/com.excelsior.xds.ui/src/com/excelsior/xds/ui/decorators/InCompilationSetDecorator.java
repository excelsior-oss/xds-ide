package com.excelsior.xds.ui.decorators;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.excelsior.xds.core.compiler.compset.CompilationSetManager;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsExternalCompilationUnit;
import com.excelsior.xds.core.model.IXdsExternalCompilationUnitContainer;
import com.excelsior.xds.core.model.IXdsResource;
import com.excelsior.xds.core.model.IXdsSdkLibraryContainer;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.ui.images.ImageUtils;

public class InCompilationSetDecorator extends    LabelProvider 
                                       implements ILightweightLabelDecorator
                                                , IXdsDecorator 
{
    public static final String ID = "com.excelsior.xds.InCompilationSetDecorator"; //$NON-NLS-1$
	private ImageDescriptor overlayImageDesc;
    
    @Override
    public void dispose() {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void decorate(Object element, IDecoration decoration) {
    	if (element instanceof IXdsSdkLibraryContainer) {
    		IProject project = ((IXdsSdkLibraryContainer)element).getXdsProject().getProject();
    		decorateWithSdkName(project, decoration);
    	}
        else {
            decorateElement(element, decoration);
        }
    }

    private void decorateWithSdkName(IProject project, IDecoration decoration) {
        if (project != null) {
            Sdk sdk = XdsProjectSettingsManager.getXdsProjectSettings(project).getProjectSdk();
            if ((sdk != null) && StringUtils.isNotEmpty(sdk.getName())) {
                decoration.addSuffix(String.format("  [%s]", sdk.getName())); //$NON-NLS-1$
            }
        }
    }

    private void decorateElement(Object element, IDecoration decoration) {
    	IResource res = null;
    	IProject project = null;
        boolean isDecorate = false;
        if (element instanceof IXdsExternalCompilationUnitContainer || element instanceof IXdsExternalCompilationUnit) { // external dependencies are always inside compset
        	isDecorate = true;
        }
        else{
        	// get project and IResource for IXdsElement here ...
            if (element instanceof IXdsResource) {
            	res = ((IXdsResource)element).getResource();
            	if (res != null) {
            		project = res.getProject();
            	}
            	else{
            		project = ((IXdsElement)element).getXdsProject().getProject();
            	}
            }
            else if (element instanceof IResource) { // ... and for IResource - here
            	res = (IResource)element;
            	project = res.getProject();
        	}
            
            if (!(element instanceof IXdsElement) && // IXdsElement with resources were already decorated as IResource`s
            		!(element instanceof IProject) && // dont decorate projects
            		res != null && res.exists() && res.getProject() != null) 
            {
                String projectName = res.getProject().getName();
    			String absolutePath = ResourceUtils.getAbsolutePath(res);
    			boolean isInCompilationSet = CompilationSetManager.getInstance().isInCompilationSet(projectName, absolutePath);
                boolean isParentContainsCompilationSetChildren = CompilationSetManager.getInstance().isHasCompilationSetChildren(res.getProject(), absolutePath);
    			if (isInCompilationSet || isParentContainsCompilationSetChildren) {
    				isDecorate = true;
                }
            }
        }
        
        if (isDecorate) {
        	initializeOverlayImageDescriptor();
			decoration.addOverlay(overlayImageDesc);
            if (project != null) {
            	XdsProjectSettings projectSettings = XdsProjectSettingsManager.getXdsProjectSettings(project);
            	decoration.addSuffix("  [" + projectSettings.getCompilationRootName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

	private void initializeOverlayImageDescriptor() {
		if (overlayImageDesc == null) {
			Image image = ImageUtils.getImage(ImageUtils.IN_COMP_SET_OVERLAY_IMAGE_NAME);
			overlayImageDesc = ImageDescriptor.createFromImage(image);
		}
	}
    
    private void fireLabelEvent(final LabelProviderChangedEvent event) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                fireLabelProviderChanged(event);
            }
        });
    }

    @Override
    public void refresh(IResource[] changedElements) {
        fireLabelEvent(new LabelProviderChangedEvent(this, changedElements));
    }
    
}
