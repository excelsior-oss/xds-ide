package com.excelsior.xds.ui.search.modula;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.ISearchEditorAccess;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.ui.internal.nls.Messages;

public class ModulaSearchResult extends    AbstractTextSearchResult 
                                implements IEditorMatchAdapter 
{
    protected ModulaSearchQuery fMQuery;

    public ModulaSearchResult(ModulaSearchQuery query) {
        fMQuery = query;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.search.ui.text.AbstractTextSearchResult#getEditorMatchAdapter
     * ()
     */
    public IEditorMatchAdapter getEditorMatchAdapter() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.search.ui.ISearchResult#getLabel()
     */
    public String getLabel() {
        String desc = fMQuery.getModulaSearchInput().getSearchDescription(); // Declarations of variables 'abc*' in project 'zz.prj'"
        return String.format(Messages.ModulaSearchResult_MatchesFmt, desc, getMatchCount());
        
//        int numMatches = getMatchCount();
//        // Last digit - issues with russian localization:
//        // 1 совпадение (match)
//        // 2 3 4 совпадения (matches)
//        // 5 6 7 8 9 0 совпадений (matches)
//        String mtchs = Messages.M2SearchResult_matches_sovpadeniy; // matches | совпадений
//        switch(numMatches % 10) {
//        case 1:
//            mtchs = Messages.M2SearchResult_match_sovpadenie; // match | совпадение
//            break;
//        case 2:
//        case 3:
//        case 4:
//            mtchs = Messages.M2SearchResult_matches_sovpadeniya; // matches | совпадения
//        }
//        if (Messages.M2SearchResult_EN_or_RU.equals("EN") && numMatches > 1) { //$NON-NLS-1$
//            mtchs = Messages.M2SearchResult_matches_sovpadeniy; // matches (EN only)
//        }
//        
//        return fMQuery.getLabel() + " - " + numMatches + " " + mtchs;  //$NON-NLS-1$ //$NON-NLS-2$
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.search.ui.ISearchResult#getTooltip()
     */
    public String getTooltip() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.search.ui.ISearchResult#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return null; // PDEPluginImages.DESC_PSEARCH_OBJ;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.search.ui.ISearchResult#getQuery()
     */
    public ISearchQuery getQuery() {
        return fMQuery;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.search.ui.text.IEditorMatchAdapter#isShownInEditor(org.eclipse
     * .search.ui.text.Match, org.eclipse.ui.IEditorPart)
     */
    public boolean isShownInEditor(Match match, IEditorPart editor) {
        Object element = match.getElement();
        if (element instanceof IFile)
            return isMatchContained(editor, (IFile) element);
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.search.ui.text.IEditorMatchAdapter#computeContainedMatches
     * (org.eclipse.search.ui.text.AbstractTextSearchResult,
     * org.eclipse.ui.IEditorPart)
     */
    public Match[] computeContainedMatches( AbstractTextSearchResult result
                                          , IEditorPart editor ) 
    {
        ArrayList<Match> list = new ArrayList<Match>();
        Object[] objects = result.getElements();
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof IFile) {
                IFile f = (IFile) objects[i];
                if (isMatchContained(editor, f)) {
                    Match[] matches = getMatches(f);
                    for (int j = 0; j < matches.length; j++) {
                        list.add(matches[j]);
                    }
                }
            }
        }
        return (Match[]) list.toArray(new Match[list.size()]);
    }

    
    @Override
    public Match[] getMatches(Object element) {
        if (element instanceof Match) {
            return new Match[] {(Match)element};
        } 
        return super.getMatches(element);
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.search.ui.text.AbstractTextSearchResult#getFileMatchAdapter()
     */
    public IFileMatchAdapter getFileMatchAdapter() {
        return null;
    }

    protected boolean isMatchContained(IEditorPart editor, IFile f) {
        IFile resource = (IFile) editor.getEditorInput().getAdapter(IFile.class);
        if (resource != null) {
            return resource.equals(f);
        }
        return false;
    }

    protected IDocument getDocument(IEditorPart editor, Match match) {
        IDocument document = null;
        if (editor instanceof ISearchEditorAccess) {
            document = ((ISearchEditorAccess) editor).getDocument(match);
        } else if (editor instanceof ITextEditor) {
            document = ((ITextEditor) editor).getDocumentProvider()
                    .getDocument(editor.getEditorInput());
        }
        return document;
    }

}
