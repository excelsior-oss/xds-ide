package com.excelsior.xds.ui.commons.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.MenuUtil;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.core.log.LogHelper;

@SuppressWarnings("restriction")
public final class WorkbenchUtils 
{
    /**
     * Fetches the editor currently in use. If the active editor is a multi page
     * editor and the param getSubEditor is true, then the editor of the active
     * page is returned instead of the multi page editor.
     * 
     * @param getSubEditor
     *            indicates that a sub editor should be returned
     * @return the active editor or <code>null</code> if no editor is active
     */
    public static IEditorPart getActiveEditor(boolean getSubEditor) {
        IWorkbenchPage page = getActivePage();
        if (page != null) {
            IEditorPart result = page.getActiveEditor();
            if (getSubEditor && (result instanceof FormEditor)) {
                result = ((FormEditor) result).getActiveEditor();
            }
            return result;
        }
        return null;
    }

    public static IWorkbenchPage getActivePage() {
        IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        return (w != null) ? w.getActivePage() : null;
    }
    
    public static IWorkbenchPart getActivePart() {
    	IWorkbenchPage activePage = getActivePage();
    	return activePage != null ? activePage.getActivePart() : null;
    }
    
    public static IWorkbenchPartSite getActivePartSite() {
    	IWorkbenchPart activePart = getActivePart();
    	return activePart != null ? activePart.getSite() : null;
    }
    
    public static ISelectionProvider getActivePartSelectionProvider() {
    	IWorkbenchPartSite site = getActivePartSite(); 
    	return site != null ? site.getSelectionProvider() : null;
    }
    
    /**
     * Fetches the text selection from the active text editor.
     * 
     * @return the text selection or <code>null</code>, when there is no active
     *         editor or it is not a text editor
     */
    public static ITextSelection getActiveTextSelection() {
        IEditorPart editor = getActiveEditor(true);
        if (editor instanceof ITextEditor) {
            ISelection selection = ((ITextEditor) editor)
                    .getSelectionProvider().getSelection();
            if (selection instanceof ITextSelection) {
                return (ITextSelection) selection;
            }
        }
        return null;
    }

    /**
     * Fetches the document provider for the active text editor.
     * 
     * @return the document provider or <code>null</code> if no active editor or
     *         the active editor is not a text editor
     */
    public static IDocumentProvider getActiveDocumentProvider() {
        IEditorPart editor = getActiveEditor(true);
        if (editor instanceof ITextEditor) {
            return ((ITextEditor) editor).getDocumentProvider();
        }
        return null;
    }
    
        /**
     * Fetches the input under edit.
     * 
     * @return the active input or <code>null</code>, when there is no active
     *         editor
     */
    public static IEditorInput getActiveInput() {
        IEditorPart editor = getActiveEditor(true);
        if (editor == null) {
            return null;
        }
        return editor.getEditorInput();
    }

    /**
     * Fetches the document just edited.
     * 
     * @return the document or <code>null</code>
     */
    public static IDocument getActiveDocument() {
        IDocumentProvider provider = getActiveDocumentProvider();
        IEditorInput input = getActiveInput();
        if ((provider != null) && (input != null)) {
            return provider.getDocument(input);
        }
        return null;
    }

    /**
     * Fetches the file represented in the active editor.
     * 
     * @return the file or <code>null</code>, when there is no active file
     * 
     *         NOTE that IFiles are meant to represent files within the
     *         workspace and will not work if the file that has been opened is
     *         not contained within the workspace.
     */
    public static IFile getActiveFile() {
        return getIFileFrom(getActiveInput());
    }
    
    /**
     * Tries to get IFile from editor input - i.e. the IFile which can be modified by this editor
     * @param input
     * @return
     */
    public static IFile getIFileFrom(IEditorInput input) {
    	if (input == null) {
    		return null;
    	}
    	
    	return (IFile) input.getAdapter(IFile.class);
    }
    
