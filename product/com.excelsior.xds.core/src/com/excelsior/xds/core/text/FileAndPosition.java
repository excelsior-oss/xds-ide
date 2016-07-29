package com.excelsior.xds.core.text;

import java.io.File;

/**
 * The file and {@link TextPosition} inside
 * @author lsa
 */
public class FileAndPosition {
	private final File file;
	private final TextPosition textPosition;
	
	public FileAndPosition(File file, TextPosition textPosition) {
		this.file = file;
		this.textPosition = textPosition;
	}

	public File getFile() {
		return file;
	}

	public TextPosition getTextPosition() {
		return textPosition;
	}
}