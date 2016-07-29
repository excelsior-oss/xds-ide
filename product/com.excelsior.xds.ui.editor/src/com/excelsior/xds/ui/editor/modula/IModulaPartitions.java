package com.excelsior.xds.ui.editor.modula;

import org.eclipse.jface.text.IDocument;

/**
 * Definition of Modula-2 partitioning and its partitions.
 */
public interface IModulaPartitions {

    /**
     * The uniquely identifier of the Modula-2 partitioning.
     */
	public static final String M2_PARTITIONING = "__m2_partitioning__"; //$NON-NLS-1$
	
	public static final String M2_CONTENT_TYPE_DEFAULT             = IDocument.DEFAULT_CONTENT_TYPE;
    public static final String M2_CONTENT_TYPE_BLOCK_COMMENT       = "__m2_block_comment"; //$NON-NLS-1$
    public static final String M2_CONTENT_TYPE_END_OF_LINE_COMMENT = "__m2_end_of_line_comment"; //$NON-NLS-1$
	public static final String M2_CONTENT_TYPE_SINGLE_QUOTE_STRING = "__m2_single_quote_string__"; //$NON-NLS-1$
    public static final String M2_CONTENT_TYPE_DOUBLE_QUOTE_STRING = "__m2_double_quote_string__"; //$NON-NLS-1$
    public static final String M2_CONTENT_TYPE_PRAGMA              = "__m2_pragma__"; //$NON-NLS-1$
    public static final String M2_CONTENT_TYPE_DISABLED_CODE       = "__m2_disabled_code__"; //$NON-NLS-1$
	
    /**
     * Array with legal content types of Modula-2 partitioner.
     */
    public final static String[] LEGAL_CONTENT_TYPES = new String[] {
        M2_CONTENT_TYPE_BLOCK_COMMENT,
        M2_CONTENT_TYPE_END_OF_LINE_COMMENT,
        M2_CONTENT_TYPE_SINGLE_QUOTE_STRING,
        M2_CONTENT_TYPE_DOUBLE_QUOTE_STRING,
        M2_CONTENT_TYPE_PRAGMA,
        M2_CONTENT_TYPE_DISABLED_CODE
    };

    /**
     * All configured content types for the Modula-2 source viewer.
     */
    public final static String[] ALL_CONFIGURED_CONTENT_TYPES = new String[] {
        M2_CONTENT_TYPE_DEFAULT, 
        M2_CONTENT_TYPE_BLOCK_COMMENT,
        M2_CONTENT_TYPE_END_OF_LINE_COMMENT,
        M2_CONTENT_TYPE_SINGLE_QUOTE_STRING,
        M2_CONTENT_TYPE_DOUBLE_QUOTE_STRING,
        M2_CONTENT_TYPE_PRAGMA,
        M2_CONTENT_TYPE_DISABLED_CODE
    };
    
}
