package com.excelsior.xds.ui.editor.modula.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.modula.IModulaEditorActionDefinitionIds;
import com.excelsior.xds.ui.editor.modula.ModulaEditor;

public class DeclarationsSearchGroup extends ActionGroup {
	
	private ModulaEditor editor;
	
	private FindAction findDeclarationsAction;
	private FindAction findDeclarationsProjectAction;
	
	/**
	 * @param editor
	 */
	public DeclarationsSearchGroup(ModulaEditor editor) {
		this.editor = editor;

		findDeclarationsAction= new FindDeclarationsProjectAction(editor);
		findDeclarationsAction.setActionDefinitionId(IModulaEditorActionDefinitionIds.FIND_DECL);
		if (editor != null){
			editor.setAction(IModulaEditorActionDefinitionIds.FIND_DECL, findDeclarationsAction);
		}
		findDeclarationsProjectAction = new FindDeclarationsProjectAction(editor);
	}
	/* 
	 * Method declared on ActionGroup.
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		
		IMenuManager incomingMenu = menu;
	
		IMenuManager declarationsMenu = new MenuManager(Messages.DeclarationsSearchGroup_Declarations, IContextMenuConstants.GROUP_SEARCH); 
		
		if (editor != null){
			menu.appendToGroup(ITextEditorActionConstants.GROUP_FIND, declarationsMenu);	
		} else {
			incomingMenu.appendToGroup(IContextMenuConstants.GROUP_SEARCH, declarationsMenu);
		}
		incomingMenu = declarationsMenu;
		
		incomingMenu.add(findDeclarationsAction);
		incomingMenu.add(findDeclarationsProjectAction);
	}	
	/* 
	 * Overrides method declared in ActionGroup
	 */
	@Override
	public void dispose() {
		super.dispose();
		findDeclarationsAction = null;
		findDeclarationsProjectAction = null;
	}
}
