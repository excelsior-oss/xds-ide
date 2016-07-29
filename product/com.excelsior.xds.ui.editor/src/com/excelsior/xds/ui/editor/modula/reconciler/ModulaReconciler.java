package com.excelsior.xds.ui.editor.modula.reconciler;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import com.excelsior.xds.ui.editor.commons.SourceCodeTextEditor;

public class ModulaReconciler extends MonoReconciler
{
    /** The property change listener. */
    private IPropertyChangeListener propertyChangeListener;

    /** The mutex that keeps us from running multiple reconcilers on one editor. */
    private Object fMutex;
    
    private boolean ininitalProcessDone = false;
    
    /**
     * Creates a new reconciler.
     *
     * @param editor the editor
     * @param strategy the reconcile strategy
     * @param isIncremental <code>true</code> if this is an incremental reconciler
     */
    public ModulaReconciler(ITextEditor editor, ModulaCompositeReconcilingStrategy strategy) {
        super(strategy, false);
        
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=63898
        // when re-using editors, a new reconciler is set up by the source viewer
        // and the old one uninstalled. However, the old reconciler may still be
        // running.
        // To avoid having to reconcilers calling SourceCodeTextEditor.reconciled,
        // we synchronized on a lock object provided by the editor.
        // The critical section is really the entire run() method of the reconciler
        // thread, but synchronizing process() only will keep JavaReconcilingStrategy
        // from running concurrently on the same editor.
        // TODO remove once we have ensured that there is only one reconciler per editor.
        if (editor instanceof SourceCodeTextEditor)
            fMutex= ((SourceCodeTextEditor) editor).getReconcilerLock();
        else
            fMutex= new Object(); // Null Object
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void install(ITextViewer textViewer) {
        super.install(textViewer);
        
        propertyChangeListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                boolean isEpeelingEnambed = SpellingService.PREFERENCE_SPELLING_ENABLED.equals(event.getProperty())
                                         || SpellingService.PREFERENCE_SPELLING_ENGINE.equals(event.getProperty());
                if (isEpeelingEnambed) {
                    forceReconciling();
                }
            }
        };
        EditorsUI.getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void uninstall() {
        EditorsUI.getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
        propertyChangeListener = null;

        super.uninstall();
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialProcess() {
        synchronized (fMutex) {
            super.initialProcess();
        }
        ininitalProcessDone = true;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void forceReconciling() {
        if (ininitalProcessDone) {
            super.forceReconciling();
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void aboutToBeReconciled() {
        ModulaCompositeReconcilingStrategy strategy = (ModulaCompositeReconcilingStrategy) getReconcilingStrategy(IDocument.DEFAULT_CONTENT_TYPE);
        strategy.aboutToBeReconciled();
    }
    
}
