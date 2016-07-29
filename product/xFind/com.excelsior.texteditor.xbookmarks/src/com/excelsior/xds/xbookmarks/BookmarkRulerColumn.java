package com.excelsior.xds.xbookmarks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.ImageUtilities;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TypedListener;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.excelsior.xds.ui.commons.swt.resources.ColorRegistry;

// See org.eclipse.jface.text.source.LineNumberRulerColumn realization

@SuppressWarnings("restriction")
public class BookmarkRulerColumn implements IVerticalRulerColumn {
	
	private static final RGB RGB_YELLOW = new RGB(0xff, 0xf7, 0xaa);
	private Color yellowColor;
	/** The font of this column */
    private Font fFont;
    /** The foreground color */
    private Color fForeground;
    
    private static final int ICON_WIDTH = 12;
    private static final int ICON_HEIGHT = 16;
    
    private final ColorRegistry colorRegistry = new ColorRegistry();

    
    private IAnnotationModel model;
    private IAnnotationModelListener modelListener;
    private HashMap<Integer, ArrayList<Integer>> mapMarks; // <line, <bm2, bm4..>>
    private int fMaxBookmarksInLine; 
    private MouseHandler fMouseHandler;
    private boolean isTextMode;
    private boolean isHideNumbers;
    private int txtWidth;
    private IPreferenceStore editPrefStore;
    private IPropertyChangeListener propListener;

    private static final boolean IS_MAC= Util.isMac();

    /** This column's parent ruler */
    private CompositeRuler fParentRuler;
    /** Cached text viewer */
    private ITextViewer fCachedTextViewer;
    /** Cached text widget */
    private StyledText fCachedTextWidget;
    /** The columns canvas */
    private Canvas fCanvas;
    /** Cache for the actual scroll position in pixels */
    private int fScrollPos;
    /** The drawable for double buffering */
    private Image fBuffer;
    /** The internal listener */
    private InternalListener fInternalListener = new InternalListener();
    
    /** Flag indicating whether a relayout is required */
    private boolean fRelayoutRequired = false;
    /** Redraw runnable lock */
    private Object fRunnableLock= new Object();
    /** Redraw runnable state */
    private boolean fIsRunnablePosted= false;
    /** Redraw runnable */
    private Runnable fRunnable= new Runnable() {
        public void run() {
            synchronized (fRunnableLock) {
                fIsRunnablePosted= false;
            }
            redraw();
        }
    };



