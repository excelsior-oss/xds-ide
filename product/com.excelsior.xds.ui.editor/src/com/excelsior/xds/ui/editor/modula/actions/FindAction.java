package com.excelsior.xds.ui.editor.modula.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.FileTextSearchScope;

import com.excelsior.xds.core.search.modula.ModulaSearchInput;
import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.ui.editor.modula.ModulaEditor;
import com.excelsior.xds.ui.editor.modula.utils.ModulaEditorSymbolUtils;
import com.excelsior.xds.ui.search.modula.ModulaSearchQuery;

public abstract class FindAction extends SelectionParseAction {

	protected FindAction(ModulaEditor editor, String text, String tooltipText) {
		super(editor);
		setText(text);
		setToolTipText(tooltipText);
	}

	@Override
	public void run() {
		ITextSelection textSelection = getSelectedStringFromEditor();
		if (textSelection != null) {
			ISourceViewer textViewer = (ISourceViewer)editor.getAdapter(ISourceViewer.class);
			
			PstLeafNode pstLeafNode = ModulaEditorSymbolUtils.getIdentifierPstLeafNode(editor, textSelection.getOffset());
		    if (pstLeafNode != null) {
		        IModulaSymbol symbol = ModulaEditorSymbolUtils.getModulaSymbol(
                    textViewer.getDocument(), pstLeafNode
                );
		        IResource searchScope = getSearchScope();
				if (symbol != null && searchScope != null) {
					NewSearchUI.activateSearchResultView();
			        NewSearchUI.runQueryInBackground(new ModulaSearchQuery(getInput(symbol, searchScope)));
				}
		    }
		}
	}

	private ModulaSearchInput getInput(IModulaSymbol symbol, IResource searchScope) {
		ModulaSearchInput m2SearchInput = new ModulaSearchInput(searchScope.getProject());
		m2SearchInput.setCaseSensitive(false);
		m2SearchInput.setLimitTo(getLimitTo());
		m2SearchInput.setSearchFor(ModulaSearchInput.SEARCH_FOR_ANY_ELEMENT);
		m2SearchInput.setSearchInFlags(ModulaSearchInput.SEARCH_IN_ALL_SOURCES);
		m2SearchInput.setSearchScope(FileTextSearchScope.newSearchScope(new IResource[]{searchScope}, new String[]{"*"}, false)); //$NON-NLS-1$
		m2SearchInput.setSymbolToSearchFor(symbol);
		m2SearchInput.setSearchString(symbol.getName());
		return m2SearchInput;
	}
	
	protected abstract int getLimitTo();
	protected abstract IResource getSearchScope();
}
