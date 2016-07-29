package com.excelsior.xds.ui.editor.commons.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

import com.excelsior.xds.ui.editor.commons.PersistentTokenDescriptor;

/**
 * Helper class for bracket matching.
 * 
 * @author fsa
 */
public class PairedBracketsMatcher  implements ICharacterPairMatcher {

    public static final int MATCH_FLAG_DIGRAPH  = 0x0001; // 2-chars bracket (like "(*" )
    public static final int MATCH_FLAG_NO_MATCH = 0x0002; // bracket found, but no matched bracket found

    protected static final String BRACE_PAIRS = "()<>[]{}"; //$NON-NLS-1$
    protected static final PersistentTokenDescriptor defTokenWithColors = new PersistentTokenDescriptor("", "", new RGB(0, 0, 0), SWT.NONE, null);  //$NON-NLS-1$, //$NON-NLS-2$
    
    protected final String fPartitioning;
    protected final String fDefaultPartition;
    
    private ITypedRegion  fCachedPartition;
    private IDocument     fCachedListenedDocument;
    private IDocumentListener fDocumentListener;
    protected int fAnchor;
    protected int fMatchFlags;
    protected PersistentTokenDescriptor fTokenWithColors; // used to re-draw brackets with syntax coloring settings
    
    public PairedBracketsMatcher (String partitioning, String default_partition) {
        fPartitioning     = partitioning;
        fDefaultPartition = default_partition;
        fDocumentListener = new IDocumentListener() {

            @Override
            public void documentAboutToBeChanged(DocumentEvent event) {
                fCachedPartition = null;
            }

            @Override
            public void documentChanged(DocumentEvent event) {
            }
        };
    }

    /* @see ICharacterPairMatcher#match(IDocument, int) */
    @Override
    public IRegion match(IDocument document, int offset) {
        try {
            fTokenWithColors = defTokenWithColors;
            return performMatch(document, offset);
        } catch (BadLocationException ble) {
            return null;
        }
    }
    
    @Override
    /**
     *  Returns LEFT/RIGHT as in prototype or additional value NO_MATCH 
     */
    public int getAnchor() {
        return fAnchor;
    }
    
    public int getMatchFlags() {
        return fMatchFlags;
    }
    
    public RGB getRGB() {
        return fTokenWithColors.isDisabled() ? fTokenWithColors.getDefaultRgb() : fTokenWithColors.getRgbWhenEnabled();
    }

    public int getFontStyle() {
        return fTokenWithColors.isDisabled() ? fTokenWithColors.getDefaultStyle() : fTokenWithColors.getStyleWhenEnabled();
   }

    /* @see ICharacterPairMatcher#dispose() */
    @Override
    public void dispose() { 
        clear();
    }

    /* @see ICharacterPairMatcher#clear() */
    @Override
    public void clear() {
        if (fCachedListenedDocument != null) {
            fCachedListenedDocument.removeDocumentListener(fDocumentListener);
            fCachedListenedDocument = null;
        }
        fCachedPartition = null;
        fAnchor = -1;
    }

    
    
    protected ITypedRegion getPartition(IDocument document, int pos) {
        if (fCachedListenedDocument == null) {
            fCachedListenedDocument = document;
            fCachedListenedDocument.addDocumentListener(fDocumentListener);
        }
        if (fCachedPartition != null) {
            int offs = fCachedPartition.getOffset();
            if (offs <= pos && pos < offs + fCachedPartition.getLength()) {

//                /////---- dbg:
//                try {
//                    ITypedRegion pp = TextUtilities.getPartition(document, fPartitioning, pos, false);
//                    if (!pp.getType().equals(fCachedPartition.getType())) {
//                        System.out.println("****** Partition " + pp.getType() + 
//                                           "(" + pp.getOffset() + 
//                                           " / " + pp.getLength() + 
//                                           ") is cached as '" + fCachedPartition.getType() + 
//                                           "(" + fCachedPartition.getOffset() + 
//                                           " / " + fCachedPartition.getLength() + ")");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                /////----
                
                return fCachedPartition;
            }
        }
        try {
            fCachedPartition= TextUtilities.getPartition(document, fPartitioning, pos, false);
        } catch (BadLocationException e) {
            fCachedPartition= null;
        }
        return fCachedPartition;
    }

    /*
     * Performs the actual work of matching for #match(IDocument, int).
     */
    protected IRegion performMatch(IDocument document, int offs) throws BadLocationException {
        final int doclen = document.getLength();
        fMatchFlags = 0; 
        if (document == null || doclen < 1 || offs < 0 || offs > doclen) {
            return null;
        }
        char begCh = 0;
        int idx = -1;
        if (offs > 0) {
        // Try char on the left of caret:
            begCh = document.getChar(offs - 1);
            idx = BRACE_PAIRS.indexOf(begCh);
        }
        if (idx < 0) {
            // No bracket on the left. Try the caret position (move offs+=1 and try it):
            ++offs;
            if (offs <= doclen) {
                begCh = document.getChar(offs - 1);
                idx = BRACE_PAIRS.indexOf(begCh);
            }
        }

        if (idx < 0) {
            return null;
        }
        int pos = offs-1;
            
        // Bracket 'ch' found at [pos]
        int pos0 = pos;
        int step = ((idx & 0x1) == 0) ? 1 : -1; // open/close bracket
        
        ITypedRegion partition = getPartition(document, pos);

        final String partType = partition.getType(); 
        
        // Search direction depends on step. Search area is current partition if the partition type
        // is not 'CONTENT_TYPE_DEFAULT' or all document in other case
        int minPos = 0;
        int maxPos = doclen-1;
        if (!partType.equals(fDefaultPartition)) {
            minPos = Math.max(0,  partition.getOffset());
            maxPos = Math.min(maxPos, partition.getOffset() + partition.getLength() - 1);
        }
        char pairCh = BRACE_PAIRS.charAt(idx ^ 0x1);

        fAnchor = step > 0 ? LEFT : RIGHT;
        
        int  deep = 1;
        while(true) {
            pos += step;
            if (pos < minPos || pos>maxPos) {
                // unmatched
                fMatchFlags |= MATCH_FLAG_NO_MATCH; 
                return new Region(pos0, 1); 
            }
            if (getPartition(document, pos).getType().equals(partType)) {
                // skip other partitions (required in default partition)
                char c = document.getChar(pos);
                if (c == begCh) {
                    ++deep;
                } else if (c == pairCh) {
                    if (--deep == 0) {
                        break;
                    }
                }
            }
        } // while
        
        // While was breaked => found:
        if (step > 0) {
            fAnchor = LEFT;
            return new Region(pos0, pos-pos0+1);
        } else {
            fAnchor = RIGHT;
            return new Region(pos, pos0-pos+1);
        }
    }

}
