package com.excelsior.xds.ui.editor.modula.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

public class ModulaContentViewerCreator implements IViewerCreator {
	public ModulaContentViewerCreator() {
	}

	@Override
	public Viewer createViewer(Composite parent, CompareConfiguration config) {
		config.setLeftEditable(false);
		config.setRightEditable(false);
		return new ModulaMergeViewer(parent, config);
	}
}
