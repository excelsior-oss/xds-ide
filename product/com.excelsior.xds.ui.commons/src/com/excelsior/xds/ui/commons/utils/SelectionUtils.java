package com.excelsior.xds.ui.commons.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.excelsior.xds.core.utils.AdapterUtilities;

public final class SelectionUtils 
{
    /**
     * Returns the current selection in the active part.  If the selection in the
     * active part is <em>undefined</em> (the active part has no selection provider)
     * the result will be <code>null</code>.
     *
     * @return the current selection, or <code>null</code> if undefined
     */
	 public static IStructuredSelection getStructuredSelection() {
		 ISelectionService selectionService = WorkbenchUtils.getSelectionService();
		 if (selectionService != null) {
			 ISelection selection = selectionService.getSelection();
			 if (selection instanceof IStructuredSelection) {
				 return (IStructuredSelection)selection;
			 }
		 }
		 return null;
	 }
	 
	 /**
	  * Returns first element of the {@link #getObjectsFromStructuredSelection(ISelection, Class)} 
	  * @see {@link #getObjectsFromStructuredSelection(ISelection, Class)}
	 */
	public static <T> T getObjectFromStructuredSelection( ISelection selection
             , Class<T> tagClass ) 
	 {
		 List<T> objects = getObjectsFromStructuredSelection(selection, tagClass);
		 if (objects.isEmpty()) {
			 return null;
		 }
		 return objects.get(0);
	 }
	
	/**
	  * Uses selection {@link getStructuredSelection()} and passes it to the {@link #getObjectFromStructuredSelection(ISelection, Class)}
	 * @param tagClass
	 * @return
	 * @see #getObjectFromStructuredSelection(ISelection, Class)
	 * @see #getStructuredSelection()
	 */
	public static <T> T getObjectFromStructuredSelection( Class<T> tagClass ) {
		 return getObjectFromStructuredSelection( getStructuredSelection(), tagClass);
	}
	
	 /**
	  * Uses selection {@link getStructuredSelection()} and passes it to the {@link #getObjectsFromStructuredSelection(ISelection, Class)}
	 * @param tagClass
	 * @return
	 * @see #getObjectsFromStructuredSelection(ISelection, Class)
	 * @see #getStructuredSelection()
	 */
	public static <T> List<T> getObjectsFromStructuredSelection( Class<T> tagClass ){
		 return getObjectsFromStructuredSelection(getStructuredSelection(), tagClass);
	}
	
    /**
     * Returns the selected elements of the given class as a unmodifiable list.
     *
     * @param selection - a selection containing elements.
     * @param tagClass - expected class of elements in selection - only objects of this class will be returned 
     * @return the selected elements of given class as a immutable list 
     */
    public static <T> List<T> getObjectsFromStructuredSelection( ISelection selection
                                                               , Class<T> tagClass ) 
    {
        List<T> objectList = Collections.emptyList(); 
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            objectList = new ArrayList<T>(structuredSelection.size());
            Iterator<?> selectionIterator = structuredSelection.iterator();
            while (selectionIterator.hasNext()) {
                Object object = selectionIterator.next();
                T adapted = AdapterUtilities.getAdapter(object, tagClass);
                if (adapted != null){
                	objectList.add(adapted);
                }
            }
        }
        return objectList;
    }

    
    private static final String tokenExpression = "[\\s\\p{Punct}&&[^_]]"; // whitespaces and punctuation except _ symbol //$NON-NLS-1$
    
    /**
     * Retrieves the word (sequence of symbols between the whitespace and punctuation) under cursor, 
     * or previous in the document, if <code>isPrevious</code> is <code>true</code>.
     */
    public static WordAndRegion getWordUnderCursor(IDocument document, boolean isPrevious, ITextSelection selection) {
        return getWordUnderCursor(document, isPrevious, selection, false); 

    }

     /**
     * Special for keyword "@ARG" in debug script language
     */
    public static WordAndRegion getWordUnderCursor(IDocument document, boolean isPrevious, ITextSelection selection, boolean withSobaka) 
    {
        try {
            boolean isPunctUnderCursor = document.get(selection.getOffset(), 1).matches(tokenExpression);
            int right = selection.getOffset();
            if (!isPunctUnderCursor) {
                for (int i = right + 1; i < document.getLength(); i++) {
                    if (document.get(i, 1).matches(tokenExpression)) break;
                    right = i;
                }
            }
            else {
                right--;
            }
            int left = selection.getOffset();
            for (int i = left - 1; i > -1; i--) {
                if (document.get(i, 1).matches(tokenExpression)) break;
                left = i;
            }
            String word = document.get(left, right - left + 1);
            if (word.length() == 0                     // empty word OR ... 
                    || word.matches(tokenExpression+"+")) { // ... only punctuation/spaces //$NON-NLS-1$
                return null;
            }
            if (withSobaka && left>0) {
                if ("@".equals(document.get(left-1, 1))) {
                    --left;
                    word = "@" + word;
                }
            }
            return new WordAndRegion(word, left, right - left + 1, selection.getOffset());
        } catch (BadLocationException e) {
        }
        return null;
    }
    