    /**
     * Fetches the currently visible status line manager. Note, that even if it
     * looks like Eclipse has just one statusline this is far from beeing the
     * case. Instead <em>every</em> editor and <em>every</em> view has it's own
     * status line instance, Eclipse switches visibility when focus changes.
     * This is important to remember when trying to clear a message, especially
     * an error message, which supresses display of normal messages: you must
     * remember the status line you have written the message to, else you might
     * clear messages in the wrong status line.
     * <p>
     * Note also, that the returned status line manager most often is a
     * SubStatusLineManager and there seem to be views (Taskview for instance)
     * that write some status to their status line and don't refreh it often,
     * trusting, that nobody else destroys their message. To solve this use:
     * 
     * <pre>
     *     IStatusLineManager statusLine = getStatusLine();
     *     while (statusLine instanceof SubStatusLineManager) {
     *         IContributionManager cb =
     *             ((SubStatusLineManager)statusLine).getParent();
     *         if (!(cb instanceof IStatusLineManager)) {
     *             break;
     *         }
     *         statusLine = (IStatusLineManager) cb;
     *     }
     *     statusLine.setMessage(...);
     * </pre>
     * 
     * @return the currently active status line manager
     */
    public static IStatusLineManager getStatusLine() {
        IEditorSite esite = getEditorSite(false);
        if (esite != null) {
            return esite.getActionBars().getStatusLineManager();
        }
        IViewSite vsite = getViewSite();
        if (vsite != null) {
            return vsite.getActionBars().getStatusLineManager();
        }
        return null;
    }
    
    /**
     * Fetches the editor site of the active editor. If the active editor is a
     * multi page editor and the param getSubEditor is true, then the editor
     * site of the active page is returned instead of the multi page editors
     * site.
     * 
     * @param getSubEditor
     *            indicates that a sub editor should be returned
     * @return the editor site or <code>null</code>
     */
    public static IEditorSite getEditorSite(boolean getSubEditor) {
        IEditorPart editor = getActiveEditor(getSubEditor);
        if (editor != null) {
            return editor.getEditorSite();
        }
        return null;
    }
    
    /**
     * Fetches the view site of the active view.
     * 
     * @return the site or <code>null</code>
     */
    public static IViewSite getViewSite() {
        IViewPart part = getActiveView();
        if (part != null) {
            return part.getViewSite();
        }
        return null;
    }
    
    /**
     * Fetches the active view.
     * 
     * @return the active view or <code>null</code> if no view is active
     */
    public static IViewPart getActiveView() {
        IWorkbenchPage page = getActivePage();
        if (page != null) {
            IWorkbenchPart part = page.getActivePart();
            if (part instanceof IViewPart) {
                return (IViewPart) part;
            }
        }
        return null;
    }
    
    /**
     * Remove any message set by us from the right statusline.
     */
    public static void clearStatusLine() {
        IStatusLineManager statusLine = getStatusLine();
        if (statusLine != null) {
            statusLine.setErrorMessage(null);
            statusLine.setMessage(null);
            statusLine = null;
        }
    }
    
    public static void reportToStatusLine(String message, boolean isError) {
        IStatusLineManager statusLine = getStatusLine();
        if (statusLine != null) {
            if (isError) {
                statusLine.setErrorMessage(message);
            }
            statusLine.setMessage(message);
        }
    }
    
    public static Shell getActivePartShell() {
        IWorkbenchPage activePage = WorkbenchUtils.getActivePage();
        if (activePage == null) {
        	return null;
        }
		IWorkbenchPart activePart = activePage.getActivePart();
		if (activePart == null) {
        	return null;
        }
		IWorkbenchPartSite site = activePart.getSite();
		if (site == null) {
        	return null;
        }
		return site.getShell();
    }
    
    public static Shell getWorkbenchWindowShell() {
		IWorkbenchWindow activeWindow = getActiveWorkbenchWindow();
		return activeWindow != null ? activeWindow.getShell() : null;

	}
    
    public static IWorkbenchWindow getActiveWorkbenchWindow() {
    	return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }

    public static boolean isCaseSensitiveFilesystem() {
        return Platform.OS_MACOSX.equals(Platform.getOS()) ? false : new java.io.File("a").compareTo(new java.io.File("A")) != 0; //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public static void refreshMainMenu() {
    	IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		WorkbenchWindow ww = (WorkbenchWindow)w;
    	IMenuService service = (IMenuService)w.getService(IMenuService.class);
    	service.populateContributionManager((ContributionManager)ww.getActionBars().getMenuManager(), MenuUtil.MAIN_MENU);
    }
    
