package com.excelsior.xds.ui.navigator.project;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;

import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.core.project.ProjectUtils;

public class XdsLinkHelper implements ILinkHelper {
	@Override
	public void activateEditor(IWorkbenchPage page,
			IStructuredSelection selection) {
		Object element= selection.getFirstElement();
		IEditorPart part= CoreEditorUtils.isOpenInEditor(element);
		if (part != null) {
			page.bringToTop(part);
		}
	}

	@Override
	public IStructuredSelection findSelection(IEditorInput editorInput) {
	    IFile element = (IFile) editorInput.getAdapter(IFile.class);
	    IStructuredSelection selection = StructuredSelection.EMPTY;
	    if (element != null && ProjectUtils.isBelongsToXdsProject(element)) {
	        IXdsElement xdsElement = XdsModelManager.getModel().getXdsElement(element);
	        if (xdsElement != null) {
	            selection = new StructuredSelection(xdsElement); 
	        }
	    }
		return selection;
	}
}