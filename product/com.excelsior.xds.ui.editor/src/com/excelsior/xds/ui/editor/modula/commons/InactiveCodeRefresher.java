package com.excelsior.xds.ui.editor.modula.commons;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;

import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.ui.editor.modula.ModulaFastPartitioner;

/**
 * Refreshes the source viewer representation of the source code using information about the inactive code regions from the AST.
 * @author lsa80
 */
public final class InactiveCodeRefresher {
	private final ModulaAst ast;
	private final ITextPresentation textPresentation;
	private final IDocumentProvider documentProvider;
	private final IInactiveCodeRefresherListener listener;
	
	public InactiveCodeRefresher(ModulaAst ast, ITextPresentation textPresentation,
			IDocumentProvider documentProvider) {
		this(ast, textPresentation, documentProvider, null);
	}

	public InactiveCodeRefresher(ModulaAst ast, ITextPresentation textPresentation,
			IDocumentProvider documentProvider, IInactiveCodeRefresherListener listener) {
		this.ast = ast;
		this.textPresentation = textPresentation;
		this.documentProvider = documentProvider;
		this.listener = listener;
	}

	public void refresh() {
		IDocument doc = documentProvider.getDocument();
		if (doc != null) {
    		final ModulaFastPartitioner partitioner = ModulaEditorCommons.getModulaFastPartitioner(doc);
    		if (partitioner != null) {
    			Display.getDefault().asyncExec(new Runnable() {
    				@Override
    				public void run() {
    					if (textPresentation.isDisposed()) {
    						return;
    					}
    					try {
    						partitioner.setModulaAst(ast);
    						DocumentEvent documentEvent = new DocumentEvent(doc, 0, doc.getLength(), doc.get());
    						partitioner.documentChanged(documentEvent);
    						textPresentation.invalidateTextPresentation();
    					} 
    					finally{
    						partitioner.setModulaAst(null);
    					}
    					
    					if (listener != null) {
    						listener.afterTextPresentationUpdated();
    					}
    				}
    			});
			}
    	}
	}

	/**
	 * Interface to source viewer widget
	 * @author lsa80
	 */
	public interface ITextPresentation {
		void invalidateTextPresentation();
		boolean isDisposed();
	}
	
	/**
	 * Supplier of the {@link IDocument}
	 * @author lsa80
	 */
	public interface IDocumentProvider {
		IDocument getDocument();
	}
	
	public interface IInactiveCodeRefresherListener {
		void afterTextPresentationUpdated();
	}
}
