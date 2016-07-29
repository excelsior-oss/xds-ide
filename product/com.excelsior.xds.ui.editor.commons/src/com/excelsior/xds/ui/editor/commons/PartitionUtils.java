package com.excelsior.xds.ui.editor.commons;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.ITypedRegion;

public class PartitionUtils {

    /**
     * Returns the partitioner for the given partitioning or <code>null</code> if
     * no partitioner is registered.
     *
     * @param document the document to be processed
     * @param  partitioningType the partitioning for which to set the partitioner
     * @return the partitioner for the given partitioning
     * 
     * @see org.eclipse.jface.text.IDocumentExtension3#getDocumentPartitioner(IDocument,String)
     */
    public static IDocumentPartitioner getPartitioner( IDocument document
                                                     , String partitioningType ) 
    {
        IDocumentPartitioner result = null;
        if (document instanceof IDocumentExtension3) {
            IDocumentExtension3 extension = (IDocumentExtension3) document;
            result = extension.getDocumentPartitioner(partitioningType);
        } else if (document != null){
            result = document.getDocumentPartitioner();
        }
        return result;
    }    
    

    /**
     * Sets document's partitioner. 
     *
     * @param document the document to be processed
     * @param partitioningType the partitioning for which to set the partitioner
     * @param partitioner the document's new partitioner
     * 
     * @see org.eclipse.jface.text.IDocumentExtension3#setDocumentPartitioner(IDocument,String,IDocumentPartitioner)
     * @see IDocumentPartitioningListener
     */
    public static void setDocumentPartitioning( IDocument document
                                              , String partitioningType
                                              , IDocumentPartitioner partitioner ) 
    {
        // Setting the partitioner will trigger a partitionChanged listener that
        // will attempt to use the partitioner to initialize the document's
        // partitions. Therefore, need to connect first.
        partitioner.connect(document);
        if (document instanceof IDocumentExtension3) {
            IDocumentExtension3 extension3= (IDocumentExtension3) document;
            extension3.setDocumentPartitioner(partitioningType, partitioner);
        } else {
            document.setDocumentPartitioner(partitioner);
        }
    }    

    
    /**
     * Returns the document partition of the given partitioning in which the
     * given offset is located.
     * 
     * @param document the document to be processed
     * @param partitioning the partitioning
     * @param offset the document offset
     * @param preferOpenPartitions <code>true</code> if precedence should be
     *        given to a open partition ending at <code>offset</code> over a
     *        closed partition starting at <code>offset</code>
     * @return a specification of the partition
     * 
     * @see org.eclipse.jface.text.IDocumentExtension3#getPartition(IDocument,String,int,boolean)
     */
    public static ITypedRegion getPartition( IDocument document
                                           , String partitioningType
                                           , int offset, boolean preferOpenPartition ) 
    {
        ITypedRegion region = null;
        try {
            if (document instanceof IDocumentExtension3) {
                IDocumentExtension3 extension = (IDocumentExtension3) document;
                try {
                    region = extension.getPartition(partitioningType, offset, true);
                } catch (BadPartitioningException e) {
                    // Log the error.
                }
            } else {
                region = document.getPartition(offset);
            }
        } catch (BadLocationException e) {
            // Log the error.
        }
        return region;
    }    
    
}
