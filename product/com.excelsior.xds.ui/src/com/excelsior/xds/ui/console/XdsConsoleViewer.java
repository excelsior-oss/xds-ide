package com.excelsior.xds.ui.console;

/*******************************************************************************
 * *Fsa: made from IOConsoleViewer & TextConsoleViewer
 *******************************************************************************/

import java.util.ArrayList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IHyperlink2;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsoleViewer;

/**
 * Viewer used to display an IOConsole
 * 
 * @since 3.1
 */
public class XdsConsoleViewer extends TextConsoleViewer {
    private int markedOffset = 0;
    private int markedLength = 0;

    /**
     * will always scroll with output if value is true.
     */
    private boolean fAutoScroll = true;

    private IDocumentListener fDocumentListener;
    
    public XdsConsoleViewer(Composite parent, TextConsole console) {
        super(parent, console);
        StyledText styledText = getTextWidget();
        styledText.setDoubleClickEnabled(false);
        styledText.addListener(SWT.MouseDoubleClick, mouseDoubleClickListener);
    }

    public boolean isAutoScroll() {
        return fAutoScroll;
    }

    public void setAutoScroll(boolean scroll) {
        fAutoScroll = scroll;
    }

    
    // event listener used to send doubleclick to hyperlink for IHyperlink2
    private Listener mouseDoubleClickListener = new Listener() {
        public void handleEvent(Event event) {
            IHyperlink hyperlink = getHyperlink();
            if (hyperlink instanceof IHyperlink2) {
                if (event.button == 1) {
                    ((IHyperlink2) hyperlink).linkActivated(event);
                }
            }
        }
    };

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.TextViewer#handleVerifyEvent(org.eclipse.swt.events.VerifyEvent)
     */
    protected void handleVerifyEvent(VerifyEvent e) {
        IDocument doc = getDocument();
        String[] legalLineDelimiters = doc.getLegalLineDelimiters();
        String eventString = e.text;
        try {
            IConsoleDocumentPartitioner partitioner = (IConsoleDocumentPartitioner) doc.getDocumentPartitioner();
            if (!partitioner.isReadOnly(e.start)) {
                boolean isCarriageReturn = false;
                for (int i = 0; i < legalLineDelimiters.length; i++) {
                    if (e.text.equals(legalLineDelimiters[i])) {
                        isCarriageReturn = true;
                        break;
                    }
                }

                if (!isCarriageReturn) {
                    super.handleVerifyEvent(e);
                    return;
                }
            }

            int length = doc.getLength();
            if (e.start == length) {
                super.handleVerifyEvent(e);
            } else {
                try {
                    doc.replace(length, 0, eventString);
                } catch (BadLocationException e1) {
                }
                e.doit = false;
            }
        } finally {
            StyledText text = (StyledText) e.widget;
            text.setCaretOffset(text.getCharCount());
        }
    }

    /**
     * makes the associated text widget uneditable.
     */
    public void setReadOnly() {
        ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
            public void run() {
                StyledText text = getTextWidget();
                if (text != null && !text.isDisposed()) {
                    text.setEditable(false);
                }
            }
        });
    }

    /**
     * @return <code>false</code> if text is editable
     */
    public boolean isReadOnly() {
        return !getTextWidget().getEditable();
    }
   
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextViewer#setDocument(org.eclipse.jface.text.IDocument)
     */
    public void setDocument(IDocument document) {
        IDocument oldDocument= getDocument();
        
        super.setDocument(document);
        
        if (oldDocument != null) {
            oldDocument.removeDocumentListener(getDocumentListener());
        }
        if (document != null) {
            document.addDocumentListener(getDocumentListener());
        }
    }
    
    private IDocumentListener getDocumentListener() {
        if (fDocumentListener == null) {
            fDocumentListener= new IDocumentListener() {
                public void documentAboutToBeChanged(DocumentEvent event) {
                }

                public void documentChanged(DocumentEvent event) {
                    if (fAutoScroll) {
                        revealEndOfDocument();
                    }
                }
            };
        }
        return fDocumentListener;
    }
    
    @Override
    public void lineGetStyle(LineStyleEvent event) {
        // Overrided to don't show hyperlinks as original console do it 
        IDocument document = getDocument();
        if (document != null && document.getLength() > 0) {
            ArrayList<StyleRange> ranges = new ArrayList<StyleRange>();
            int offset = event.lineOffset;
            int length = event.lineText.length();

            StyleRange[] partitionerStyles = ((IConsoleDocumentPartitioner) document.getDocumentPartitioner()).getStyleRanges(event.lineOffset, event.lineText.length());
            if (partitionerStyles != null) {
                for (int i = 0; i < partitionerStyles.length; i++) {
                    ranges.add(partitionerStyles[i]);
                }
            } else {
                ranges.add(new StyleRange(offset, length, null, null));
            }

            if (ranges.size() > 0) {
                event.styles = (StyleRange[]) ranges.toArray(new StyleRange[ranges.size()]);
            }
        }
    }
    
    public void setCurrentLinkRange(int offset, int length, boolean scrollToMakeItVisible) {
        try {
            boolean redraw = false;

            StyledText st = getTextWidget();
            
            if (markedOffset != offset || markedLength != length) {
                markedOffset = offset;
                markedLength = length;
                redraw = true;
            }
            
            if (scrollToMakeItVisible) {
                redraw = true;
                
                int topIdx = st.getTopIndex();
                int btmIdx = topIdx + st.getSize().y / st.getLineHeight();

                int mTop = st.getLineAtOffset(offset);
                int mBtm = st.getLineAtOffset(offset + length);

                int newTop = topIdx;
                if (mTop < topIdx) {
                    newTop = mTop;
                } else if (mBtm > btmIdx) {
                    newTop = topIdx + (mBtm - btmIdx);
                    if (newTop < mTop) {
                        newTop = mTop;
                    }
                }
                
                if (newTop != topIdx) {
                    st.setTopIndex(newTop);
                }
            }

            if (redraw) {
                st.redraw();
            }
        } catch (Exception e) {}

    }
    
    @Override
    public void lineGetBackground(LineBackgroundEvent event) {
        if (event.lineOffset >= markedOffset && event.lineOffset < markedOffset + markedLength) {
            event.lineBackground = new Color(event.display, 255,255,128);
        } else {
            event.lineBackground = null;
        }
    }

}
