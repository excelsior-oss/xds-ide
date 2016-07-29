package com.excelsior.xds.ui.editor.modula.actions;

import org.eclipse.core.resources.IResource;

import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.search.modula.ModulaSearchInput;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.modula.ModulaEditor;

public class FindDeclarationsAction extends FindAction {

	public FindDeclarationsAction(ModulaEditor editor) {
		super(editor, Messages.FindDeclarationsAction_Workspace, Messages.FindDeclarationsAction_SearchDeclsOfSelInWorkspace);
	}

	@Override
	protected int getLimitTo() {
		return ModulaSearchInput.LIMIT_TO_DECLARATIONS;
	}

	@Override
	protected IResource getSearchScope() {
		return ResourceUtils.getWorkspaceRoot();
	}
}