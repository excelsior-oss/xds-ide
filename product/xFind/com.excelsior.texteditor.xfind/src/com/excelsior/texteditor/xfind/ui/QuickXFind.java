package com.excelsior.texteditor.xfind.ui;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.texteditor.xfind.internal.ImageUtils;
import com.excelsior.texteditor.xfind.internal.nls.Messages;

public abstract class QuickXFind 
{
    /** Whitespace and Punctuation except '_' and '@' symbol */
    private static final String WORD_DELIMETERS = "[\\s\\p{Punct}&&[^_@]]";  //$NON-NLS-1$
    
    public static void findPrevious(IEditorPart editorPart) {
        IFindReplaceTarget target = (IFindReplaceTarget) editorPart.getAdapter(IFindReplaceTarget.class);
        if (target == null) 
            return;

        StatusLine statusLine = new StatusLine(editorPart.getEditorSite());
        statusLine.cleanStatusLine();

        SearchRegion region = getSearchRegion(target, editorPart);
        if (region == null) 
            return;
        
        int offset = Math.max(region.offset - 1, 0);
        
        offset = findAndSelect(target, offset, region.text, false);
        
        if (offset < 0) {
            String message = String.format(Messages.QuickFind_Status_FirstOccurence, region.text);
            statusLine.showMessage(message,  ImageUtils.getImage(ImageUtils.FIND_STATUS));
        }
    }
    
    public static void findNext(IEditorPart editorPart) {
        IFindReplaceTarget target = (IFindReplaceTarget) editorPart.getAdapter(IFindReplaceTarget.class);
        if (target == null) 
            return;
        
        StatusLine statusLine = new StatusLine(editorPart.getEditorSite());
        statusLine.cleanStatusLine();

        SearchRegion region = getSearchRegion(target, editorPart);
        if (region == null) 
            return;
        
        int offset = region.offset + region.length;

        offset = findAndSelect(target, offset, region.text, true);

        if (offset < 0) {
            String message = String.format(Messages.QuickFind_Status_LastOccurence, region.text);
            statusLine.showMessage(message, ImageUtils.getImage(ImageUtils.FIND_STATUS));
        }
    }

    
    /**
     * Searches for a string starting at the given offset and using the specified search
     * directives. If a string has been found it is selected and its start offset is
     * returned.
     *
     * @param target  the target for finding string
     * @param offset the offset at which searching starts
     * @param findString the string which should be found
     * @param forwardSearch the direction of the search

     * @return the position of the specified string, or -1 if the string has not been found
     */
    private static int findAndSelect( IFindReplaceTarget target
                                    , int offset, String findString
                                    , boolean forwardSearch ) 
    {
        boolean caseSensitive = true; 
        boolean wholeWord     = XFindUtils.isWord(findString); 
        boolean regExSearch   = false; 

        try {
            if (target instanceof IFindReplaceTargetExtension3) {
                return ((IFindReplaceTargetExtension3)target).findAndSelect(
                    offset, findString, forwardSearch, caseSensitive, wholeWord, regExSearch
                );
            }
            return target.findAndSelect(
                offset, findString, forwardSearch, caseSensitive, wholeWord
            );
        } catch (Exception e){
            return -1;
        }
    }


    private static SearchRegion getSearchRegion(IFindReplaceTarget target, IEditorPart editorPart) 
    {
        SearchRegion searchRegion = null;
        
        String text = target.getSelectionText();
        Point range = target.getSelection();
        if (range.y > 0) {
            searchRegion = new SearchRegion(text, range.x, range.y);
        }
        else if (editorPart instanceof ITextEditor) {
            ISelection selection = ((ITextEditor) editorPart).getSelectionProvider().getSelection();
            if (selection instanceof ITextSelection) {
                int offset = ((ITextSelection)selection).getOffset();
                IDocumentProvider provider = ((ITextEditor) editorPart).getDocumentProvider();
                IEditorInput input = editorPart.getEditorInput();
                if ((provider != null) && (input != null)) {
                    IDocument document = provider.getDocument(input);
                    if (document != null) {
                        searchRegion = getSearchRegion(document, offset);
                        if (searchRegion != null) {
//                            ITextSelection textSelection = new TextSelection(
//                                document, searchRegion.offset, searchRegion.length
//                            );
//                            ((ITextEditor) editorPart).getSelectionProvider().setSelection(textSelection);
                            searchRegion = new SearchRegion(
                                searchRegion.text, 
                                searchRegion.offset + (range.x - offset), 
                                searchRegion.length  
                            );
                        }
                    }
                }
            }
        }
        
        return searchRegion;
    }

    private static SearchRegion getSearchRegion(IDocument document, int offset) 
    {
        SearchRegion searchRegion = null;

        if ((offset >= 0) && (offset <= document.getLength())) {
            int start = offset;
            int end   = offset;
            try {
                boolean isCursorOnDelimiter = (offset == document.getLength())
                                           || document.get(offset, 1).matches(WORD_DELIMETERS);

                if (isCursorOnDelimiter) {
                    start--;
                }
                while ((start >= 0) && !document.get(start, 1).matches(WORD_DELIMETERS)) {
                    start--;
                }
                if (start !=  offset) {
                    start++;
                }
                
                while ((end < document.getLength()) && !document.get(end, 1).matches(WORD_DELIMETERS)) {
                    end++;
                }

                if (start != end) {
                    int length = end - start;
                    searchRegion = new SearchRegion(
                        document.get(start, length), start, length 
                    );
                }
            } catch (BadLocationException e) {
            }
            
        }
        return searchRegion;
    }
    
    
    /** The string being searched for */
    private static class SearchRegion {
        final String text;
        final int offset;
        final int length;
        
        SearchRegion(String text, int offset, int length) {
            this.text   = text;
            this.offset = offset;
            this.length = length;
        }
    }
    
}
