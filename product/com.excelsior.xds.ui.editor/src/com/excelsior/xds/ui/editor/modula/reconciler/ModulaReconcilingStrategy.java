package com.excelsior.xds.ui.editor.modula.reconciler;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.core.ide.symbol.ParseTask;
import com.excelsior.xds.core.ide.symbol.ParseTaskFactory;
import com.excelsior.xds.core.ide.symbol.SymbolModelManager;

public class ModulaReconcilingStrategy implements IReconcilingStrategy
                                                , IReconcilingStrategyExtension
{
    /** The text editor to operate on. */
    private final ITextEditor textEditor;

    /**
     * Creates a new Modula-2 reconciling strategy.
     *
     * @param editor the editor of the strategy's reconciler
     */
    public ModulaReconcilingStrategy(ITextEditor editor) {
        this.textEditor = editor;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocument(IDocument document) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProgressMonitor(IProgressMonitor monitor) {
//        progressMonitor = monitor;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void initialReconcile() {
    	 reconcileInternal();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void reconcile(IRegion partition) {
        reconcileInternal();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
        reconcileInternal();
    }
    
    /**
     * Schedules parse of the modified or recently opened editor.
     */
    private void reconcileInternal() {
    	ParseTask task = ParseTaskFactory.create(textEditor.getEditorInput());
    	task.setNeedModulaAst(true);
		SymbolModelManager.instance().scheduleParse(task, null);
    }

	public void aboutToBeReconciled() {
	}

    
    /**
     * Called when reconcile has finished.
     */
    public void reconciled() {
    }
}
