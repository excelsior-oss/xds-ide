package com.excelsior.xds.ui.editor.modula.commands;

import java.util.Stack;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import com.excelsior.xds.parser.commons.ast.TokenTypes;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.editor.modula.ModulaEditor;
import com.excelsior.xds.ui.editor.modula.utils.ModulaEditorSymbolUtils;

public class SelectEnclosingElementHandler extends AbstractHandler 
{
    private static final Object syncObj = new Object();
    private static final Stack<SelInfo> stack = new Stack<SelInfo>();
    private static String curEditor = "";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            ITextSelection selection = WorkbenchUtils.getActiveTextSelection();
            IDocument      doc       = WorkbenchUtils.getActiveDocument();
            IEditorPart    editor    = WorkbenchUtils.getActiveEditor(false);
    
            if ((selection != null) && (doc != null) && (editor instanceof ModulaEditor)) 
            {
                ITextEditor iTextEditor = (ITextEditor)editor;
                final ITextOperationTarget operationTarget = (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
                
                if (operationTarget instanceof TextViewer) {
                    int initialSelOffs = selection.getOffset();
                    int initialSelLen = selection.getLength();
                    
                    // Determine if it is first step: 
                    boolean is1st = false;
                    if (stack.isEmpty()) {
                        // stack is empty?
                        is1st = true;
                    } else {
                        // stack top is not match curret selection or other editor window?
                        SelInfo si = stack.peek();
                        if ((si.offs != initialSelOffs) || (si.len != initialSelLen) || !editor.toString().equals(curEditor)) {
                            stack.clear();
                            is1st = true;
                        }
                    }
                    
                    int selOffs = initialSelOffs;
                    int selEnd  = selOffs + initialSelLen;
                    PstNode pn = ModulaEditorSymbolUtils.getPstLeafNode(iTextEditor, selOffs);
                    
                    if (is1st && pn != null && (selOffs == selEnd) && pn.getElementType().equals(TokenTypes.WHITE_SPACE)) {
                        // jump from whitespace side to the nearest node:
                        int eof = doc.getLength();
                        if ((selOffs > 0) && (selOffs == pn.getOffset())) {
                            --selOffs; 
                            --selEnd;
                            pn = ModulaEditorSymbolUtils.getPstLeafNode(iTextEditor, selOffs);
                        } else if ((selOffs+1 < eof) && selOffs == pn.getOffset() + pn.getLength()) {
                            ++selOffs;
                            ++selEnd;
                            pn = ModulaEditorSymbolUtils.getPstLeafNode(iTextEditor, selOffs);
                        }
                    }
                    
                    for (; pn != null; pn = pn.getParent()) {
                        int offs = pn.getOffset();
                        int len = pn.getLength();
                        int end = offs + len;
                        if ((offs < selOffs && end >= selEnd) || (offs <= selOffs && end > selEnd)) {
                            // Do selection:
                            iTextEditor.getSelectionProvider().setSelection(new TextSelection(offs, len));
                            synchronized (syncObj) {
                                if (is1st) {
                                    curEditor = iTextEditor.toString();
                                    stack.push(new SelInfo(initialSelOffs, initialSelLen));
                                }
                                stack.push(new SelInfo(offs, len));
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    /**
     * Called from RestoreLastSelectionHandler.class
     */
    public static void restoreSelection() {
        try {
            synchronized (syncObj) {
                ITextSelection selection = WorkbenchUtils.getActiveTextSelection();
                IEditorPart    editor    = WorkbenchUtils.getActiveEditor(false);
        
                if ((selection != null) && (editor instanceof ModulaEditor)) 
                {
                    // Is it the same editor?
                    if (editor.toString().equals(curEditor) && !stack.isEmpty()){
                        // Is selection preserved since last enclosion selection?
                        SelInfo si = stack.pop();
                        if (selection.getOffset() == si.offs && selection.getLength() == si.len && !stack.isEmpty()) {
                            si = stack.peek();
                            ((ITextEditor)editor).getSelectionProvider().setSelection(new TextSelection(si.offs, si.len));
                            if (stack.size() > 1) {
                                return;
                            } // else - it was last item, clear all
                        }
                    }
                }
                stack.clear();
                curEditor = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    private static class SelInfo {
        public int offs;
        public int len;
        
        public SelInfo (int offs, int len) {
            this.offs = offs;
            this.len = len;
        }
    }

}
