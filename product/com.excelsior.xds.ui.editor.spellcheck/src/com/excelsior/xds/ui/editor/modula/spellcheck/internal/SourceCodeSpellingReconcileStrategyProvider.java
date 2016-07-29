package com.excelsior.xds.ui.editor.modula.spellcheck.internal;

import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;

import com.excelsior.xds.ui.editor.modula.reconciling.IReconcilingStrategyProvider;

public class SourceCodeSpellingReconcileStrategyProvider implements
		IReconcilingStrategyProvider {

	public SourceCodeSpellingReconcileStrategyProvider() {
	}

	@Override
	public IReconcilingStrategy createReconcilingStrategy(ISourceViewer viewer) {
		return new SourceCodeSpellingReconcileStrategy(viewer);
	}
}