package com.excelsior.xds.core.text;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.internal.content.TextContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;

/**
 * This class provides common basis for text-based content describers
 * with encoding autodetect. 
 */

@SuppressWarnings("restriction")
public class TextEncodingContentDescriber extends TextContentDescriber {

	public TextEncodingContentDescriber() {
	}

	/*
     * Mod, def,... files encoding is determined here and in ModulaDocumentProvider.getPersistedEncoding().
     * 
     * This describe() is called when file is added to eclipse project from file system, not via XDS
     * compilation set list. The encoding determined here will be cached and getPersistedEncoding()
     * will return it as is when editor will be opened.
     */
    @Override
    public int describe(InputStream contents, IContentDescription description) throws IOException {
    	// here will be read only 1 byte - (used to define endianness)
        return super.describe(contents, description);
    }
}
