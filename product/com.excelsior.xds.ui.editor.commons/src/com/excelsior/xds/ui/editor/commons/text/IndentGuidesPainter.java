package com.excelsior.xds.ui.editor.commons.text;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.excelsior.xds.parser.indents.IndentGuideDescriptor;
import com.excelsior.xds.ui.commons.utils.UiUtils;
import com.excelsior.xds.ui.editor.commons.internal.preferences.IndentGuidePreferencePage;
import com.excelsior.xds.ui.editor.commons.internal.preferences.IndentGuidePreferencePage.PreferenceConstants;

/**
 * A painter is responsible for creating, managing, updating, and removing
 * visual decorations of vertical indent guides.
 */
public class IndentGuidesPainter implements IPainter, PaintListener {

	/** Indicates whether this painter is active. */
	private boolean fIsActive = false;
	/** The source viewer this painter is attached to. */
	private ITextViewer fTextViewer;
	/** The viewer's widget. */
	private StyledText fTextWidget;
	/** Tells whether the advanced graphics sub system is available. */
	private final boolean fIsAdvancedGraphicsPresent;

	private IndentGuidesModel indentsModel;

	
    private Color color;
	private int lineAlpha;
	private int lineStyle;
	private int lineWidth;
	private int lineShift;
	private int spaceWidth;

	
    public static void installToViewer(ITextViewer textViewer, IndentGuidesModel indentsModel) {
        if (textViewer instanceof ITextViewerExtension2) {
            new PainterOnOffManager(textViewer, indentsModel);
        }
    
    }
    
    
    private static class PainterOnOffManager {
        private IndentGuidesPainter painter;
        private ITextViewer textViewer;
        private IndentGuidesModel indentsModel;
        private IPreferenceStore store;
        
        public PainterOnOffManager(ITextViewer textViewer, IndentGuidesModel indentsModel)
        {
            this.textViewer = textViewer;
            this.indentsModel = indentsModel;
            this.store = IndentGuidePreferencePage.staticGetPreferenceStore();
            
            store.addPropertyChangeListener(new IPropertyChangeListener() 
            {
        	    private Set<String> hsProps;
        	    
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                	if (hsProps == null) {
            	        hsProps = new HashSet<String>();
                        hsProps.add(IndentGuidePreferencePage.PreferenceConstants.ENABLED);
                        hsProps.add(IndentGuidePreferencePage.PreferenceConstants.LINE_ALPHA);
                        hsProps.add(IndentGuidePreferencePage.PreferenceConstants.LINE_STYLE);
                        hsProps.add(IndentGuidePreferencePage.PreferenceConstants.LINE_WIDTH);
                        hsProps.add(IndentGuidePreferencePage.PreferenceConstants.LINE_SHIFT);
                        hsProps.add(IndentGuidePreferencePage.PreferenceConstants.LINE_COLOR);
                	}

                	if (event == null || hsProps.contains(event.getProperty())) {
                		reReadStore(event);
                	}
                }
            });
            reReadStore(null);
        }
        
        private void reReadStore(PropertyChangeEvent event) {
            String p = event!=null ? event.getProperty() : null;
            
            if (painter != null) {
            	painter.getPreferencies();
            }
            
            if (p == null || p.equals(IndentGuidePreferencePage.PreferenceConstants.ENABLED))  {
                if (store.getBoolean(IndentGuidePreferencePage.PreferenceConstants.ENABLED)) {
                    showIndentGuides();
                } else {
                    hideIndentGuides();
                }
            }
        }
        
        private void showIndentGuides() {
            if (painter == null) {
                painter= new IndentGuidesPainter(textViewer, indentsModel);
                painter.getPreferencies();
                ((ITextViewerExtension2)textViewer).addPainter(painter);
            }
        }
        
