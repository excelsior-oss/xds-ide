package com.excelsior.xds.ui.internal.adapters;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * Supplies adapter for the various platform objects
 * @author lsa80
 */
public class XdsPlatformAdapterFactory implements IAdapterFactory{
	private static Class<?>[] ADAPTER_LIST= new Class[] {
        IProject.class,
    };

	@Override
	public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
		if (adaptableObject instanceof IEditorInput) {
			IEditorInput editorInput = (IEditorInput) adaptableObject;
			if (IProject.class.equals(adapterType)) {
				IResource resource = (IResource) editorInput.getAdapter(IResource.class);
				if (resource != null){
					return resource.getProject();
				}
			}
		}
		else if (adaptableObject instanceof IEditorPart) {
			IEditorPart editorPart = (IEditorPart)adaptableObject;
			if (IEditorInput.class.equals(adapterType)) {
				return editorPart.getEditorInput();
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class[] getAdapterList() {
		return ADAPTER_LIST;
	}
}
