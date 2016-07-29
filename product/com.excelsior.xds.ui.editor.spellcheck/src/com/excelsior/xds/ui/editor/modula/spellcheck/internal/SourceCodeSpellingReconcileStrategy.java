package com.excelsior.xds.ui.editor.modula.spellcheck.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;

import com.excelsior.xds.core.XdsCorePlugin;

/**
 * Reconcile strategy for spell checking comments.
 */
public class SourceCodeSpellingReconcileStrategy extends SpellingReconcileStrategy 
{
    /** The content type of the underlying editor input */
    private IContentType contentType;
    
    /**
     * Creates a new comment reconcile strategy.
     *
     * @param viewer the source viewer
     * @param editor the text editor to operate on
     */
    public SourceCodeSpellingReconcileStrategy(ISourceViewer viewer) {
        super(viewer, EditorsUI.getSpellingService());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IContentType getContentType() {
    	if (contentType == null) {
    		contentType = Platform.getContentTypeManager().getContentType(XdsCorePlugin.CONTENT_TYPE_XDS_SOURCE);
    	}
        return contentType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reconcile(IRegion region) {
        if (isSpellingEnabled()) {
            super.reconcile(region);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
        if (isSpellingEnabled()) {
            super.reconcile(dirtyRegion, subRegion);
        }
    }
    
    
    private boolean isSpellingEnabled() {
        return true;
    }
    
}
