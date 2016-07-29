package com.excelsior.xds.ui.editor.commons.text;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.excelsior.xds.ui.commons.utils.UiUtils;

/**
 * A painter is responsible for creating, managing, updating, and removing
 * visual decorations of highlighting brackets pair.
 * 
 * @author fsa
 */
public class PairedBracketsPainter implements IPainter, PaintListener {

    /** Indicates whether this painter is active. */
    private boolean fIsActive = false;
    /** The source viewer this painter is attached to. */
    private ISourceViewer fSourceViewer;
    /** The viewer's widget. */
    private StyledText fTextWidget;

    private PairedBracketsMatcher fMatcher;

    private Color fColor;
    private Color fNoMatchColor;
    private Color fFontColor;
    
    private IPaintPositionManager fPaintPositionManager;
    private Position fPairPosition= new Position(0, 0);
    private int fAnchor;
    private int fMatchFlags;
    private int fFontStyle;
    private RGB fLastFontRgb;

    public static final RGB DEF_RGB_MATCHED   = new RGB(204, 230, 255);
    public static final RGB DEF_RGB_UNMATCHED = new RGB(255, 220, 220);

    public static void installToEditor( ISourceViewer isv
                                      , PairedBracketsMatcher matcher
                                      , IPreferenceStore store
                                      , String keyHighlightMatch 
                                      , String keyColor, String keyColorNoMatch ) 
    {
        if (isv instanceof ITextViewerExtension2) {
            new PainterOnOffManager(isv, matcher, store, keyHighlightMatch, keyColor, keyColorNoMatch);
        }
    }

    private static class PainterOnOffManager {
        private PairedBracketsPainter painter;
        private PairedBracketsMatcher matcher;
        private ISourceViewer isv;
        private IPreferenceStore store;
        private final String keyHighlightMatch;
        private final String keyColor, keyColorNoMatch;
        
