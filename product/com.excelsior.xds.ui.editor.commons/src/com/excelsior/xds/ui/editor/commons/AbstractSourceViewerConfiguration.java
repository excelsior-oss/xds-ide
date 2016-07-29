package com.excelsior.xds.ui.editor.commons;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public abstract class AbstractSourceViewerConfiguration extends
		TextSourceViewerConfiguration {
	
	public AbstractSourceViewerConfiguration() {
		super();
	}

	public AbstractSourceViewerConfiguration(IPreferenceStore preferenceStore) {
		super(preferenceStore);
	}

	/**
	 * Called in {@link SourceCodeTextEditor#refreshConfiguration()}.<br>
	 * Hook to refresh configuration, used for example to re-create cached {@link org.eclipse.swt.graphics.Color} or other SWT resources.
	 */
	public abstract void refresh();
}
