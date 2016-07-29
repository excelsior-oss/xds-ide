package com.excelsior.xds.ui.search.modula;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

import com.excelsior.xds.core.search.modula.ModulaSearchInput;
import com.excelsior.xds.core.search.modula.ModulaSearchOperation;
import com.excelsior.xds.core.search.modula.ModulaSearchOperation.ISearchResultCollector;
import com.excelsior.xds.ui.XdsPlugin;
import com.excelsior.xds.ui.internal.nls.Messages;

public class ModulaSearchQuery implements ISearchQuery 
{
	private ModulaSearchResult fSearchResult;

	private ModulaSearchInput fSearchInput;

	public ModulaSearchQuery(ModulaSearchInput input) {
		fSearchInput = input;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.ui.ISearchQuery#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor) {
		final AbstractTextSearchResult result = (AbstractTextSearchResult) getSearchResult();
		result.removeAll();
		ISearchResultCollector collector = new ISearchResultCollector() {
			public void accept(Object match) {
				if (match instanceof Match) {
					result.addMatch((Match)match); // match.getElement() is IFile
				}
			}
		};
		ModulaSearchOperation op = new ModulaSearchOperation(fSearchInput, collector);
		MultiStatus status = new MultiStatus(XdsPlugin.PLUGIN_ID, IStatus.OK, Messages.M2SearchQuery_SearchProblems, null);
		op.execute(monitor, status);
		monitor.done();
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.ui.ISearchQuery#getLabel()
	 */
	public String getLabel() {
		return fSearchInput.getSearchString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.ui.ISearchQuery#canRerun()
	 */
	public boolean canRerun() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.ui.ISearchQuery#canRunInBackground()
	 */
	public boolean canRunInBackground() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.ui.ISearchQuery#getSearchResult()
	 */
	public ISearchResult getSearchResult() {
		if (fSearchResult == null)
			fSearchResult = new ModulaSearchResult(this);
		return fSearchResult;
	}
	
	public ModulaSearchInput getModulaSearchInput() {
	    return fSearchInput;
	}

}
