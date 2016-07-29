package com.excelsior.xds.ui.editor.modula;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * The shared text file document provider specialized for Modula-2 source files.
 */
public class ModulaDocumentProvider extends ForwardingDocumentProvider {

    /**
     * Creates a new Modula-2 source file document provider and sets up the parent chain.
     */
    public ModulaDocumentProvider() {
        super( IModulaPartitions.M2_PARTITIONING
             , new ModulaDocumentSetupParticipant()
             , new TextFileDocumentProvider() );
    }
    
    public ModulaDocumentProvider(IDocumentProvider parentProvider) {
        super( IModulaPartitions.M2_PARTITIONING
             , new ModulaDocumentSetupParticipant()
             , parentProvider);
    }
       
    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(Object element) throws CoreException {
        super.connect(element);
        IDocument document = getDocument(element);
        if (document != null) {
            setupDocumentListener(document);
        }
    }
    
    /**
     * Creates and sets document's listener. 
     *
     * @param document the document for setup
     */    
    public static void setupDocumentListener(IDocument document) {
        document.addDocumentListener(new IDocumentListener() {
            @Override
            public void documentAboutToBeChanged(DocumentEvent event) {
            }
            @Override
            public void documentChanged(DocumentEvent event) {
            }
        });
    }
    
}