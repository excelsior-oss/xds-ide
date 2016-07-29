package com.excelsior.xds.ui.editor.oberon;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IDocumentSetupParticipantExtension;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;

import com.excelsior.xds.ui.editor.modula.ModulaDocumentSetupParticipant;

/**
 * The document setup participant for a Oberon-2 source file document.
 */
public class OberonDocumentSetupParticipant implements IDocumentSetupParticipant, IDocumentSetupParticipantExtension {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setup(IDocument document) {
        ModulaDocumentSetupParticipant.setupPartitioner(document);
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public void setup(IDocument document, IPath location, LocationKind locationKind) {
    	setup(document);
	}

}
