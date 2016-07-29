package com.excelsior.xds.ui.editor.symfile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;

import com.excelsior.xds.ui.editor.modula.ModulaDocumentProvider;

public class XdsSymfileForwardingDocumentProvider extends ModulaDocumentProvider 
{
    public XdsSymfileForwardingDocumentProvider() {
        super( new XdsSymfileDocumentProvider() );
    }
    
    @Override
    public boolean isModifiable(Object element) {
        return false;
    }
    
    @Override
    public void saveDocument( IProgressMonitor monitor, Object element
                            , IDocument document, boolean overwrite
                            ) throws CoreException 
    {
        // do nothing
        monitor.done();
    }
}
