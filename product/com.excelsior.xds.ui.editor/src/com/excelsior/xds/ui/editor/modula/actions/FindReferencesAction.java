package com.excelsior.xds.ui.editor.modula.actions;

import org.eclipse.core.resources.IResource;

import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.search.modula.ModulaSearchInput;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.modula.ModulaEditor;

public class FindReferencesAction extends FindAction {

	public FindReferencesAction(ModulaEditor editor) {
		super(editor, Messages.FindReferencesAction_Workspace, Messages.FindReferencesAction_SearchRefsToSelInWorkspace);
	}

	@Override
	protected int getLimitTo() {
		return ModulaSearchInput.LIMIT_TO_USAGES;
	}

	@Override
	protected IResource getSearchScope() {
		return ResourceUtils.getWorkspaceRoot();
	}
}