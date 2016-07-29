package com.excelsior.xds.ui.editor.commons.contentassist;

import org.eclipse.jface.text.ITextViewer;

public class BaseCompletionContext {
	protected final ITextViewer viewer; 
	protected final int offset;
	
	public BaseCompletionContext(ITextViewer viewer, int offset) {
		this.viewer = viewer;
		this.offset = offset;
	}

	public ITextViewer getViewer() {
		return viewer;
	}

	public int getOffset() {
		return offset;
	}
}
