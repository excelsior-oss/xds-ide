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
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
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
import com.excelsior.xds.ui.editor.modula.IModulaPartitions;
import com.excelsior.xds.ui.editor.modula.ModulaEditor;

/**
 * A command handler to remove the block comment enclosing the selection.
 */
public class RemoveBlockCommentHandler extends AbstractHandler implements IHandler
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
                    int pos = selection.getOffset();

                    ITypedRegion partition = TextUtilities.getPartition(doc, IModulaPartitions.M2_PARTITIONING, pos, false);
                    if (!partition.getType().equals(IModulaPartitions.M2_CONTENT_TYPE_BLOCK_COMMENT)) {
                        if (pos > 1) {
                            // is cursor is exactly after "(* ... *)" ?
                                    partition = TextUtilities.getPartition(doc, IModulaPartitions.M2_PARTITIONING, pos-2, false);
                        }
                    }

                    if (!partition.getType().equals(IModulaPartitions.M2_CONTENT_TYPE_BLOCK_COMMENT)) {
                        // Search next (**) comment near cursor and uncomment it. Test 2 next lines.
                        partition = searchCommentStart(doc, pos, 0);
                        if (partition == null) {
                            partition = searchCommentStart(doc, pos, 1);
                        }
                    }

                    ArrayList<ReplaceEdit> edits = new ArrayList<ReplaceEdit>();
                    int offsAfter = 0;

                    if (partition != null) {
                        // Uncomment this partition
                        int offs = partition.getOffset();
                        int len = partition.getLength();
                        if (len >= 4 && // check it for diff case..
                                doc.get(offs, 2).equals("(*") &&
                                doc.get(offs+len-2, 2).equals("*)"))
                        {
                            // CRLF-s lengths when comment is <crlf> "(*" <CRLF> .... <CRLF> "*)" <crlf>
                            // in this case it will be removed with the comment:
                            int crlf1 = 0;
                            int crlf2 = 0;
                            {
                                int lin1 = doc.getLineOfOffset(offs);
                                int lin2 = doc.getLineOfOffset(offs + len - 1);
                                if (doc.getLineInformation(lin1).getLength() == 2 &&
                                    doc.getLineInformation(lin2).getLength() == 2) 
                                { // lines are exactly "(*" and "*)"
                                    String s = doc.getLineDelimiter(lin1);
                                    crlf1 = s != null ? s.length() : 0;
                                    
                                    if (lin2 > lin1 + 1) {
                                        s = doc.getLineDelimiter(lin2-1);
                                        crlf2 = s != null ? s.length() : 0;
                                    }
                                }
                            }
                            
                            edits.add(new ReplaceEdit(offs, 2 + crlf1, ""));
                            edits.add(new ReplaceEdit(offs+len-2 - crlf2, 2 + crlf2, ""));
                            offsAfter = offs + len - 4 - crlf1 - crlf2;

                            // if offsAfter is on the end of line - jump to beginning of the next line:
                            boolean eol = true;
                            int lnum = doc.getLineOfOffset(offs + len - 1);
                            IRegion reg = doc.getLineInformation(lnum);
                            String line = doc.get(reg.getOffset(), reg.getLength());
                            line = line.substring(offs+len - reg.getOffset()); // line tail after "*)"
                            for (char ch : line.toCharArray()) {
                                if (ch != ' ' && ch != '\t') {
                                    eol = false;
                                    break;
                                }
                                
                            }
                            if (eol) {
                                String crlf = doc.getLineDelimiter(lnum);
                                if (crlf != null) {
                                    offsAfter += line.length() + crlf.length();
                                }
                            }
                        }
                    }

                    if (!edits.isEmpty()) {
                        DocumentRewriteSession drws = null;
                        try {
                            if (doc instanceof IDocumentExtension4) {
                                drws = ((IDocumentExtension4)doc).startRewriteSession(DocumentRewriteSessionType.UNRESTRICTED);
                            }
                            MultiTextEdit edit= new MultiTextEdit(0, doc.getLength());
                            edit.addChildren((TextEdit[]) edits.toArray(new TextEdit[edits.size()]));
                            edit.apply(doc, TextEdit.CREATE_UNDO);
                            iTextEditor.getSelectionProvider().setSelection(new TextSelection(offsAfter, 0));
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

    private ITypedRegion searchCommentStart(IDocument doc, int pos, int addLines) throws BadLocationException {
        int lnum = doc.getLineOfOffset(pos);
        lnum += addLines;
        if (lnum < doc.getNumberOfLines()) {
            IRegion reg = doc.getLineInformation(lnum);
            String line = doc.get(reg.getOffset(), reg.getLength());
            for (int i = addLines > 0 ? 0 : pos - reg.getOffset(); i < line.length(); ++i) {
                if (line.charAt(i) == '(' && 
                    i+1 < line.length() && 
                    line.charAt(i+1) == '*')
                {
                    ITypedRegion partition = TextUtilities.getPartition(doc, IModulaPartitions.M2_PARTITIONING, 
                            reg.getOffset() + i, false);
                    if (partition.getType().equals(IModulaPartitions.M2_CONTENT_TYPE_BLOCK_COMMENT)) {
                        return partition;
                    }
                }
            }
        }
        return null;
    }

}