    public static IMenuManager getWorkbenchMenuManager() {
    	IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		WorkbenchWindow ww = (WorkbenchWindow)w;
    	return ww.getActionBars().getMenuManager();
    }
    
    public static ISelectionService getSelectionService() {
    	IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    	if (w != null) {
    		return w.getSelectionService();
    	}
    	return null;
    }
    
    public static Object getService(Class<?> api) {
    	IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    	return  w.getService(api);
    }
    
    public static ICommandService getCommandService() {
    	return (ICommandService)getService(ICommandService.class);
    }
    
    public static ISourceProviderService getSourceProviderService() {
        return (ISourceProviderService) getService(ISourceProviderService.class);
    }

    /**
     * Retrieves a source provider providing the given source. This is used by
     * clients who only need specific sources.
     * 
     * @param sourceName  The name of the source; must not be <code>null</code>.
     * 
     * @return A source provider which provides the request source, or
     *         <code>null</code> if no such source exists.
     */    
    public static ISourceProvider getSourceProvider(String sourceName) {
        ISourceProviderService sourceProviderService = getSourceProviderService();
        if (sourceProviderService == null) {
        	return null;
        }
		return sourceProviderService.getSourceProvider(sourceName);
    }
    
    /** Returns selected XDS project or null
     *
     * @param projectNatureId - should be NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID
     * 
     * @return
     */
    public static IProject getCurrentXdsProject(String projectNatureId) {
        IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = (w != null) ? w.getActivePage() : null;
        IProject ipr = null;
        if (page != null) {
            // Search in selection set:
            ISelection selection = page.getSelection();
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection ss = (IStructuredSelection)selection;
                if (!ss.isEmpty()) {
                    Object obj = ss.getFirstElement();
                    if (obj instanceof IProject) {
                        ipr = (IProject)obj;
                    } else if (obj instanceof IResource) {
                        ipr = ((IResource)obj).getProject();
                    }
                }
            }
            if (!isXdsProject(ipr, projectNatureId)) {
                // No selections in the XDS project. May be current file is from XDS? :
                IEditorPart part = page.getActiveEditor();
                if (part != null) {
                    IEditorInput input = part.getEditorInput();
                    if (input != null) {
                        IFile ifile = (IFile) input.getAdapter(IFile.class);                                    
                        if (ifile != null) {
                            ipr = ifile.getProject();
                        }
                    }
                }
            }
        }
        return isXdsProject(ipr, projectNatureId) ? ipr : null;
    }
    
    public static IWorkbenchWindow[] getWorkbenchWindows() {
    	IWorkbench workbench = PlatformUI.getWorkbench();
    	 return workbench.getWorkbenchWindows();
    }
    
    public static IWorkbench getWorkbench() {
    	return PlatformUI.getWorkbench();
    }
    
    public static IViewPart showView(String viewId) {
    	IViewPart viewPart = null;
    	IWorkbenchPage page = getActivePage();
    	if (page != null) {
    		try {
    			viewPart = page.showView(viewId);
			} catch (PartInitException e) {
				LogHelper.logError(e);
			}
    	}
    	return viewPart;
    }

    // Returns false when no XDS or null
    private static boolean isXdsProject(IProject prj, String projectNatureId) {
        try {
            return prj != null && prj.isOpen() && prj.getNature(projectNatureId) != null;
        } catch (CoreException e) {
            LogHelper.logError(e);
        }
        return false;
    }
    
    /**
     * Opens property page corresponding to the {@code element}
     * @param shellProvider
     * @param element element to open property page for
     */
    public static void openProperties(IShellProvider shellProvider, final Object element) {
    	PropertyDialogAction action = new PropertyDialogAction( shellProvider, new ISelectionProvider() {

			public void addSelectionChangedListener( ISelectionChangedListener listener ) {
			}

			public ISelection getSelection() {
				return new StructuredSelection( element );
			}

			public void removeSelectionChangedListener( ISelectionChangedListener listener ) {
			}

			public void setSelection( ISelection selection ) {
			}
		} );
    	try{
    		action.run();
    	}
    	finally{
    		action.dispose();
    	}
    }
    
    private WorkbenchUtils() {
    }
}