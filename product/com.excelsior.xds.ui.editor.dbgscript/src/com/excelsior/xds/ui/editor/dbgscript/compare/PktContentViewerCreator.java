package com.excelsior.xds.ui.editor.dbgscript.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

public class PktContentViewerCreator implements IViewerCreator {

	public PktContentViewerCreator() {
	}

	@Override
	public Viewer createViewer(Composite parent, CompareConfiguration config) {
		config.setLeftEditable(false);
		config.setRightEditable(false);
		return new PktMergeViewer(parent, config);
	}
}
