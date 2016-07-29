package com.excelsior.xds.ui.editor.modula.commands;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.texteditor.ITextEditorExtension2;

import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.editor.commons.SourceCodeTextEditor;
import com.excelsior.xds.ui.editor.modula.ModulaEditor;

/**
 * A command handler to enclose the selection with a block comment.
 */
public class AddBlockCommentHandler extends AbstractHandler implements IHandler
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            ITextSelection selection = WorkbenchUtils.getActiveTextSelection();
            IDocument      doc       = WorkbenchUtils.getActiveDocument();
            IEditorInput   input     = WorkbenchUtils.getActiveInput();
            IEditorPart    editor    = WorkbenchUtils.getActiveEditor(false);
    
            boolean isTextOperationAllowed = (selection != null) && (doc != null) 
                                          && (input != null)     && (editor != null) 
                                          && (editor instanceof ModulaEditor);
    
            if (isTextOperationAllowed) {
                ITextEditor iTextEditor = (ITextEditor)editor;
                final ITextOperationTarget operationTarget = (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
                String commentPrefix = ((SourceCodeTextEditor)editor).getEOLCommentPrefix();
                isTextOperationAllowed = (operationTarget != null)
                                      && (operationTarget instanceof TextViewer)
                                      && (validateEditorInputState(iTextEditor))
                                      && (commentPrefix != null);
                
                if ((isTextOperationAllowed)) {
                    int startLine = selection.getStartLine();
                    int endLine = selection.getEndLine(); 
                    int selOffset = selection.getOffset();
                    int selLen = selection.getLength();
                    int realEndLine = doc.getLineOfOffset(selOffset + selLen); // for selection end at pos=0 (endLine is line before here) 
                    
                    // Are cursor and anchor at 0 positions?
                    boolean is0pos = false;
                    if (doc.getLineOffset(startLine) == selOffset) {
                        if ((doc.getLineOffset(endLine) + doc.getLineLength(endLine) == selOffset + selLen)) {
                            is0pos = true;
                        }
                    }
                    
                    
                    ArrayList<ReplaceEdit> edits = null;
                    int offsAfter[] = {0};
                    if (is0pos || selLen == 0) {
                        edits = commentWholeLines(startLine, (selLen == 0) ? startLine : endLine, realEndLine, doc, offsAfter);
                    } else {
                        edits = commentRange(selOffset, selLen, "(*", "*)", offsAfter); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    
                    if (edits != null && !edits.isEmpty()) {
                        DocumentRewriteSession drws = null;
                        try {
                            if (doc instanceof IDocumentExtension4) {
                                drws = ((IDocumentExtension4)doc).startRewriteSession(DocumentRewriteSessionType.UNRESTRICTED);
                            }
                            MultiTextEdit edit= new MultiTextEdit(0, doc.getLength());
                            edit.addChildren((TextEdit[]) edits.toArray(new TextEdit[edits.size()]));
                            edit.apply(doc, TextEdit.CREATE_UNDO);
                            iTextEditor.getSelectionProvider().setSelection(new TextSelection(offsAfter[0], 0));
                        }
                        finally {
                            if (doc instanceof IDocumentExtension4 && drws != null) {
                                ((IDocumentExtension4)doc).stopRewriteSession(drws);
                            }
                        }
                        
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected boolean validateEditorInputState(ITextEditor editor) {
        if (editor instanceof ITextEditorExtension2)
            return ((ITextEditorExtension2) editor).validateEditorInputState();
        else if (editor instanceof ITextEditorExtension)
            return !((ITextEditorExtension) editor).isEditorInputReadOnly();
        else if (editor != null)
            return editor.isEditable();
        else
            return false;
    }

    private ArrayList<ReplaceEdit> commentRange(int offset, int length, String openStr, String closeStr, int offsAfter[]) {
        //System.out.println("commentRange " + offset + " "+ length);
        ArrayList<ReplaceEdit> edits = new ArrayList<ReplaceEdit>();
        
        edits.add(new ReplaceEdit(offset, 0, openStr));
        edits.add(new ReplaceEdit(offset + length, 0, closeStr));
        offsAfter[0] = offset + openStr.length() + length + closeStr.length();
        return edits;
    }
    
    private ArrayList<ReplaceEdit> commentWholeLines(int startLine, int endLine, int realEndLine, IDocument doc, int offsAfter[]) throws BadLocationException {
        //System.out.println("commentWholeLines " + startLine  + " " + endLine);
        int beg = doc.getLineOffset(startLine);
        int end = doc.getLineOffset(endLine) + doc.getLineInformation(endLine).getLength();

        String crlf = doc.getLineDelimiter(endLine);
        if (crlf == null) crlf = ""; //$NON-NLS-1$

        boolean isSingleLine = (startLine == realEndLine);
        String openStr = isSingleLine ? "(*" : "(*" + crlf; //$NON-NLS-1$ //$NON-NLS-2$
        String closeStr = isSingleLine ? " *)" : crlf+"*)"; //$NON-NLS-1$ //$NON-NLS-2$
        ArrayList<ReplaceEdit> edits = commentRange(beg,  end-beg, openStr, closeStr, offsAfter);
        offsAfter[0] += crlf.length();
        return edits;
    }

}
