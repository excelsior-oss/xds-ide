package com.excelsior.xds.ui.editor.symfile;

import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import com.excelsior.xds.ui.editor.modula.ModulaEditor;

public class XdsSymfileEditor extends ModulaEditor 
{
    /**
     * The shared decompiled XDS Symbol-file document provider.
     */
    private IDocumentProvider symfileDocumentProvider;

    
    public XdsSymfileEditor() {
        super();
    }
    
    protected synchronized IDocumentProvider createDocumentProvider() {
        if (symfileDocumentProvider == null) {
            symfileDocumentProvider = new XdsSymfileForwardingDocumentProvider();
        }
        return symfileDocumentProvider;
    }
    
    @Override
    protected void createActions() {
        super.createActions();

        setAction(ITextEditorActionConstants.SAVE, null);
        setAction(ITextEditorActionConstants.REVERT_TO_SAVED, null);
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isEditorInputReadOnly() {
        return true;
    }

}
