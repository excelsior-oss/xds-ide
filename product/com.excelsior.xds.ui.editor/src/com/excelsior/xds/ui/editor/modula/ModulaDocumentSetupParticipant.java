package com.excelsior.xds.ui.editor.modula;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IDocumentSetupParticipantExtension;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;

import com.excelsior.xds.core.preferences.PreferenceKeys;
import com.excelsior.xds.ui.editor.commons.PartitionUtils;
import com.excelsior.xds.ui.editor.modula.scanner.rules.ModulaRuleBasedPartitionScanner;

/**
 * The document setup participant for a Modula-2 source file document.
 */
public class ModulaDocumentSetupParticipant implements IDocumentSetupParticipant
                                                     , IDocumentSetupParticipantExtension 
{

    /**
     * {@inheritDoc}
     */
    @Override
    public void setup(IDocument document) {
        setupPartitioner(document);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
	public void setup(IDocument document, IPath location, LocationKind locationKind) {
    	setup(document);
	}

    /**
     * Creates and sets document's partitioner. 
     *
     * @param document the document for setup
     * @see org.eclipse.core.filebuffers.IDocumentSetupParticipant#setup(org.eclipse.jface.text.IDocument)
     */
    public static void setupPartitioner(IDocument document) {
        if (PartitionUtils.getPartitioner(document, IModulaPartitions.M2_PARTITIONING) == null) {
            IDocumentPartitioner partitioner = createDocumentPartitioner();
            PartitionUtils.setDocumentPartitioning(document, IModulaPartitions.M2_PARTITIONING, partitioner); 
        }
    }
    
    /**
     * Factory method for creating a Modula-2 specific document partitioner.
     *
     * @return a newly created properties file document partitioner
     */

    public static ModulaFastPartitioner createDocumentPartitioner() {
        ModulaFastPartitioner modulaFastPartitioner = new ModulaFastPartitioner( new ModulaRuleBasedPartitionScanner()
                                  , IModulaPartitions.LEGAL_CONTENT_TYPES );
		modulaFastPartitioner.setShowInactiveCode(PreferenceKeys.PKEY_HIGHLIGHT_INACTIVE_CODE.getStoredBoolean());
		return modulaFastPartitioner;
    }
    
}