        public PainterOnOffManager( ISourceViewer isv
                                  , PairedBracketsMatcher matcher
                                  , IPreferenceStore store
                                  , String keyHighlightMatch 
                                  , String keyColor, String keyColorNoMatch ) 
        {
            this.isv = isv;
            this.store = store;
            this.keyHighlightMatch = keyHighlightMatch;
            this.keyColor = keyColor;
            this.keyColorNoMatch = keyColorNoMatch;
            this.matcher = matcher; 
            store.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    reReadStore(event);
                }
            });
            reReadStore(null);
        }
        
        private void reReadStore(PropertyChangeEvent event) {
            String p = event!=null ? event.getProperty() : null;
            
            if (p != null && painter != null) {
                if (p.equals(keyColor))  {
                    painter.setRgb(getRgb(keyColor, DEF_RGB_MATCHED), true);
                } else if (p.equals(keyColorNoMatch))  {
                    painter.setNoMatchRgb(getRgb(keyColorNoMatch, DEF_RGB_UNMATCHED), true);
                }
            }
            
            if (p == null || p.equals(keyHighlightMatch))  {
                if (store.getBoolean(keyHighlightMatch)) {
                    showMatching();
                } else {
                    hideMatching();
                }
            }
        }
        
        private void showMatching() {
            if (painter == null) {
                painter= new PairedBracketsPainter(isv, matcher);
                painter.setRgb       (getRgb(keyColor,        DEF_RGB_MATCHED), false);
                painter.setNoMatchRgb(getRgb(keyColorNoMatch, DEF_RGB_UNMATCHED), false);
                ((ITextViewerExtension2)isv).addPainter(painter);
            }
        }
        
        private void hideMatching() {
            if (painter != null) {
                ((ITextViewerExtension2)isv).removePainter(painter);
                painter.deactivate(true);
                painter.dispose();
                painter= null;
            }
        }
        
        private RGB getRgb(String key, RGB defVal) {
            return store.contains(key) ? PreferenceConverter.getColor(store, key) : defVal;
        }
        
    }
    
    
    

    private PairedBracketsPainter( ISourceViewer sourceViewer 
                                 , PairedBracketsMatcher matcher ) 
    {
        fSourceViewer = sourceViewer;
        fMatcher      = matcher;
        fTextWidget   = sourceViewer.getTextWidget();
        fColor        = new Color(Display.getDefault(), DEF_RGB_MATCHED); // to don't NPE on problems with store
    }

    private void setRgb(RGB rgb, boolean redraw) {
    	UiUtils.dispose(fColor);
        fColor = new Color(Display.getDefault(), rgb);
        if (redraw) {
            handleDrawRequest(null);
        }
    }
    
    private void setNoMatchRgb(RGB rgb, boolean redraw) {
    	UiUtils.dispose(fNoMatchColor);
        fNoMatchColor = new Color(Display.getDefault(), rgb);
        if (redraw) {
            handleDrawRequest(null);
        }
    }

    /*
     * @see org.eclipse.jface.text.IPainter#dispose()
     */
    @Override
    public void dispose() {
        if (fMatcher != null) {
            fMatcher.clear();
            fMatcher= null;
        }

        UiUtils.dispose(fColor);
        UiUtils.dispose(fNoMatchColor);
        UiUtils.dispose(fFontColor);
        fTextWidget   = null;
        fSourceViewer = null;
    }

    /*
     * @see org.eclipse.jface.text.IPainter#deactivate(boolean)
     */
    @Override
    public void deactivate(boolean redraw) {
        if (fIsActive) {
            fIsActive= false;
            fTextWidget.removePaintListener(this);
            if (fPaintPositionManager != null)
                fPaintPositionManager.unmanagePosition(fPairPosition);
            if (redraw)
                handleDrawRequest(null);
        }
    }

    /*
     * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
     */
    @Override
    public void paintControl(PaintEvent event) {
        if (fTextWidget != null)
            handleDrawRequest(event.gc);
    }

    /**
     * Handles a redraw request.
     *
     * @param gc the GC to draw into.
     */
    private void handleDrawRequest(GC gc) {

        if (fPairPosition.isDeleted)
            return;

        int offset= fPairPosition.getOffset();
        int length= fPairPosition.getLength();
        if (length < 1)
            return;

        if (fSourceViewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension= (ITextViewerExtension5) fSourceViewer;
            IRegion widgetRange= extension.modelRange2WidgetRange(new Region(offset, length));
            if (widgetRange == null)
                return;

            try {
                // don't draw if the pair position is really hidden and widgetRange just
                // marks the coverage around it.
                IDocument doc= fSourceViewer.getDocument();
                int startLine= doc.getLineOfOffset(offset);
                int endLine= doc.getLineOfOffset(offset + length);
                if (extension.modelLine2WidgetLine(startLine) == -1 || extension.modelLine2WidgetLine(endLine) == -1)
                    return;
            } catch (BadLocationException e) {
                return;
            }

            offset= widgetRange.getOffset();
            length= widgetRange.getLength();

        } else {
            IRegion region= fSourceViewer.getVisibleRegion();
            if (region.getOffset() > offset || region.getOffset() + region.getLength() < offset + length)
                return;
            offset -= region.getOffset();
        }
        
        boolean digraph = ((fMatchFlags & PairedBracketsMatcher.MATCH_FLAG_DIGRAPH) != 0);
        int cx = digraph ? 1 : 0;

        if (ICharacterPairMatcher.RIGHT == fAnchor)
            draw(gc, offset + length -1 - cx, offset, cx);
        else
            draw(gc, offset, offset + length -1 - cx, cx);
    }

    /**
     * Highlights the given widget region.
     *
     * @param gc the GC to draw into
     * @param curOffset - offset of the bracket near cursor position
     * @param pairOffset - offset of the pair bracket
     */
    private void draw(GC gc, int curOffset, int pairOffset, int cx) {
        boolean isMatch = ((fMatchFlags & PairedBracketsMatcher.MATCH_FLAG_NO_MATCH) == 0);
        int len = fTextWidget.getCharCount()-1;
        if (len < 0) {
            return;
        }
        int co = curOffset = Math.min(curOffset, len);
        int po = pairOffset = isMatch ? Math.min(pairOffset, len) : -1;
        if (po >= 0 && po < co) {
            int tmp = co;
            co = po;
            po = tmp;
        }

        if (gc != null) {
            gc.setBackground(isMatch ? fColor : fNoMatchColor);
            gc.setForeground(fFontColor);

            Font fontOld = gc.getFont();
            Font fontNew = UiUtils.modifyFont(gc.getDevice(), fontOld, fFontStyle);
            gc.setFont(fontNew);
            
            for (int offs = co; offs >= 0; offs = po, po = -1) {
                int ll = Math.min(offs+cx, len);
                Rectangle curBounds = fTextWidget.getTextBounds(offs,  ll);

                // old frame-style:                
                // gc.drawRectangle(curBounds.x, curBounds.y, curBounds.width - 1, curBounds.height - 1);
                
                gc.fillRectangle(curBounds.x, curBounds.y, curBounds.width, curBounds.height);
                String txt = fTextWidget.getTextRange(offs, ll-offs+1);
                gc.drawText(txt, curBounds.x, curBounds.y, SWT.DRAW_TRANSPARENT);
                
                if ((fFontStyle & TextAttribute.STRIKETHROUGH) != 0) {
                    gc.drawLine(curBounds.x, curBounds.y + curBounds.height / 2, curBounds.x + curBounds.width - 1, curBounds.y + curBounds.height / 2);
                }
                
                if ((fFontStyle & TextAttribute.UNDERLINE) != 0) {
                    gc.drawLine(curBounds.x, curBounds.y + curBounds.height - 1, curBounds.x + curBounds.width - 1, curBounds.y + curBounds.height - 1);
                }

            }
            
            gc.setFont(fontOld);
            fontNew.dispose();
        } else {
            int x = Math.min(1+cx, len-co);
            if (x>0) {
                fTextWidget.redrawRange(co, x, true);
            }
            if (isMatch) {
                x = Math.min(1+cx, len-po);
                if (x > 0) {
                    fTextWidget.redrawRange(po, x, true);
                }
            }
        }
    }
      
    /*
     * @see org.eclipse.jface.text.IPainter#paint(int)
     */
    @Override
    public void paint(int reason) {

        IDocument document= fSourceViewer.getDocument();
        if (document == null) {
            deactivate(false);
            return;
        }

        Point selection= fSourceViewer.getSelectedRange();
        if (selection.y > 0) {
            deactivate(true);
            return;
        }

        IRegion pair= fMatcher.match(document, selection.x);
        if (pair == null) {
            deactivate(true);
            return;
        }

        if (fIsActive) {

            if (IPainter.CONFIGURATION == reason) {

                // redraw current highlighting
                handleDrawRequest(null);

            } else if (pair.getOffset() != fPairPosition.getOffset() ||
                    pair.getLength() != fPairPosition.getLength() ||
                    fMatcher.getAnchor() != fAnchor || fMatcher.getMatchFlags() != fMatchFlags) 
            {

                // otherwise only do something if position is different

                // remove old highlighting
                handleDrawRequest(null);
                // update position
                updatePos(pair);
                // apply new highlighting
                handleDrawRequest(null);

            }
        } else {
            fIsActive = true;
            updatePos(pair);
            fTextWidget.addPaintListener(this);
            fPaintPositionManager.managePosition(fPairPosition);
            handleDrawRequest(null);
        }
    }
    
    private void updatePos(IRegion pair) {
        fPairPosition.isDeleted= false;
        fPairPosition.offset= pair.getOffset();
        fPairPosition.length= pair.getLength();
        fAnchor = fMatcher.getAnchor();
        fFontStyle = fMatcher.getFontStyle();
        fMatchFlags = fMatcher.getMatchFlags();
        RGB rgb = fMatcher.getRGB();
        if (!rgb.equals(fLastFontRgb)) {
        	UiUtils.dispose(fFontColor);
            fFontColor = new Color(Display.getDefault(), rgb);
        }
    }

    /*
     * @see org.eclipse.jface.text.IPainter#setPositionManager(org.eclipse.jface.text.IPaintPositionManager)
     */
    @Override
    public void setPositionManager(IPaintPositionManager manager) {
        fPaintPositionManager= manager;
    }
}

