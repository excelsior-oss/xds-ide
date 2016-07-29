package com.excelsior.xds.ui.editor.oberon;

import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

import com.excelsior.xds.ui.editor.modula.IModulaPartitions;

/**
 * The shared text file document provider specialized for Oberon-2 source files.
 */
public class OberonDocumentProvider extends ForwardingDocumentProvider {

    /**
     * Creates a new Oberon-2 source file document provider and sets up the parent chain.
     */
    public OberonDocumentProvider() {
        super( IModulaPartitions.M2_PARTITIONING
             , new OberonDocumentSetupParticipant()
             , new TextFileDocumentProvider() );
    }
    
}
