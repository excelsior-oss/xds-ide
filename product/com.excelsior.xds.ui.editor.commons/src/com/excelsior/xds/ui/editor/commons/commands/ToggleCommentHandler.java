package com.excelsior.xds.ui.editor.commons.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.texteditor.ITextEditorExtension2;

import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.editor.commons.SourceCodeTextEditor;

/**
 * Command handler to toggle comment the selected lines. 
 * 
 * Comment is being toggled only for SourceViewerConfiguration which implements the method 
 * org.eclipse.jface.text.source.SourceViewerConfiguration#getDefaultPrefixes(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
 *
 * @author lion
 */
public class ToggleCommentHandler extends AbstractHandler 
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ITextSelection selection = WorkbenchUtils.getActiveTextSelection();
        IDocument      document  = WorkbenchUtils.getActiveDocument();
        IEditorInput   input     = WorkbenchUtils.getActiveInput();
        IEditorPart    editor    = WorkbenchUtils.getActiveEditor(false);

        boolean isTextOperationAllowed = (selection != null) && (document != null) 
                                      && (input != null)     && (editor != null) 
                                      && (editor instanceof SourceCodeTextEditor);

        if (isTextOperationAllowed) {
            final ITextOperationTarget operationTarget = (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
            String commentPrefix = ((SourceCodeTextEditor)editor).getEOLCommentPrefix();
            isTextOperationAllowed = (operationTarget != null)
                                  && (operationTarget instanceof TextViewer)
                                  && (validateEditorInputState((ITextEditor)editor))
                                  && (commentPrefix != null);
            
            if ((isTextOperationAllowed)) {
                final int operation = isSelectionCommented(document, selection, commentPrefix) 
                                    ? ITextOperationTarget.STRIP_PREFIX 
                                    : ITextOperationTarget.PREFIX;
    
                if (operationTarget.canDoOperation(operation)) {
                    BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
                        public void run() {
                            // Really processed in TextViewer.doOperation:
                            operationTarget.doOperation(operation);
                        }
                    });
                }
            }
        }

        return null;
    }
    
    
    /**
     * Checks and validates the editor's modifiable state. Returns <code>true</code> if an action
     * can proceed modifying the editor's input, <code>false</code> if it should not.
     *
     * <p>If the editor implements <code>ITextEditorExtension2</code>,
     * this method returns {@link ITextEditorExtension2#validateEditorInputState()};<br> else if the editor
     * implements <code>ITextEditorExtension</code>, it returns {@link ITextEditorExtension#isEditorInputReadOnly()};<br>
     * else, {@link ITextEditor#isEditable()} is returned, or <code>false</code> if the editor is <code>null</code>.</p>
     *
     * <p>There is only a difference to {@link #canModifyEditor()} if the editor implements
     * <code>ITextEditorExtension2</code>.</p>
     *
     * @return <code>true</code> if a modifying action can proceed to modify the underlying document, <code>false</code> otherwise
     */
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

    
    protected boolean isSelectionCommented(IDocument document, ITextSelection selection, String commentPrefix) 
    {
        try {
            for (int lineNum = selection.getStartLine(); lineNum <= selection.getEndLine(); ++lineNum) {
                IRegion r  = document.getLineInformation(lineNum);
                String str = document.get(r.getOffset(), r.getLength()).trim();
                if (!str.startsWith(commentPrefix)) {
                    return false;
                }
            }
            return true;
        } catch (Exception x) {
        }

        return false;
    }
    
}