//    public static CompletionContext getCompletionContext(ITextViewer viewer,
//			int offset) {
//		CompletionContext completionContext = new CompletionContext();
//		IDocument doc = viewer.getDocument();
//		
//		int i = offset;
//		
//		try {
//			StringBuilder sb = new StringBuilder();
//			char c;
//			while(i < doc.getLength() && Character.isJavaIdentifierPart(c = doc.getChar(i))) {
//				sb.append(c);
//				++i;
//			}
//			
//			String afterCursorPart = sb.toString();
//			sb = new StringBuilder();
//			i = offset - 1;
//			while(i > -1 && Character.isJavaIdentifierPart(c = doc.getChar(i))) {
//				sb.append(c);
//				--i;
//			}
//			completionContext.beforeCursorWordPart = StringUtils.reverse(sb.toString());
//			
//			completionContext.currentWord = completionContext.beforeCursorWordPart + afterCursorPart;
//			if (completionContext.beforeCursorWordPart.isEmpty()) {
//				completionContext.beforeCursorWordPart = null;
//			}
//			
//			while(i > -1 && Character.isWhitespace(doc.getChar(i))) {
//				--i;
//			}
//			
//			if (doc.getChar(i) == '.') {
//				completionContext.isDotBeforeCursor = true;
//			}
//			
//			if (completionContext.isDotBeforeCursor) {
//				--i;
//				while(i > -1 && Character.isWhitespace(doc.getChar(i))) {
//					--i;
//				}
//				
//				sb = new StringBuilder();
//				while(i > -1 && Character.isJavaIdentifierPart(c = doc.getChar(i))) {
//					sb.append(c);
//					--i;
//				}
//				
//				completionContext.wordBeforeDot = StringUtils.reverse(sb.toString());
//				if (completionContext.wordBeforeDot.length() > 0) {
//					completionContext.wordBeforeDotOffset = i + 1; 
//				}
//			}
//		} catch (BadLocationException e) {
//		}
//		
//		return completionContext;
//	}
    
    /**
     * Retrieves the word (sequence of symbols between the whitespace and punctuation)
     * under cursor in the active text editor.
     * 
     * @param isPrevious retrieves the previous word.
     * 
     * @return the word description or <code>null</code>
     */
    public static WordAndRegion getWordUnderCursor (boolean isPrevious) {
        return getWordUnderCursor (isPrevious, false);
    }
    
    /**
     * Special for keyword "@ARG" in debug script language
     */
    public static WordAndRegion getWordUnderCursor (boolean isPrevious, boolean withSobaka) {
        ITextSelection selection = WorkbenchUtils.getActiveTextSelection();
        if (selection != null) {
            IDocument document = WorkbenchUtils.getActiveDocument();
            if (document != null) {
                return getWordUnderCursor(document, isPrevious, selection, withSobaka);
            }
        }
        return null;
    }
    
    /**
     * Returns the list of currently selected resources in the active workbench window,
     * or empty list if none. If an editor is active, the resource adapter
     * associated with the editor is returned.
     * 
     * @return the list of selected <code>IResource</code> objects, or empty list if none.
     */
    public static List<IResource> getSelectedResources() {
        if (PlatformUI.getWorkbench().getDisplay().isDisposed()) {
            return Collections.emptyList();
        }
        if (PlatformUI.getWorkbench().getDisplay().getThread().equals(Thread.currentThread())) {
            return getSelectedResources0();
        } 
        else {
            @SuppressWarnings("unchecked")
            final List<IResource>[] resources = new List[1];
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                public void run() {
                    resources[0] = getSelectedResources0();
                }
            });
            return resources[0];
        }
    }
    
    /**
     * Underlying implementation of <code>getSelectedResources</code>
     * 
     * @return the list of selected <code>IResource</code> objects, or empty list if none.
     */
    protected static List<IResource> getSelectedResources0() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        List<IResource> resources = new ArrayList<IResource>();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                IWorkbenchPart part = page.getActivePart();
                if (part instanceof IEditorPart) {
                    IEditorPart epart = (IEditorPart) part;
                    IResource adaptee = (IResource) epart.getEditorInput().getAdapter(IResource.class);
                    if (adaptee != null) {
                    	resources.add(adaptee);
                    }
                }
                else if (part != null) {
                    IWorkbenchPartSite site = part.getSite();
                    if(site != null) {
                        ISelectionProvider provider = site.getSelectionProvider();
                        if (provider != null) {
                            ISelection selection = provider.getSelection();
                            resources = getObjectsFromStructuredSelection(selection, IResource.class);
                        }
                    }
                }
            }
        }
        return resources;
    }
    

    /**
     * Checks that the currently selected resources only and all selected 
     * resources are siblings.
     *   
     * @return <tt> true</tt> if selected resources are siblings. 
     */
    public static boolean isSelectedSiblingResources() {
        IStructuredSelection structuredSelection = getStructuredSelection();
        List<IResource> selectedResources = SelectionUtils.getObjectsFromStructuredSelection(structuredSelection, IResource.class);
        boolean isResourceSelected = !selectedResources.isEmpty() 
                                && (structuredSelection.size() == selectedResources.size()); 
        if (isResourceSelected) {
            IResource firstParent = selectedResources.get(0).getParent();
            if (firstParent == null) {
                return false;
            }
            for (IResource resource : selectedResources) {
                IResource parent = resource.getParent();
                if ((parent != null) && (!parent.equals(firstParent))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
 
    /**
     * This class in static methods only
     */
    private SelectionUtils() {
    }
}