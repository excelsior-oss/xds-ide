package com.excelsior.xds.ui.search;

import org.eclipse.search.ui.text.FileTextSearchScope;

public class SearchWholeWordInWorkspaceAction extends SearchWholeWordAction 
{
	@Override
	public FileTextSearchScope getScope() {
		return FileTextSearchScope.newWorkspaceScope(new String[]{"*.*"}, false); //$NON-NLS-1$
	}
}