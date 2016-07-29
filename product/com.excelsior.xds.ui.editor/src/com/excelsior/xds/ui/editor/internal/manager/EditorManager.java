package com.excelsior.xds.ui.editor.internal.manager;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.model.IEditableXdsModel;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsNonWorkspaceCompilationUnit;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.parser.modula.XdsParserManager;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.editor.modula.utils.ModulaEditorSymbolUtils;


public class EditorManager {
	public void initialize() {
		IWorkbenchWindow window = WorkbenchUtils.getActiveWorkbenchWindow();
		IPartService partService = window.getPartService();
		partService.addPartListener(new ViewPartListener());
	}
	
	private class ViewPartListener implements IPartListener {
		@Override
		public void partOpened(IWorkbenchPart part) {
			IEditorPart editorPart = getEditorPart(part);
			if (editorPart != null) {
				IEditorInput editorInput = editorPart.getEditorInput();
				IResource resource = (IResource)editorInput.getAdapter(IResource.class);
				if (resource == null) {
					IFileStore fileStore = CoreEditorUtils.editorInputToFileStore(editorInput);
					if (fileStore != null) {
						IEditableXdsModel editableModel = XdsModelManager.getEditableModel();
						editableModel.createNonWorkspaceXdsElement(fileStore);
					}
				}
			}
		}
		
		@Override
		public void partClosed(IWorkbenchPart part) {
			IEditorPart editorPart = getEditorPart(part);
			if (editorPart != null) {
				IEditorInput editorInput = editorPart.getEditorInput();
				IResource resource = (IResource)editorInput.getAdapter(IResource.class);
				IFileStore fileStore = CoreEditorUtils.editorInputToFileStore(editorInput);
				if (resource == null) {
					if (fileStore != null) {
						IEditableXdsModel editableModel = XdsModelManager.getEditableModel();
						IXdsElement xdsElement = editableModel.getNonWorkspaceXdsElement(fileStore);
						if (xdsElement instanceof IXdsNonWorkspaceCompilationUnit) {
							IXdsNonWorkspaceCompilationUnit compilationUnit = (IXdsNonWorkspaceCompilationUnit) xdsElement;
							editableModel.removeNonWorkspaceXdsElement(compilationUnit);
						}
					}
				}
				ModulaAst modulaAst = ModulaEditorSymbolUtils.getModulaAst(editorInput);
				XdsParserManager.discardModulaAst(modulaAst);
			}
		}

		@Override
		public void partActivated(IWorkbenchPart part) {
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
		}
		
		IEditorPart getEditorPart(IWorkbenchPart part) {
			if (part instanceof IEditorPart) {
				return (IEditorPart) part;
			}
			return null;
		}
	}
	
	private static class EditorManagerHolder{
		static EditorManager INSTANCE = new EditorManager();
	}

	public static EditorManager getInstance(){
		return EditorManagerHolder.INSTANCE;
	}
}
