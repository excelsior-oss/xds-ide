package com.excelsior.xds.ui.editor.modula.outline;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.core.model.IXdsCompilationUnit;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsModule;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.ui.editor.internal.nls.Messages;

/**
 * The content providers for Modula-2 POutline viewers.
 * 
 * TODO use symbol interfaces instead of concrete implementations
 */
public class ModulaOutlinePageContentProvider implements ITreeContentProvider 
{
    private static final LoadingContentRoot LOADING_ROOT = new LoadingContentRoot(Messages.XdsOutlinePage_Loading);
    private final Object[] EMPTY_ARRAY = new Object[0];
    
    private IEditorInput input;
    private IXdsCompilationUnit compilationUnit;
    private ITextEditor editor;
    private ModulaOutlineFilter filter;
    private IDocument document;
    
    public ModulaOutlinePageContentProvider( ITextEditor editor
                                           , ModulaOutlineFilter filter )
    {
        super();
        this.editor = editor;
        this.filter = filter;
    }
    
    public IXdsModule getRoot() {
        IXdsModule root = null;

        retrieveCompilationUnit();
    	if (compilationUnit != null) {
            root = compilationUnit.getModuleElement();
    	}
    	
    	if (root == null) {
    	    root = LOADING_ROOT;
    	}
        
    	return root;
    }

    
    @Override // ITreeContentProvider
    public Object getParent(Object element)
    {
        if (element instanceof IXdsElement)
            return ((IXdsElement)element).getParent();
        return null;
    }

    
    @Override  // ITreeContentProvider
    public Object[] getElements(Object inputElement) {
        if (getRoot() != null) {
            Collection<IXdsElement> children = getRoot().getChildren();
            children = filter(children);
            if (children != null) {
                return children.toArray();
            }
        }
        return EMPTY_ARRAY;
    }

    private Collection<IXdsElement> filter(Collection<IXdsElement> elements) {
    	if (!elements.isEmpty()) {
    		elements = new ArrayList<IXdsElement>(elements);
    	}
        CollectionUtils.filter(elements, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                IXdsElement xdsElement = (IXdsElement)o;
                return filter.accept(xdsElement);
            }
        });
        return elements;
    }

    
    @Override // ITreeContentProvider
    public Object[] getChildren(Object parentElement) {
        if (parentElement == input) {
            parentElement = getRoot();
        }
        if (!(parentElement instanceof IXdsContainer)) {
        	return EMPTY_ARRAY;
        }
        
        Collection<IXdsElement> children = ((IXdsContainer)parentElement).getChildren();
        children = filter(children);
        return parentElement == null ? EMPTY_ARRAY : children.toArray();
    }

    @Override // ITreeContentProvider
    public boolean hasChildren(Object element)
    {
        if (element == input) { 
            return true;
        } else {
        	 if (!(element instanceof IXdsContainer)) {
        		 return false;
             }
        	
            Collection<IXdsElement> children = ((IXdsContainer)element).getChildren();
            children = filter(children);
            return children.size() > 0;
        }
    }
    

    @Override // ITreeContentProvider
    public void dispose() {
    }
    

    @Override // ITreeContentProvider
    public void inputChanged(final Viewer viewer, Object oldInput, Object newInput)
    {
        input = (IEditorInput) newInput;
        compilationUnit = null;
    }

	/**
	 * Gets compilation unit from the model. This done because of external
	 * (non-workspace) elements, which are not in the model from the startup
	 * (until moment they are opened in the editor).
	 */
	private void retrieveCompilationUnit() {
		if (compilationUnit == null) {
			document = editor.getDocumentProvider().getDocument(input);
			if (document != null)
			{
				IXdsElement xdsElement =  XdsModelManager.getModel().getXdsElement(input);
				if (xdsElement instanceof IXdsCompilationUnit) {
					compilationUnit = (IXdsCompilationUnit)xdsElement;
				}
			}
		}
	}
	
}
