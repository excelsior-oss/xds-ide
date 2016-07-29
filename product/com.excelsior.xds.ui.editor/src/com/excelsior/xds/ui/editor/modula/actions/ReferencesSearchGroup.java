package com.excelsior.xds.ui.editor.modula.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.modula.IModulaEditorActionDefinitionIds;
import com.excelsior.xds.ui.editor.modula.ModulaEditor;

public class ReferencesSearchGroup extends ActionGroup {
	
	private ModulaEditor editor;
	
	private FindAction findReferencesAction;
	private FindAction findReferencesProjectAction;
	
	/**
	 * @param editor
	 */
	public ReferencesSearchGroup(ModulaEditor editor) {
		this.editor = editor;

		findReferencesAction = new FindReferencesProjectAction(editor);
		findReferencesAction.setActionDefinitionId(IModulaEditorActionDefinitionIds.FIND_REFS);
		if (editor != null){
			editor.setAction(IModulaEditorActionDefinitionIds.FIND_REFS, findReferencesAction);
		}
		findReferencesProjectAction = new FindReferencesProjectAction(editor);
	}
	/* 
	 * Method declared on ActionGroup.
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		
		IMenuManager incomingMenu = menu;
	
		IMenuManager declarationsMenu = new MenuManager(Messages.ReferencesSearchGroup_References, IContextMenuConstants.GROUP_SEARCH); 
		
		if (editor != null){
			menu.appendToGroup(ITextEditorActionConstants.GROUP_FIND, declarationsMenu);	
		} else {
			incomingMenu.appendToGroup(IContextMenuConstants.GROUP_SEARCH, declarationsMenu);
		}
		incomingMenu = declarationsMenu;
		
		incomingMenu.add(findReferencesAction);
		incomingMenu.add(findReferencesProjectAction);
	}	
	/* 
	 * Overrides method declared in ActionGroup
	 */
	@Override
	public void dispose() {
		super.dispose();
		findReferencesAction = null;
		findReferencesProjectAction = null;
	}
}
