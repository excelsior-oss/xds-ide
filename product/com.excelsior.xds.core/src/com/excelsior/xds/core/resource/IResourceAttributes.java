package com.excelsior.xds.core.resource;

import org.eclipse.core.runtime.QualifiedName;

public class IResourceAttributes {
	public static final String LINKED_RESOURCE_ORIGINAL_PATH_ATTR_NAME = "LINKED_RESOURCE_ORIGINAL_PATH"; //$NON-NLS-1$
	/**
	 * Synthetic Folder maps to the actual folder at the File System. However, it may have less number of children than its actual File System counterpart. 
	 */
	public static final String SYNTHETIC_RESOURCE_FLAG = "SYNTHETIC_RESOURCE_FLAG";
	public static final String BASE_ENCODING = "ENCODING"; //$NON-NLS-1$
	public static final String POSTFIX_ENCODING_DETERMINED = "DETERMINED"; //$NON-NLS-1$
	
	public static final QualifiedName ENCODING_DETERMINED = new QualifiedName(BASE_ENCODING, POSTFIX_ENCODING_DETERMINED);
}
