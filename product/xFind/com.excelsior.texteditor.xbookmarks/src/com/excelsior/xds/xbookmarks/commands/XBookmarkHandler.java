package com.excelsior.xds.xbookmarks.commands;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.xbookmarks.StatusLine;
import com.excelsior.xds.xbookmarks.XBookmarksUtils;
import com.excelsior.xds.xbookmarks.XBookmarksPlugin;
import com.excelsior.xds.xbookmarks.internal.nls.Messages;

/**
 * Abstract command handler to process bookmark's commands. 
 */
public abstract class XBookmarkHandler extends AbstractHandler {
    
    protected StatusLine statusLine = new StatusLine();
    
    
    /**
     * Toggle to the xBookmark with the given number on the current line.
     * 
     * @param markerNumber  the number of the mark to toggle
     */
    protected void toggleBookmark (int markerNumber) {
        ITextSelection selection = WorkbenchUtils.getActiveTextSelection();
        if (selection == null) {
            return;
        }
        IDocument document = WorkbenchUtils.getActiveDocument();
        if (document == null) {
            return;
        }
        IResource scope = XBookmarksUtils.getWorkspaceScope();
        if (scope == null) {
            return;
        }
        
        IFile file = WorkbenchUtils.getActiveFile();
        if (file == null) {
            // this often indicates a 'read only' editor (beeing essentially a
            // view), e.g. when opening a class file with associated source or
            // looking at associated source of a binary plugin. Although setting
            // a xBookmark here also would make sense this seems to be impossible.
            statusLine.showErrMessage(MessageFormat.format(Messages.BookmarkAction_CantSetBM_NoProjectDocument, markerNumber));
            return;
        }


        int charStart = selection.getOffset();
        int lineStart = selection.getStartLine() + 1;
        IRegion preferenceSelection = new Region(charStart, 0);
        IMarker oldMarker = XBookmarksUtils.fetchBookmark(scope, markerNumber);

        if (oldMarker != null) {
            try {
                Integer markerStartLine = (Integer) oldMarker.getAttribute(IMarker.LINE_NUMBER);

                oldMarker.delete();
                
                if (markerStartLine != null && markerStartLine.intValue() == lineStart) {
                    // It was on the current line - just delete it
                    statusLine.showMessage(
                            MessageFormat.format(Messages.BookmarkAction_DeletedBM, markerNumber),
                            XBookmarksPlugin.IMGID_DELETED);
                    XBookmarksState xBbookmarkStateService = (XBookmarksState)WorkbenchUtils.getSourceProvider(XBookmarksState.EXIST_STATE);
                    if (xBbookmarkStateService != null) {
                    	xBbookmarkStateService.fireBookmarkRemoved(1);
                    }
                    return;
                }

            }
            catch (CoreException e) {
                XBookmarksUtils.logError(e);
                statusLine.showErrMessage(MessageFormat.format(Messages.BookmarkAction_CantDeleteBM, markerNumber));
                return;
            }
        }

        // set the location according to preference settings
        String markerName = mkBookmarkNamePrefix(markerNumber);
        String docText = getLabelProposal(document, charStart);
        if (!docText.isEmpty()) {
            markerName += ": " + docText; //$NON-NLS-1$
        }

        Map<String, Integer> attributes = new HashMap<String, Integer>();
        attributes.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_INFO));
        attributes.put(XBookmarksPlugin.BOOKMARK_NUMBER_ATTR, markerNumber);
        MarkerUtilities.setLineNumber(attributes, lineStart);
        MarkerUtilities.setCharStart(attributes, preferenceSelection.getOffset());
        MarkerUtilities.setCharEnd(attributes, preferenceSelection.getOffset() + preferenceSelection.getLength());
        MarkerUtilities.setMessage(attributes, markerName);

        try {
            MarkerUtilities.createMarker(file, attributes, XBookmarksPlugin.BOOKMARK_MARKER_ID);
            if (oldMarker != null) {
                statusLine.showMessage(
                        MessageFormat.format(Messages.BookmarkAction_MovedBM, markerNumber),
                        XBookmarksPlugin.IMGID_SET);
            } else {
                statusLine.showMessage(
                        MessageFormat.format(Messages.BookmarkAction_SetBM, markerNumber),
                        XBookmarksPlugin.IMGID_SET);
            }
            XBookmarksState xBbookmarkStateService = (XBookmarksState)WorkbenchUtils.getSourceProvider(XBookmarksState.EXIST_STATE);
            if (xBbookmarkStateService != null) {
            	xBbookmarkStateService.fireBookmarkSet();
            }
        }
        catch (CoreException e) {
            XBookmarksUtils.logError(e);
            statusLine.showErrMessage(MessageFormat.format(Messages.BookmarkAction_CantSetBM, markerNumber));
        }
    }

    
    /**
     * Jumps to the xBookmark with the given number, if there is one.
     * When necessary an editor will be opened.
     * If there are multiple marks with the same number then the previous/next
     * one from the current selection will be jumped to (when exactly on
     * a xBookmark that mark isn't considered as a jump target).
     * If after any xBookmark with that number, the search toggles around
     * to the first one and vice versa.
     * 
     * @param markerNumber  the number of the mark to jump to
     * @param backward  true, if the previous mark should be jumped to instead
     *                  of the next one
     */
    protected void gotoBookmark (int markerNumber) {
        IResource scope = XBookmarksUtils.getWorkspaceScope();
        if (scope == null) {
            return;
        }
        IMarker marker = XBookmarksUtils.fetchBookmark(scope, markerNumber);
        if (marker == null) {
            statusLine.showErrMessage(MessageFormat.format(Messages.BookmarkAction_BM_NotFound, markerNumber));
            return;
        }
        IResource resource = marker.getResource();
        if (! (resource instanceof IFile)) {
            statusLine.showErrMessage(MessageFormat.format(Messages.BookmarkAction_BM_HbzWhere, markerNumber));
            return;
        }
        IWorkbenchPage page = WorkbenchUtils.getActivePage();
        if (page == null) {
            statusLine.showErrMessage(MessageFormat.format(Messages.BookmarkAction_CantGoBM_NoActivePage, markerNumber));
            return;
        }
        // now try to jump and open the right editor, if necessary
        try {
            IDE.openEditor(page, marker, OpenStrategy.activateOnOpen());
            statusLine.showMessage(
                    MessageFormat.format(Messages.BookmarkAction_OnBM, markerNumber),
                    XBookmarksPlugin.IMGID_GOTO);
        }
        catch (PartInitException e) {
            XBookmarksUtils.logError(e);
            statusLine.showErrMessage(MessageFormat.format(Messages.BookmarkAction_CantGoBM, markerNumber));
        }
    }
    

    protected String mkBookmarkNamePrefix(int bmNum) {
        return MessageFormat.format(Messages.BookmarkAction_BM, bmNum);
    }

    protected String getLabelProposal(IDocument document, int offset) {
        String txt = ""; //$NON-NLS-1$
        try {
            int line = document.getLineOfOffset(offset);
            int beg  = document.getLineOffset(line);
            txt = document.get(beg, document.getLineLength(line)).trim();
        } catch (Exception e) {}
        return txt;
    }
    
}
