package com.excelsior.xds.ui.editor.modula.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.ui.editor.modula.IModulaEditorActionDefinitionIds;
import com.excelsior.xds.ui.editor.modula.ModulaEditor;

public class OpenViewActionGroup extends ActionGroup
{
	private Action openDeclarationAction;

	public OpenViewActionGroup(ITextEditor part) {
		if (part instanceof ModulaEditor) {
			openDeclarationAction = new OpenDeclarationsAction((ModulaEditor) part);
			openDeclarationAction.setActionDefinitionId(IModulaEditorActionDefinitionIds.OPEN_DECL);
            part.setAction(OpenDeclarationsAction.ID, openDeclarationAction); //$NON-NLS-1$
		}
	}
	
	@Override
	public void fillActionBars(IActionBars actionBar) {
		super.fillActionBars(actionBar);
		setGlobalActionHandlers(actionBar);
	}

	private void setGlobalActionHandlers(IActionBars actionBars) {
		if (openDeclarationAction != null) {
        	actionBars.setGlobalActionHandler(OpenDeclarationsAction.ID, openDeclarationAction);
        }
	}
}