        private void hideIndentGuides() {
            if (painter != null) {
            	((ITextViewerExtension2)textViewer).removePainter(painter);
                painter.deactivate(true);
                painter.dispose();
                painter= null;
            }
        }
    }
    

    
	
	/**
	 * Creates a new painter for the given text viewer.
	 * 
	 * @param textViewer
	 *            the text viewer the painter should be attached to
	 * @param iXdsModuleIndentModel 
	 */
	private IndentGuidesPainter(ITextViewer textViewer, IndentGuidesModel indentsModel) {
		super();
		fTextViewer = textViewer;
		fTextWidget = textViewer.getTextWidget();
		GC gc = new GC(fTextWidget);
		try {
			gc.setAdvanced(true);
			fIsAdvancedGraphicsPresent = gc.getAdvanced();
		} finally {
			gc.dispose();
		}
		
		this.indentsModel = indentsModel;
	}
	
	private int chkMinMax(int val, int min, int max, int defVal) {
	    if (val < min || val > max) {
	        return defVal;
	    }
	    return val;
	}
	
	private void getPreferencies() {
	    IPreferenceStore store = IndentGuidePreferencePage.staticGetPreferenceStore();
        lineAlpha = chkMinMax(store.getInt(PreferenceConstants.LINE_ALPHA), 0, 255, 50);
        lineStyle = chkMinMax(store.getInt(PreferenceConstants.LINE_STYLE), SWT.LINE_SOLID, SWT.LINE_DASHDOTDOT, SWT.LINE_SOLID);
        lineWidth = chkMinMax(store.getInt(PreferenceConstants.LINE_WIDTH), 1, 8, 1);
        lineShift = chkMinMax(store.getInt(PreferenceConstants.LINE_SHIFT), 0, 8, 3);
        RGB rgb = PreferenceConverter.getColor(store, PreferenceConstants.LINE_COLOR);
        UiUtils.dispose(color);
        color = new Color(Display.getDefault(), rgb);
	}
	
	/*
	 * @see org.eclipse.jface.text.IPainter#dispose()
	 */
	public void dispose() {
		fTextViewer = null;
		fTextWidget = null;
		indentsModel = null;
		UiUtils.dispose(color);
	}

    /*
     * @see org.eclipse.jface.text.IPainter#deactivate(boolean)
     */
    public void deactivate(boolean redraw) {
        if (fIsActive) {
            fIsActive = false;
            fTextWidget.removePaintListener(this);
            indentsModel.deactivate();
            if (redraw) {
                redrawAll();
            }
        }
    }
	
	/*
	 * @see org.eclipse.jface.text.IPainter#paint(int)
	 */
	public void paint(int reason) {
		IDocument document = fTextViewer.getDocument();
		if (document == null) {
			deactivate(false);
			return;
		}
		if (!fIsActive) {
			fIsActive = true;
			fTextWidget.addPaintListener(this);
			indentsModel.applyToDocument(document, this, fTextWidget);
		} else if (reason == CONFIGURATION || reason == INTERNAL) {
			redrawAll();
		} else if (reason == TEXT_CHANGE) {
			// redraw current line only
			try {
				IRegion lineRegion = document
						.getLineInformationOfOffset(getDocumentOffset(fTextWidget
								.getCaretOffset()));
				int widgetOffset = getWidgetOffset(lineRegion.getOffset());
				int charCount = fTextWidget.getCharCount();
				int redrawLength = Math.min(lineRegion.getLength(), charCount - widgetOffset);
				if (widgetOffset >= 0 && redrawLength > 0) {
					fTextWidget.redrawRange(widgetOffset, redrawLength, true);
				}
			} catch (BadLocationException e) {
				// ignore
			}
		}
	}

	/*
	 * @see
	 * org.eclipse.jface.text.IPainter#setPositionManager(org.eclipse.jface.
	 * text.IPaintPositionManager)
	 */
	public void setPositionManager(IPaintPositionManager manager) {
		// no need for a position manager
	}

	/*
	 * @see
	 * org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events
	 * .PaintEvent)
	 */
	public void paintControl(PaintEvent event) {
		if (fTextWidget != null) {
			handleDrawRequest(event.gc, event.x, event.y, event.width, event.height);
		}
	}

	/*
	 * Draw characters in view range.
	 */
	private void handleDrawRequest(GC gc, int x, int y, int w, int h) {
		int startLine = fTextWidget.getLineIndex(y);
		int endLine = fTextWidget.getLineIndex(y + h - 1);
		if (startLine <= endLine && startLine < fTextWidget.getLineCount()) {
			Color fgColor = gc.getForeground();
			LineAttributes lineAttributes = gc.getLineAttributes();
			gc.setForeground(color);
			gc.setLineStyle(lineStyle);
			gc.setLineWidth(lineWidth);
			if (fIsAdvancedGraphicsPresent) {
				int alpha = gc.getAlpha();
				gc.setAlpha(this.lineAlpha);
				drawLineRange(gc, startLine, endLine, x, w);
				gc.setAlpha(alpha);
			} else {
				drawLineRange(gc, startLine, endLine, x, w);
			}
			gc.setForeground(fgColor);
			gc.setLineAttributes(lineAttributes);
		}
	}

	/**
	 * Draw the given line range.
	 * 
	 * @param gc
	 *            the GC
	 * @param startLine
	 *            first line number
	 * @param endLine
	 *            last line number (inclusive)
	 * @param x
	 *            the X-coordinate of the drawing range
	 * @param w
	 *            the width of the drawing range
	 */
	private void drawLineRange(GC gc, int startLine, int endLine, int x, int w) {
		spaceWidth = gc.getAdvanceWidth(' ');

		StyledTextContent content = fTextWidget.getContent();
		for (int line = startLine; line <= endLine; line++) {
			int widgetOffset = fTextWidget.getOffsetAtLine(line);
            if (!isFoldedLine(content.getLineAtOffset(widgetOffset))) {
                IndentGuideDescriptor[] indentGuides = indentsModel.getIndentGuidesAtLine(line);
                if (indentGuides == null) 
                    return; // not computed yet
                for (IndentGuideDescriptor indent : indentGuides) {
                    draw(gc, widgetOffset, indent.indentLevel);
                }
            }
		}
	}

	/**
	 * Check if the given widget line is a folded line.
	 * 
	 * @param widgetLine
	 *            the widget line number
	 * @return <code>true</code> if the line is folded
	 */
	private boolean isFoldedLine(int widgetLine) {
		if (fTextViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) fTextViewer;
			int modelLine = extension.widgetLine2ModelLine(widgetLine);
			int widgetLine2 = extension.modelLine2WidgetLine(modelLine + 1);
			return widgetLine2 == -1;
		}
		return false;
	}

	/**
	 * Redraw all of the text widgets visible content.
	 */
	private void redrawAll() {
		fTextWidget.redraw();
	}

	/**
	 * 
	 * @param gc
	 * @param offset
	 * @param column
	 */
	private void draw(GC gc, int offset, int column) {
		Point pos = fTextWidget.getLocationAtOffset(offset);
		pos.x += column * spaceWidth + lineShift;
		gc.drawLine(pos.x, pos.y, pos.x,
				pos.y + fTextWidget.getLineHeight(offset));
	}

	/**
	 * Convert a document offset to the corresponding widget offset.
	 * 
	 * @param documentOffset
	 *            the document offset
	 * @return widget offset
	 */
	private int getWidgetOffset(int documentOffset) {
		if (fTextViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) fTextViewer;
			return extension.modelOffset2WidgetOffset(documentOffset);
		}
		IRegion visible = fTextViewer.getVisibleRegion();
		int widgetOffset = documentOffset - visible.getOffset();
		if (widgetOffset > visible.getLength()) {
			return -1;
		}
		return widgetOffset;
	}

	/**
	 * Convert a widget offset to the corresponding document offset.
	 * 
	 * @param widgetOffset
	 *            the widget offset
	 * @return document offset
	 */
	private int getDocumentOffset(int widgetOffset) {
		if (fTextViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) fTextViewer;
			return extension.widgetOffset2ModelOffset(widgetOffset);
		}
		IRegion visible = fTextViewer.getVisibleRegion();
		if (widgetOffset > visible.getLength()) {
			return -1;
		}
		return widgetOffset + visible.getOffset();
	}

}
