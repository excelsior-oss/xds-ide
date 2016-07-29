package com.excelsior.xds.ui.editor.modula.reconciler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.ui.editor.commons.reconciler.CompositeReconcilingStrategy;
import com.excelsior.xds.ui.editor.modula.reconciling.IReconcilingStrategyProvider;

public class ModulaCompositeReconcilingStrategy extends CompositeReconcilingStrategy
{
    /** Properties file content type */

    private ModulaReconcilingStrategy modulaStrategy; 
    
    /**
     * Creates a new Modula-2 reconciling strategy.
     *
     * @param viewer the source viewer
     * @param editor the editor of the strategy's reconciler
     */
    public ModulaCompositeReconcilingStrategy(ISourceViewer viewer, ITextEditor editor) {
        modulaStrategy = new ModulaReconcilingStrategy(editor);
        List<IReconcilingStrategyProvider> providers = ModulaEditorReconcilingStrategyContributionRegistry.get().contributions();
        List<IReconcilingStrategy> contributions = new ArrayList<IReconcilingStrategy>();
        contributions.add(modulaStrategy);
		providers.stream().map(p -> p.createReconcilingStrategy(viewer))
				.forEach(s -> contributions.add(s));
        setReconcilingStrategies(contributions.toArray(new IReconcilingStrategy[0]));
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
        try {
            super.reconcile(dirtyRegion, subRegion);
        } finally {
            reconciled();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void reconcile(IRegion partition) {
        try {
            super.reconcile(partition);
        } finally {
            reconciled();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialReconcile() {
        try {
            super.initialReconcile();
        } finally {
            reconciled();
        }
    }

        
    /**
     * Called before reconciling is started.
     */
    public void aboutToBeReconciled() {
        modulaStrategy.aboutToBeReconciled();

    }
    
    /**
     * Called when reconcile has finished.
     */
    private void reconciled() {
        modulaStrategy.reconciled();
    }
    
}
