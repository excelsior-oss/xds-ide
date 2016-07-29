package com.excelsior.xds.ui.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.search.ui.text.FileTextSearchScope;

import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;

public class SearchWholeWordInProjectAction extends SearchWholeWordAction 
{
	@Override
	public FileTextSearchScope getScope() {
		IFile activeIFile = WorkbenchUtils.getActiveFile();
		if (activeIFile != null) {
			return FileTextSearchScope.newSearchScope(
					new IResource[] { activeIFile.getProject() },
					new String[] { "*.*" }, false); //$NON-NLS-1$
		}
		else{
			return FileTextSearchScope.newWorkspaceScope(new String[]{"*.*"}, false); //$NON-NLS-1$
		}
	}
}