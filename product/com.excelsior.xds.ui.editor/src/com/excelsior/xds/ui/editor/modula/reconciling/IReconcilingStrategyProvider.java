package com.excelsior.xds.ui.editor.modula.reconciling;

import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;

public interface IReconcilingStrategyProvider {
	IReconcilingStrategy createReconcilingStrategy(ISourceViewer viewer);
}