    /**
     * Constructs a new vertical ruler column.
     */
    public BookmarkRulerColumn() {
        modelListener = new IAnnotationModelListener () {
            public void modelChanged(IAnnotationModel model) {
                adjustModel(model);
            }
        };
        XBookmarksPlugin.getDefault().getPreferenceStore()
    	.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
    			if (event.getProperty() == XPreferencePage.TEXT_MODE_KEY) {
    				setTextMode(XPreferencePage.getTextMode());
    			}
                if (event.getProperty() == XPreferencePage.HIDE_NUMBERS_KEY) {
                    setHideNumbers(XPreferencePage.getHideNumbers());
                }
			}
    	});
        isTextMode = XPreferencePage.getTextMode();
        isHideNumbers = XPreferencePage.getHideNumbers();
        yellowColor = colorRegistry.createColor(RGB_YELLOW);
        
        propListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
                if (editPrefStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)) {
                    fCanvas.setBackground(null);
                    postRedraw();
                } else if (editPrefStore.contains(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND)) {
		        	RGB rgb = PreferenceConverter.getColor(editPrefStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
		        	Color clr = new Color(Display.getDefault(), rgb);
		        	if (!clr.equals(fCanvas.getBackground())) {
		        		fCanvas.setBackground(clr);
		                postRedraw();
		        	}
		        }
			}};
        editPrefStore = EditorsPlugin.getDefault().getPreferenceStore();
        if (editPrefStore != null) {
        	editPrefStore.addPropertyChangeListener(propListener);
        }
    }
    
    public void aboutToDispose() {
        if (editPrefStore != null) {
        	editPrefStore.removePropertyChangeListener(propListener);
        }
        colorRegistry.dispose();
    }
    
    private void setTextMode(boolean b) {
    	isTextMode = b;
    	layout(true); 
    }

    private void setHideNumbers(boolean b) {
        isHideNumbers = b;
        layout(true); 
    }

    
    @SuppressWarnings("rawtypes")
    private void adjustModel(final IAnnotationModel model) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                int newBookmarksInLine = 0;
                
                HashMap<Integer, ArrayList<Integer>> mapMarksNew = new HashMap<Integer, ArrayList<Integer>>(); // <line, <bm2, bm4..>>

                if (model != null) {
                    Iterator it = model.getAnnotationIterator();
                    while (it.hasNext()) {
                        Object o = it.next();
                        if (o instanceof MarkerAnnotation) {
                            MarkerAnnotation ma = (MarkerAnnotation)o;
                            IMarker marker = ma.getMarker();
                            if (!marker.exists()) {
                                continue;
                            }
                            if (!XBookmarksPlugin.BOOKMARK_MARKER_ID.equals(MarkerUtilities.getMarkerType(marker))) {
                                continue;
                            }
                            int markerNumber = marker.getAttribute(XBookmarksPlugin.BOOKMARK_NUMBER_ATTR, -1);
                            if (markerNumber < 0) {
                                try {
                                    marker.delete();
                                } catch (CoreException e) {}
                                continue;
                            }
                            
                            int line = -1;
                            try {
                                //line = fCachedTextWidget.getLineAtOffset(model.getPosition(ma).getOffset());
                                line = fCachedTextViewer.getDocument().getLineOfOffset(model.getPosition(ma).getOffset());
                            } catch (Exception e) {
                                continue;
                            }
                            ArrayList<Integer> al = mapMarksNew.get(line); 
                            if (al == null) {
                                al = new ArrayList<Integer>();
                                mapMarksNew.put(line, al);
                            }
                            al.add(markerNumber);
                            Collections.sort(al);
                            if (al.size() > newBookmarksInLine) {
                                newBookmarksInLine = al.size();
                            }
                        }
                    }
                }
                
                if (fMaxBookmarksInLine != newBookmarksInLine) {
                    fMaxBookmarksInLine = newBookmarksInLine;
                    layout(false);
                }
                
                if (!mapMarksNew.equals(mapMarks)) {
                    mapMarks = mapMarksNew;
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            redraw();
                        }
                    });
                }
            }
            
        });
    }
    
    
    /**
     * Sets the foreground color of this column.
     *
     * @param foreground the foreground color
     */
    public void setForeground(Color foreground) {
        fForeground= foreground;
    }

    /*
     * @see IVerticalRulerColumn#getControl()
     */
    @Override
    public Control getControl() {
        return fCanvas;
    }

    /*
     * @see IVerticalRuleColumnr#getWidth
     */
    @Override
    public int getWidth() {
        if (isHideNumbers) {
            return 0; // hide column
        }
        return fMaxBookmarksInLine > 0 ? fMaxBookmarksInLine * (isTextMode ? txtWidth : ICON_WIDTH) + 2 : 0;
    }


    /*
     * @see IVerticalRulerColumn#createControl(CompositeRuler, Composite)
     */
    public Control createControl(CompositeRuler parentRuler, Composite parentControl) {

        fParentRuler= parentRuler;
        fCachedTextViewer= parentRuler.getTextViewer();
        fCachedTextWidget= fCachedTextViewer.getTextWidget();

        fCanvas= new Canvas(parentControl, SWT.NO_FOCUS ) {
            /*
             * @see org.eclipse.swt.widgets.Control#addMouseListener(org.eclipse.swt.events.MouseListener)
             * @since 3.4
             */
            public void addMouseListener(MouseListener listener) {
                // see bug 40889, bug 230073 and AnnotationRulerColumn#isPropagatingMouseListener()
                if (listener == fMouseHandler)
                    super.addMouseListener(listener);
                else {
                    TypedListener typedListener= null;
                    if (listener != null)
                        typedListener= new TypedListener(listener);
                    addListener(SWT.MouseDoubleClick, typedListener);
                }
            }
        };
        fCanvas.setBackground(fCachedTextWidget.getBackground());
        fCanvas.setForeground(fForeground);

        fCanvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent event) {
                if (fCachedTextViewer != null)
                    doubleBufferPaint(event.gc);
            }
        });

        fCanvas.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (fCachedTextViewer != null) {
                    fCachedTextViewer.removeViewportListener(fInternalListener);
                    fCachedTextViewer.removeTextListener(fInternalListener);
                    fCachedTextViewer= null;
                }
                if (fBuffer != null) {
                    fBuffer.dispose();
                    fBuffer= null;
                }
                fCachedTextWidget= null;
            }
        });

        fMouseHandler= new MouseHandler();
        fCanvas.addMouseListener(fMouseHandler);
        fCanvas.addMouseMoveListener(fMouseHandler);

        if (fCachedTextViewer != null) {

            fCachedTextViewer.addViewportListener(fInternalListener);
            fCachedTextViewer.addTextListener(fInternalListener);


            if (fFont == null) {
                if (fCachedTextWidget != null && !fCachedTextWidget.isDisposed())
                    fFont= fCachedTextWidget.getFont();
            }
        }

        if (fFont != null)
            fCanvas.setFont(fFont);
        
        computeTxtWidth();

        if (model != null) {
            adjustModel(model);
        }
        return fCanvas;
    }
    
    private void computeTxtWidth() {
        if (fCanvas == null || fCanvas.isDisposed())
            return;

        GC gc= new GC(fCanvas);
        try {
            gc.setFont(fCanvas.getFont());
            txtWidth = gc.stringExtent("9").x + 1; //$NON-NLS-1$
        } finally {
            gc.dispose();
        }
    }



    /**
     * Double buffer drawing.
     *
     * @param dest the GC to draw into
     */
    private void doubleBufferPaint(GC dest) {

        Point size= fCanvas.getSize();

        if (size.x <= 0 || size.y <= 0)
            return;

        if (fBuffer != null) {
            Rectangle r= fBuffer.getBounds();
            if (r.width != size.x || r.height != size.y) {
                fBuffer.dispose();
                fBuffer= null;
            }
        }
        if (fBuffer == null)
            fBuffer= new Image(fCanvas.getDisplay(), size.x, size.y);

        GC gc= new GC(fBuffer);
        try {
        	gc.setFont(fCanvas.getFont());
        	if (fForeground != null)
        		gc.setForeground(fForeground);

        	gc.setBackground(fCachedTextWidget.getBackground());
        	gc.fillRectangle(0, 0, size.x, size.y);

        	ILineRange visibleLines= JFaceTextUtil.getVisibleModelLines(fCachedTextViewer);
        	if (visibleLines == null)
        		return;
        	fScrollPos= fCachedTextWidget.getTopPixel();
        	doPaint(gc, visibleLines);
        } finally {
            gc.dispose();
        }

        dest.drawImage(fBuffer, 0, 0);
    }


    /**
     * Draws the ruler column.
     *
     * @param gc the GC to draw into
     * @param visibleLines the visible model lines
     */
    private void doPaint(GC gc, ILineRange visibleLines) {
        Display display= fCachedTextWidget.getDisplay();
        
        int lastLine = visibleLines.getStartLine() + visibleLines.getNumberOfLines();
        int y        = -JFaceTextUtil.getHiddenTopLinePixels(fCachedTextWidget);
        
        if (mapMarks != null) {
            for (int line= visibleLines.getStartLine(); line < lastLine; line++) {
                int widgetLine= JFaceTextUtil.modelLineToWidgetLine(fCachedTextViewer, line);
                if (widgetLine == -1)
                    continue;
                int lineHeight= fCachedTextWidget.getLineHeight(fCachedTextWidget.getOffsetAtLine(widgetLine));
    
                if (mapMarks != null && mapMarks.containsKey(line)) {
                    paintLine(line, mapMarks.get(line), y, lineHeight, gc, display);
                }
                y += lineHeight;
            }
        }
    }

    private void paintLine(int line, ArrayList<Integer> bmarks, int y, int lineheight, GC gc, Display display) {
        int cy = (lineheight - ICON_HEIGHT)/2;
        int baselineBias = getBaselineBias(gc, line);
        for (int i=0; i<bmarks.size(); ++i) {
            int bmNum = bmarks.get(i);
            Image image = isTextMode ? null : XBookmarksPlugin.getDefault().getCachedBookmarkImage(bmNum);
            if (image != null) {
                Rectangle r = new Rectangle(1 + i*ICON_WIDTH, y + cy, ICON_WIDTH, ICON_HEIGHT);
                ImageUtilities.drawImage(image, gc, fCanvas, r, SWT.CENTER, SWT.CENTER);
            } else {
                gc.setBackground(yellowColor);
                Rectangle r = new Rectangle(i*txtWidth, y + baselineBias, txtWidth, lineheight);
                gc.fillRectangle(r);
                gc.drawRectangle(r);
                gc.drawString(""+bmNum, 1 + i*txtWidth, y + baselineBias, true); //$NON-NLS-1$
            }
        }
    }

    private int getBaselineBias(GC gc, int widgetLine) {
        /*
         * https://bugs.eclipse.org/bugs/show_bug.cgi?id=62951
         * widget line height may be more than the font height used for the
         * line numbers, since font styles (bold, italics...) can have larger
         * font metrics than the simple font used for the numbers.
         */
        int offset= fCachedTextWidget.getOffsetAtLine(widgetLine);
        int widgetBaseline= fCachedTextWidget.getBaseline(offset);

        FontMetrics fm= gc.getFontMetrics();
        int fontBaseline= fm.getAscent() + fm.getLeading();
        int baselineBias= widgetBaseline - fontBaseline;
        return Math.max(0, baselineBias);
    }
    
    /**
     * Triggers a redraw in the display thread.
     *
     * @since 3.0
     */
    protected final void postRedraw() {
        if (fCanvas != null && !fCanvas.isDisposed()) {
            Display d= fCanvas.getDisplay();
            if (d != null) {
                synchronized (fRunnableLock) {
                    if (fIsRunnablePosted)
                        return;
                    fIsRunnablePosted= true;
                }
                d.asyncExec(fRunnable);
            }
        }
    }

    /*
     * @see IVerticalRulerColumn#redraw()
     */
    @Override
    public void redraw() {

        if (fRelayoutRequired) {
            layout(true);
            return;
        }

        if (fCachedTextViewer != null && fCanvas != null && !fCanvas.isDisposed()) {
            if (IS_MAC) {
                fCanvas.redraw();
                fCanvas.update();
            } else {
                GC gc= new GC(fCanvas);
                try {
                	doubleBufferPaint(gc);
					
				} finally {
					gc.dispose();
				}
            }
        }
    }

    /**
     * Layouts the enclosing viewer to adapt the layout to changes of the
     * size of the individual components.
     *
     * @param redraw <code>true</code> if this column can be redrawn
     */
    protected void layout(boolean redraw) {
        if (!redraw) {
            fRelayoutRequired= true;
            return;
        }

        fRelayoutRequired= false;
        if (fCachedTextViewer instanceof ITextViewerExtension) {
            ITextViewerExtension extension= (ITextViewerExtension) fCachedTextViewer;
            Control control= extension.getControl();
            if (control instanceof Composite && !control.isDisposed()) {
                Composite composite= (Composite) control;
                composite.layout(true);
            }
        }
    }


    /*
     * @see IVerticalRulerColumn#setModel(IAnnotationModel)
     */
    @Override
    public void setModel(IAnnotationModel model) {
        if (this.model != null) {
            this.model.removeAnnotationModelListener(modelListener);
        }
        this.model = model;
        if (model != null) {
            model.addAnnotationModelListener(modelListener);
        }
        adjustModel(model);
        return;
    }

    /*
     * @see IVerticalRulerColumn#setFont(Font)
     */
    @Override
    public void setFont(Font font) {
        fFont= font;
        if (fCanvas != null && !fCanvas.isDisposed()) {
            fCanvas.setFont(fFont);
            computeTxtWidth();
        }
    }

    
    
    //------------------------------------------------------------------------------//
    //------------------------------------------------------------------------------//
    //------------------------------------------------------------------------------//

    class InternalListener implements IViewportListener, ITextListener {

        private boolean fCachedRedrawState= true;

        public void viewportChanged(int verticalPosition) {
            if (fCachedRedrawState && verticalPosition != fScrollPos)
                redraw();
        }
        
        public void textChanged(TextEvent event) {

            fCachedRedrawState= event.getViewerRedrawState();
            if (!fCachedRedrawState)
                return;

            postRedraw();
        }
        
    }

    //------------------------------------------------------------------------------//
    //------------------------------------------------------------------------------//
    //------------------------------------------------------------------------------//
    
    /**
     * Handles all the mouse interaction in this ruler column.
     */
    class MouseHandler implements MouseListener, MouseMoveListener {

        /** The cached view port size. */
        private int fCachedViewportSize;
        /** The area of the line at which line selection started. */
        private int fStartLineOffset;
        /** The number of the line at which line selection started. */
        private int fStartLineNumber;
        /** The auto scroll direction. */
        private int fAutoScrollDirection;
        /* @since 3.2 */
        private boolean fIsListeningForMove= false;

        /*
         * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseUp(MouseEvent event) {
            // see bug 45700
            if (event.button == 1) {
                stopSelecting();
                stopAutoScroll();
            }
        }

        /*
         * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseDown(MouseEvent event) {
            fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
            // see bug 45700
            if (event.button == 1) {
                startSelecting((event.stateMask & SWT.SHIFT) != 0);
            }
        }

        /*
         * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseDoubleClick(MouseEvent event) {
            fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
            stopSelecting();
            stopAutoScroll();
        }

        /*
         * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseMove(MouseEvent event) {
            if (fIsListeningForMove && !autoScroll(event)) {
                int newLine= fParentRuler.toDocumentLineNumber(event.y);
                expandSelection(newLine);
            }
            fParentRuler.setLocationOfLastMouseButtonActivity(event.x, event.y);
        }

        /**
         * Called when line drag selection started. Adds mouse move and track
         * listeners to this column's control.
         *
         * @param expandExistingSelection if <code>true</code> the existing selection will be expanded,
         *          otherwise a new selection is started
         */
        private void startSelecting(boolean expandExistingSelection) {
            try {

                // select line
                IDocument document= fCachedTextViewer.getDocument();
                int lineNumber= fParentRuler.getLineOfLastMouseButtonActivity();
                final StyledText textWidget= fCachedTextViewer.getTextWidget();
                if (textWidget != null && !textWidget.isFocusControl())
                    textWidget.setFocus();
                if (expandExistingSelection && fCachedTextViewer instanceof ITextViewerExtension5 && textWidget != null) {
                    ITextViewerExtension5 extension5= ((ITextViewerExtension5)fCachedTextViewer);
                    // Find model cursor position
                    int widgetCaret= textWidget.getCaretOffset();
                    int modelCaret= extension5.widgetOffset2ModelOffset(widgetCaret);
                    // Find model selection range
                    Point selection= fCachedTextViewer.getSelectedRange();
                    // Start from tail of selection range (opposite of cursor position)
                    int startOffset= modelCaret == selection.x ? selection.x + selection.y : selection.x;

                    fStartLineNumber= document.getLineOfOffset(startOffset);
                    fStartLineOffset= startOffset;

                    expandSelection(lineNumber);
                } else {
                    fStartLineNumber= lineNumber;
                    fStartLineOffset= document.getLineInformation(fStartLineNumber).getOffset();
                    fCachedTextViewer.setSelectedRange(fStartLineOffset, 0);
                }
                fCachedViewportSize= getVisibleLinesInViewport();

                // prepare for drag selection
                fIsListeningForMove= true;

            } catch (BadLocationException x) {
            }
        }

        /**
         * Called when line drag selection stopped. Removes all previously
         * installed listeners from this column's control.
         */
        private void stopSelecting() {
            // drag selection stopped
            fIsListeningForMove= false;
        }

        /**
         * Expands the line selection from the remembered start line to the
         * given line.
         *
         * @param lineNumber the line to which to expand the selection
         */
        private void expandSelection(int lineNumber) {
            try {

                IDocument document= fCachedTextViewer.getDocument();
                IRegion lineInfo= document.getLineInformation(lineNumber);

                Display display= fCachedTextWidget.getDisplay();
                Point absolutePosition= display.getCursorLocation();
                Point relativePosition= fCachedTextWidget.toControl(absolutePosition);

                int offset;

                if (relativePosition.x < 0)
                    offset= lineInfo.getOffset();
                else {
                    try {
                        int widgetOffset= fCachedTextWidget.getOffsetAtLocation(relativePosition);
                        Point p= fCachedTextWidget.getLocationAtOffset(widgetOffset);
                        if (p.x > relativePosition.x)
                            widgetOffset--;

                        // Convert to model offset
                        if (fCachedTextViewer instanceof ITextViewerExtension5) {
                            ITextViewerExtension5 extension= (ITextViewerExtension5)fCachedTextViewer;
                            offset= extension.widgetOffset2ModelOffset(widgetOffset);
                        } else
                            offset= widgetOffset + fCachedTextViewer.getVisibleRegion().getOffset();

                    } catch (IllegalArgumentException ex) {
                        int lineEndOffset= lineInfo.getOffset() + lineInfo.getLength();

                        // Convert to widget offset
                        int lineEndWidgetOffset;
                        if (fCachedTextViewer instanceof ITextViewerExtension5) {
                            ITextViewerExtension5 extension= (ITextViewerExtension5)fCachedTextViewer;
                            lineEndWidgetOffset= extension.modelOffset2WidgetOffset(lineEndOffset);
                        } else
                            lineEndWidgetOffset= lineEndOffset - fCachedTextViewer.getVisibleRegion().getOffset();

                        Point p= fCachedTextWidget.getLocationAtOffset(lineEndWidgetOffset);
                        if (p.x < relativePosition.x)
                            offset= lineEndOffset;
                        else
                            offset= lineInfo.getOffset();
                    }
                }

                int start= Math.min(fStartLineOffset, offset);
                int end= Math.max(fStartLineOffset, offset);

                if (lineNumber < fStartLineNumber)
                    fCachedTextViewer.setSelectedRange(end, start - end);
                else
                    fCachedTextViewer.setSelectedRange(start, end - start);

            } catch (BadLocationException x) {
            }
        }

        /**
         * Called when auto scrolling stopped. Clears the auto scroll direction.
         */
        private void stopAutoScroll() {
            fAutoScrollDirection= SWT.NULL;
        }

        /**
         * Called on drag selection.
         *
         * @param event the mouse event caught by the mouse move listener
         * @return <code>true</code> if scrolling happened, <code>false</code> otherwise
         */
        private boolean autoScroll(MouseEvent event) {
            Rectangle area= fCanvas.getClientArea();

            if (event.y > area.height) {
                autoScroll(SWT.DOWN);
                return true;
            }

            if (event.y < 0) {
                autoScroll(SWT.UP);
                return true;
            }

            stopAutoScroll();
            return false;
        }

        /**
         * Scrolls the viewer into the given direction.
         *
         * @param direction the scroll direction
         */
        private void autoScroll(int direction) {

            if (fAutoScrollDirection == direction)
                return;

            final int TIMER_INTERVAL= 5;
            final Display display= fCanvas.getDisplay();
            Runnable timer= null;
            switch (direction) {
                case SWT.UP:
                    timer= new Runnable() {
                        public void run() {
                            if (fAutoScrollDirection == SWT.UP) {
                                int top= getInclusiveTopIndex();
                                if (top > 0) {
                                    fCachedTextViewer.setTopIndex(top -1);
                                    expandSelection(top -1);
                                    display.timerExec(TIMER_INTERVAL, this);
                                }
                            }
                        }
                    };
                    break;
                case  SWT.DOWN:
                    timer= new Runnable() {
                        public void run() {
                            if (fAutoScrollDirection == SWT.DOWN) {
                                int top= getInclusiveTopIndex();
                                fCachedTextViewer.setTopIndex(top +1);
                                expandSelection(top +1 + fCachedViewportSize);
                                display.timerExec(TIMER_INTERVAL, this);
                            }
                        }
                    };
                    break;
            }

            if (timer != null) {
                fAutoScrollDirection= direction;
                display.timerExec(TIMER_INTERVAL, timer);
            }
        }

        /**
         * Returns the viewer's first visible line, even if only partially visible.
         *
         * @return the viewer's first visible line
         */
        private int getInclusiveTopIndex() {
            if (fCachedTextWidget != null && !fCachedTextWidget.isDisposed()) {
                return JFaceTextUtil.getPartialTopIndex(fCachedTextViewer);
            }
            return -1;
        }
        
        private int getVisibleLinesInViewport() {
            if (fCachedTextWidget != null) {
                Rectangle clArea= fCachedTextWidget.getClientArea();
                if (!clArea.isEmpty()) {
                    int firstPixel= 0;
                    // what about margins? don't take trims as they include scrollbars:
                    int lastPixel= clArea.height - 1; 
                    int first= JFaceTextUtil.getLineIndex(fCachedTextWidget, firstPixel);
                    int last= JFaceTextUtil.getLineIndex(fCachedTextWidget, lastPixel);
                    return last - first;
                }
            }
            return -1;
        }

    } // class MouseHandler


}
