package com.excelsior.xds.ui.search;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.TextSearchQueryProvider;
import org.eclipse.search.ui.text.TextSearchQueryProvider.TextSearchInput;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.core.utils.JavaUtils;
import com.excelsior.xds.ui.commons.utils.SelectionUtils;
import com.excelsior.xds.ui.commons.utils.WordAndRegion;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;

/**
 * Base class to search whole world either in workspace or project scope
 * 
 * @author lsa80
 */
public abstract class SearchWholeWordAction implements IWorkbenchWindowActionDelegate 
{
	private StyledText textWidget;

	@Override
    public void init(IWorkbenchWindow window) {
    }

    @Override
    public void dispose() {
    }

    @Override
	public void run(IAction action) {
    	final String pattern = getPattern();
    	if (pattern.isEmpty()) {
    		return;
    	}
        
        try {
        	ISearchQuery query = TextSearchQueryProvider.getPreferred().createQuery(new TextSearchInput() {
				
				@Override
				public boolean isRegExSearch() {
					return true;
				}
				
				@Override
				public boolean isCaseSensitiveSearch() {
					return true;
				}
				
				@Override
				public String getSearchText() {
					return pattern;
				}
				
				@Override
				public FileTextSearchScope getScope() {
					return SearchWholeWordAction.this.getScope(); //$NON-NLS-1$
				}
			});
        	NewSearchUI.runQueryInBackground(query);
		} catch (CoreException e) {
			e.printStackTrace();
		}
        
		return;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		IWorkbenchPart activePart = WorkbenchUtils.getActivePart();
		if (activePart instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) activePart;
			textWidget = JavaUtils.as(StyledText.class, textEditor.getAdapter(Control.class));
			action.setEnabled(true);
		}
		else {
			action.setEnabled(false);
		}
	}

	public abstract FileTextSearchScope getScope();
	
	
	private String getPattern() {
		String selectionText = textWidget != null? textWidget.getSelectionText() : StringUtils.EMPTY;
		if (selectionText.isEmpty()) {
			WordAndRegion findStringInfo = SelectionUtils.getWordUnderCursor(false);
			return findStringInfo != null? String.format("\\b%s\\b", findStringInfo.word) : StringUtils.EMPTY; //$NON-NLS-1$
		}
		else {
			return Pattern.quote(selectionText);
		}
	}
}
