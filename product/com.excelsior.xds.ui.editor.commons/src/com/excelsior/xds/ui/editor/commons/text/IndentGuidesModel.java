package com.excelsior.xds.ui.editor.commons.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IPainter;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.parser.indents.IndentGuideDescriptor;
import com.excelsior.xds.parser.indents.IndentsParser;
import com.excelsior.xds.ui.editor.commons.SourceCodeTextEditor;
import com.excelsior.xds.ui.editor.commons.internal.nls.Messages;

/**
 * Model of vertical indent guides in the source code. 
 * 
 * @author lion
 */
public class IndentGuidesModel {

    private volatile List<IndentGuideDescriptor> indentDescriptors;    

    private IDocumentListener  documentListener;
    private volatile IDocument listenedDocument;
    private volatile IPainter  documentPainter;
    private volatile StyledText textWidget;
    private final String eolPrefix;
    
    
    public IndentGuidesModel(SourceCodeTextEditor editor) {
        eolPrefix = editor.getEOLCommentPrefix();
        documentListener = new IDocumentListener() {
            @Override
            public void documentAboutToBeChanged(DocumentEvent event) {
            }
            @Override
            public void documentChanged(DocumentEvent event) {
                requestUpdate();
            }
        };
    }
    
    
    /**
     * Replaces current set of indent descriptors by new one.  
     * 
     * @param descriptors list of indent descriptors
     */
    protected void assumeIndents(List<IndentGuideDescriptor> descriptors) {
        indentDescriptors = descriptors; 
    }
    
    
    /**
     * Sets the given document as the indents' model input and updates
     * the model accordingly.
     * 
     * @param document the models's new input document, <code>null</code> if none
     * @param painter the painter to be notified about model changes, <code>null</code> if none
     */
    public void applyToDocument(IDocument document, IPainter painter, StyledText widget) {
        deactivate();
        textWidget       = widget;
        documentPainter  = painter;
        listenedDocument = document;
        listenedDocument.addDocumentListener(documentListener);
        requestUpdate();
    }
    
    
    /**
     * Deactivates this indents model. If the model is inactive, this call does not
     * have any effect. A deactivated model can be reactivated by calling 
     * <code>applyToDocument</code>.
     */
    public void deactivate() {
        if (listenedDocument != null) {
            listenedDocument.removeDocumentListener(documentListener);
        }
        documentPainter   = null;
        listenedDocument  = null;
        indentDescriptors = null;
    }
    
    
    /**
     * Returns indent guides which are present at the given source code line.
     * 
     * @param line  source code line number
     * 
     * @return indent guides at the given source code line.
     */
    public IndentGuideDescriptor[] getIndentGuidesAtLine(int line) {
        if (indentDescriptors == null)
            return null;
        
        List<IndentGuideDescriptor> indentGuides = new ArrayList<IndentGuideDescriptor>(16);
        for (IndentGuideDescriptor indent : indentDescriptors) {
            if (indent.indentLevel != 0 && indent.startLine < line && line < indent.endLine) {
                indentGuides.add(indent);
            }
        }
        
        return indentGuides.toArray(new IndentGuideDescriptor[indentGuides.size()]);
    }
    
    
    /**
     * Schedules the update of indent guides model to be run. The job is added to a 
     * queue of waiting jobs, and will be run when it arrives at the beginning 
     * of the queue.
     */    
    protected void requestUpdate() {
        Job job = new Job(Messages.UpdateIndentGuides) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                IDocument document = listenedDocument;
                if (document != null) {
                    try {
                        final int[] tabSizeNah = new int[1];
                        tabSizeNah[0] = -1;
                        Display.getDefault().syncExec(new Runnable() {
                            @Override
                            public void run() {
                            	if (!textWidget.isDisposed()){
                            		tabSizeNah[0] = textWidget.getTabs(); //#^(@!! gui thread expected
                            	}
                            }
                        });
                        if (tabSizeNah[0] == -1) { // Widget is disposed
                        	return Status.OK_STATUS;
                        }
                        CharSequence chars = document.get();
                        assumeIndents(IndentsParser.buildDescriptors(chars, tabSizeNah[0], eolPrefix));
                    } catch (Exception e) {
                        LogHelper.logError(e);
                    }
                    
                    if (documentPainter != null) {
                        Display.getDefault().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                IPainter painter = documentPainter; 
                                if (painter != null) 
                                    painter.paint(IPainter.CONFIGURATION);
                            }
                        });
                    }
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
    
}
