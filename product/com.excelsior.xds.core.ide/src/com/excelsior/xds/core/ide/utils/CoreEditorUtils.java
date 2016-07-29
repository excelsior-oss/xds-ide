package com.excelsior.xds.core.ide.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.ui.sourcelookup.CommonSourceNotFoundEditorInput;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.WorkbenchPartLabelProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiEditorInput;

import com.excelsior.xds.core.exceptions.ExceptionHelper;
import com.excelsior.xds.core.filesystems.history.HistoryFs;
import com.excelsior.xds.core.help.IXdsHelpContextIds;
import com.excelsior.xds.core.ide.editor.XdsEditorConstants;
import com.excelsior.xds.core.ide.editor.input.StorageEditorInput;
import com.excelsior.xds.core.ide.internal.nls.Messages;
import com.excelsior.xds.core.ide.plugin.IdeCorePlugin;
import com.excelsior.xds.core.ide.symbol.ParseTaskFactory;
import com.excelsior.xds.core.ide.symbol.SymbolModelManager;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.utils.AdapterUtilities;
import com.excelsior.xds.core.utils.BuilderUtils;
import com.excelsior.xds.core.utils.XdsFileUtils;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;
import com.excelsior.xds.ui.commons.utils.HelpUtils;
import com.excelsior.xds.ui.commons.utils.SwtUtils;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;

public final class CoreEditorUtils 
{
	/**
	 * This class can contain only static methods
	 */
	private CoreEditorUtils(){
	}
	
	public static IFileStore editorInputToFileStore(IEditorInput input) {
		if (input == null) 
		    return null;
		
		IFileStore result = null;
		
        if (input instanceof IFileEditorInput) {
        	IFile file = editorInputToIFile(input);
            if (file != null) {
            	result =  ResourceUtils.toFileStore(file);
            }
        }
        else if (input instanceof IURIEditorInput) {
            IURIEditorInput uriEditorInput = (IURIEditorInput) input;
            result =  ResourceUtils.toFileStore(uriEditorInput.getURI());
        }
        else if (input instanceof CommonSourceNotFoundEditorInput || input instanceof CompareEditorInput) {
        	// do nothing
        	 return null;
		}
        else if (input instanceof IStorageEditorInput) {
        	IStorageEditorInput storageEditorInput = (IStorageEditorInput)input;
        	IFileRevision state = AdapterUtilities.getAdapter(storageEditorInput, IFileRevision.class);
        	if (state != null) {
        		result = ResourceUtils.toFileStore(HistoryFs.toURI(state));
        	}
        }
        
        if (result == null){
        	LogHelper.logError("Unknown editor input");
        }
        
        return result;
	}
	
    /**
     * Returns File underlying this editor input. 
     * 
     * This method should be modified, whenever new editor input is supported 
     * for some editor requiring access to File.
     * 
     * @param input the editor input to operate on.
     * 
     * @return file underlying given editor input.
     */    
    public static File editorInputToFile(IEditorInput input) {
		if (input == null) 
		    return null;
		
        if (input instanceof IFileEditorInput) {
        	IFile file = editorInputToIFile(input);
            if (file != null) {
                return ResourceUtils.getAbsoluteFile(file);
            }
        }
        else if (input instanceof IURIEditorInput) {
            IURIEditorInput uriEditorInput = (IURIEditorInput) input;
            return new File(uriEditorInput.getURI());
        }
        else if (input instanceof CommonSourceNotFoundEditorInput || input instanceof CompareEditorInput || input instanceof IStorageEditorInput) {
        	// do nothing
		}
        else{
        	LogHelper.logError("Unknown editor input");
        }
        
        return null;
	}
    
    public static ParsedModuleKey editorInputToParsedModuleKey(IEditorInput input) {
    	if (input == null) 
		    return null;
    	
    	ParsedModuleKey key = null;
    	
    	if (input instanceof IFileEditorInput) {
        	IFile file = editorInputToIFile(input);
            if (file != null) {
            	key = ParsedModuleKeyUtils.create(file);
            }
        }
        else {
        	IFileStore fileStore = editorInputToFileStore(input);
        	if (fileStore != null) {
        		key = new ParsedModuleKey(fileStore);
        	}
        }
    	
    	if (key == null) {
    		LogHelper.logError("Unknown editor input");
    	}
    	
    	return key;
    }
    
    public static IFile editorInputToIFile(IEditorInput input) {
        return AdapterUtilities.getAdapter(input, IFile.class);
    }
    
    public static IProject getIProjectFrom(IEditorInput input) {
        IProject project = null;
        IFile file = editorInputToIFile(input);
        if (file != null) {
            project = file.getProject();
        }
        return project;
    }

    public static String getAssociatedEditorId(String resourceName) {
		String id = null;
		if (XdsFileUtils.isModulaProgramModuleFile(resourceName)) {
    		id = XdsEditorConstants.MODULA_PROGRAM_MODULE_EDITOR_ID;
    	}
    	else if (XdsFileUtils.isModulaDefinitionModuleFile(resourceName)) {
    		id = XdsEditorConstants.MODULA_DEFINITION_MODULE_EDITOR_ID;
    	}
    	else if (XdsFileUtils.isOberonModuleFile(resourceName)) {
    		id = XdsEditorConstants.OBERON_MODULE_EDITOR_ID;
    	}
    	else if (XdsFileUtils.isOberonDefinitionModuleFile(resourceName)) {
    		id = XdsEditorConstants.OBERON_DEFINITION_MODULE_EDITOR_ID;
    	}
    	return id;
	}
    
    public static final boolean isXdsEditor(String id) {
		return XdsEditorConstants.XDS_EDITOR_IDS.contains(id);
	}
    
    public static final boolean isXdsProgramEditor(String id) {
		return XdsEditorConstants.XDS_PROGRAM_MODULE_EDITOR_IDS.contains(id);
	}
    
    /**
     * Returns the given editor's input as XDS element.
     *
     * @param editor the editor
     * 
     * @return the given editor's input as <code>IXdsElement</code> or <code>null</code> if none
     */
    public static IXdsElement getEditorInputXdsElement(IEditorPart editor) 
    {
        Assert.isNotNull(editor);
        return getEditorInputXdsElement(editor.getEditorInput());
    }

    public static IXdsElement getEditorInputXdsElement(IEditorInput editorInput) 
    {
        if (editorInput != null) {
            if (editorInput instanceof IFileEditorInput) {
                IFile file = ((IFileEditorInput)editorInput).getFile();
                return XdsModelManager.getModel().getXdsElement(file);
            }
        }
        return null;
    }
    
    /**
     * TODO : we will need to implement this is if Project Exlplorer will have elements like procedures under 
     * file node. In this case, we would need to find out what file this particular procedure corresponds to.
     */
    public static IEditorPart findEditor(Object inputElement, boolean activate) {
            if (inputElement instanceof IXdsElement) {
                    
            }

            return null;
    }
    
    public static String getEditorID(IEditorInput input) throws PartInitException {
        Assert.isNotNull(input);
        IEditorDescriptor editorDescriptor;
        if (input instanceof IFileEditorInput)
            editorDescriptor= IDE.getEditorDescriptor(((IFileEditorInput)input).getFile());
        else {
            editorDescriptor= IDE.getEditorDescriptor(input.getName());
        }
        return editorDescriptor.getId();
    }
    
    /**
     * Saves all editors if "save automatically before build" is set in preferences
     * 
     * @return <code>true</code> if save was successful and refactoring can proceed;
     *          false if the refactoring must be cancelled
     */
	public static boolean saveEditors(boolean isPrompt) {
        if (Display.getCurrent() == null) {
            // If it is not UI thread - run us in UI thread
            final boolean res[] = {false};
            Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                res[0] = saveEditors(isPrompt);
            }
            });
            return res[0];
        }
        Shell shell = SwtUtils.getDefaultShell();
        
        List<IEditorPart> dirtyEditors = getDirtyEditors(true);
        if (dirtyEditors.isEmpty()) {
            return true;
        }
        
		if (isPrompt) {
			dirtyEditors = askSaveAllDirtyEditors(shell, dirtyEditors);
		}
    
        final List<IEditorPart> listToSave = dirtyEditors;
        if (listToSave == null) {
                return false; // Cancelled
        }
        try {
                // Save
                boolean autoBuild= BuilderUtils.setAutoBuilding(false);
                try {
                        IRunnableWithProgress runnable= new IRunnableWithProgress() {
                                public void run(IProgressMonitor pm) throws InterruptedException {
                                        int count= listToSave.size();
                                        pm.beginTask("", count); //$NON-NLS-1$
                                        for (int i= 0; i < count; i++) {
                                                listToSave.get(i).doSave(new SubProgressMonitor(pm, 1));
                                                if (pm.isCanceled())
                                                        throw new InterruptedException();
                                        }
                                        pm.done();
                                }
                                
                        };
                        try {
                                PlatformUI.getWorkbench().getProgressService().runInUI(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), runnable, null);
                        } catch (InterruptedException e) {
                                return false;
                        } catch (InvocationTargetException e) {
                                return false;
                        }
                } finally {
                        BuilderUtils.setAutoBuilding(autoBuild);
                }
                return true;
        } catch (CoreException e) {
                return false;
        }
    }
    
    /**
     * Opens a resizable dialog listing possible files to save, the user can select none, 
     * some or all of the files before pressing OK.
     *
     * @param shell
     * @param dirtyEditors
     * @return
     */
    private static List<IEditorPart> askSaveAllDirtyEditors(Shell shell, List<IEditorPart> dirtyEditors) {

        ListSelectionDialog dlg = new ListSelectionDialog(
            shell, dirtyEditors,
            new ArrayContentProvider(),
            new WorkbenchPartLabelProvider(), Messages.SaveEditorsDialog_Message+':') 
            {
                protected int getShellStyle() {
                    return super.getShellStyle() | SWT.SHEET;
                }
                protected void configureShell(Shell shell) {
                    super.configureShell(shell);
                    HelpUtils.setHelp(shell, IXdsHelpContextIds.SAVE_AND_BUILD_DIALOG);
                }
            };
        dlg.setInitialSelections(dirtyEditors.toArray());
        dlg.setTitle(Messages.SaveEditorsDialog_Title);
        int result = dlg.open();
        
        List<IEditorPart> res = null;
        if (result != IDialogConstants.CANCEL_ID) {
            Object[] objs = dlg.getResult(); 
            res = new ArrayList<IEditorPart>(objs.length);
            for (Object o : objs) {
                res.add((IEditorPart)o);
            }
        }
        return res;
    }
    
    public static List<IEditorPart> getDirtyEditors(boolean skipNonResourceEditors) {
        Set<IEditorInput> inputs= new HashSet<IEditorInput>();
        List<IEditorPart> result= new ArrayList<IEditorPart>(0);
        IWorkbench workbench= PlatformUI.getWorkbench();
        IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
        for (int i= 0; i < windows.length; i++) {
                IWorkbenchPage[] pages= windows[i].getPages();
                for (int x= 0; x < pages.length; x++) {
                        IEditorPart[] editors= pages[x].getDirtyEditors();
                        for (int z= 0; z < editors.length; z++) {
                                IEditorPart ep= editors[z];
                                IEditorInput input= ep.getEditorInput();
                                if (inputs.add(input)) {
                                        if (!skipNonResourceEditors || isResourceEditorInput(input)) {
                                                result.add(ep);
                                        }
                                }
                        }
                }
        }
        return result;
    }
    
    /**
     * @param input either {@link IFile},{@link IAdaptable},{@link IStorage}
     * @return
     */
    public static IEditorInput getEditorInput(Object input) {
        if (input instanceof IFile) {
            return new FileEditorInput((IFile) input);
        }
        else if (input instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable)input;
            IFile adapted = (IFile) adaptable.getAdapter(IFile.class);
            if (adapted != null) {
                return new FileEditorInput((IFile)adapted);
            }
        }
        else if (input instanceof IStorage) {
            return new StorageEditorInput((IStorage)input){
                @Override
                public boolean exists() {
                    return true; // TODO fix it
            }};
        }

        return null;
    }
    
    /**
     * Tests if a element is currently shown in an editor
     *
     * @param inputElement the input element
     * @return the IEditorPart if shown, null if element is not open in an editor
     */
    public static IEditorPart isOpenInEditor(Object inputElement) {
        IEditorPart editor= CoreEditorUtils.findEditor(inputElement, false);
        if (editor != null)
            return editor;

        IEditorInput input= getEditorInput(inputElement);

        if (input != null) {
            IWorkbenchPage p= WorkbenchUtils.getActivePage();
            if (p != null) {
                return p.findEditor(input);
            }
        }

        return null;
    }
    
	private static IEditorPart openInEditor(IEditorInput input,
			String editorID, boolean activate) throws CoreException {
		Assert.isNotNull(input);
		Assert.isNotNull(editorID);

		IWorkbenchPage p = WorkbenchUtils.getActivePage();
		if (p == null)
			ExceptionHelper.throwCoreException(IdeCorePlugin.PLUGIN_ID, "JavaEditorMessages.EditorUtility_no_active_WorkbenchPage"); //$NON-NLS-1$

		return p.openEditor(input, editorID, activate);
	}
    
    /**
     * Opens the editor currently associated with the given element (IFile, IStorage...)
     *
     * @param inputElement the input element
     * @param activate <code>true</code> if the editor should be activated
     * 
     * @return an open editor or <code>null</code> if an external editor was opened
     * 
     * @throws PartInitException if the editor could not be opened or the input element is not valid
     * Status code {@link IJavaStatusConstants#EDITOR_NO_EDITOR_INPUT} if opening the editor failed as
     * no editor input could be created for the given element.
     */
    public static IEditorPart openInEditor( Object inputElement, boolean activate
                                          ) throws CoreException 
    {
        if (inputElement instanceof IFile){
        	return openInEditor((IFile) inputElement, activate);
        }

        IEditorInput input = getEditorInput(inputElement);
        if (input == null)
        	ExceptionHelper.throwCoreException(IdeCorePlugin.PLUGIN_ID, "Could not get an editor input for the given element", -1); //$NON-NLS-1$

        return openInEditor(input, CoreEditorUtils.getEditorID(input), activate);
    }
    
    /**
     * Opens {@code symbol} in the editor and selects its position (obtained via {@link IModulaSymbol#getPosition()});
     *  
     * @param project - which project should be considered for the given symbol
     * @param symbol
     * 
     */
    public static IEditorPart openInEditor(IProject project, IModulaSymbol symbol) {
    	IEditorPart editorPart = null;
    	if ((symbol != null) && (symbol.getPosition() != null)) {
            IFile[] symbolFiles = ModulaSymbolUtils.findIFilesForSymbol(project, symbol);
            if (symbolFiles.length > 0) {
            	IFile file = symbolFiles[0];
                try {
					editorPart = openInEditor(file, true, symbol.getPosition().getOffset(), symbol.getName().length());
				} catch (CoreException e) {
					LogHelper.logError(e);
				}
            }
            else{
            	IFileStore fileStore = ModulaSymbolUtils.getSourceFileStore(symbol);
            	try {
					editorPart = openInEditor(fileStore, true, symbol.getPosition().getOffset(), symbol.getName().length());
				} catch (CoreException e) {
					LogHelper.logError(e);
				}
            }
        }
    	return editorPart;
    }
    
    
    /**
     * Opens an editor on the given File object.
     * <p>
     * Unlike the other <code>openEditor</code> methods, this one
     * can be used to open files that reside outside the workspace
     * resource set.
     * </p>
     * <p>
     * If the page already has an editor open on the target object then that
     * editor is brought to front; otherwise, a new editor is opened.
     * </p>
     * 
     * @param file the file to be opened
     * @param activate if <code>true</code> the editor will be activated
     * 
     * @return an open editor or <code>null</code> if an external editor was opened
     *         or file does not exist.
     */
    public static IEditorPart openInEditor(File file) {
        if (file != null && file.exists() && file.isFile()) {
            return openInEditor(file.toURI());
        }
        return null;
    }

    public static IEditorPart openInEditor(URI uri) {
		return openInEditor(ResourceUtils.toFileStore(uri));
	}

    public static IEditorPart openInEditor(IFileStore fileStore) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
		    return IDE.openEditorOnFileStore(page, fileStore);
		} catch (PartInitException e) {
		}
		return null;
	}
    
    /**
     * Opens an editor on the given File object and sets the current selection
     * for the given range.
     * 
     * <p>
     * Unlike the other <code>openEditor</code> methods, this one
     * can be used to open files that reside outside the workspace
     * resource set.
     * </p>
     * <p>
     * If the page already has an editor open on the target object then that
     * editor is brought to front; otherwise, a new editor is opened.
     * </p>
     * 
     * @param file the file to be opened
     * @param offset the offset of the range, must not be negative
     * @param length the length of the range, must not be negative
     * 
     * @return an open editor or <code>null</code> if an external editor was opened
     *         or file does not exist.
     */
    public static IEditorPart openInEditor(File file, int offset, int length) {
        IEditorPart editorPart = openInEditor(file);
        if (editorPart != null) {
            setSelection(editorPart, offset, length);
        }
        return editorPart;
    }

    
    /**
     * Opens an editor on the given IFile resource.
     * 
     * @param file the file to be opened
     * @param activate if <code>true</code> the editor will be activated
     * 
     * @return an open editor or <code>null</code> if an external editor was opened
     */
    public static IEditorPart openInEditor( IFile file, boolean activate
                                          ) throws CoreException 
    {
        if (file == null)
        	ExceptionHelper.throwCoreException(IdeCorePlugin.PLUGIN_ID, "File must not be null"); //$NON-NLS-1$

        IWorkbenchPage p = WorkbenchUtils.getActivePage();
        if (p == null)
        	ExceptionHelper.throwCoreException(IdeCorePlugin.PLUGIN_ID, "Cannot get active page"); //$NON-NLS-1$
        
        if (ResourceUtils.isOpen(file)){
        	return IDE.openEditor(p, file, activate);
        }
        else{
        	return openInEditor(ResourceUtils.getAbsoluteFile(file));
        }
    }
    
    /**
     * Opens an editor on the given IFile resource and sets the current selection
     * for the given range.
     * 
     * @param file the file to be opened
     * @param offset the offset of the range, must not be negative
     * @param length the length of the range, must not be negative
     * 
     * @return an open editor or <code>null</code> if an external editor was opened.
     */
    public static IEditorPart openInEditor( IFile file, boolean activate
                                          , int offset, int length
                                          ) throws CoreException 
    {
    	IEditorPart editorPart = openInEditor(file, activate);
    	if (editorPart != null) {
    	    setSelection(editorPart, offset, length);
    	}
        return editorPart;
    }
    
	public static IEditorPart openInEditor(IFileStore fileStore,
			boolean activate, int offset, int length) throws CoreException {
		IEditorPart editorPart = openInEditor(fileStore.toURI());
		if (editorPart != null) {
			setSelection(editorPart, offset, length);
		}
		return editorPart;
	}
    /**
     * Sets the current selection for the given range.
     *
     * @param editor the editor to be operated on
     * @param offset the offset of the range, must not be negative
     * @param length the length of the range, must not be negative
     */
    public static void setSelection(IEditorPart editor, int offset, int length)
    {
        ISelectionProvider provider = editor.getEditorSite().getSelectionProvider();
        if (provider != null) {
            IWorkbenchPart activePart = WorkbenchUtils.getActivePart();
            if (activePart instanceof IEditorPart) {
                IWorkbenchPage page = WorkbenchUtils.getActivePage();
                page.getNavigationHistory().markLocation((IEditorPart) activePart);
            }
            provider.setSelection(new TextSelection(offset, length));
        }
    }
    
    public static IEditorInput getActiveEditorInput() {
        IEditorPart part = WorkbenchUtils.getActiveEditor(false);
        if (part != null) {
            return part.getEditorInput();
        }
        return null;
    }
    
    public static IResource getActiveEditorInputAsResource() {
        IEditorInput editorInput = getActiveEditorInput();
        if (editorInput == null) return null;
        return (IResource) editorInput.getAdapter(IResource.class);
    }
    
    public static IFile getActiveEditorInputAsIFile() {
        IEditorInput editorInput = getActiveEditorInput();
        if (editorInput == null) return null;
        return (IFile) editorInput.getAdapter(IFile.class);
    }

    private static boolean isResourceEditorInput(IEditorInput input) {
        if (input instanceof MultiEditorInput) {
                IEditorInput[] inputs= ((MultiEditorInput) input).getInput();
                for (int i= 0; i < inputs.length; i++) {
                        if (inputs[i].getAdapter(IResource.class) != null) {
                                return true;
                        }
                }
        } else if (input.getAdapter(IResource.class) != null) {
                return true;
        }
        return false;
    }
    
    public static String syncGetSymbolName(IFile ifile) {
    	IModuleSymbol moduleSymbol = SymbolModelManager.instance().syncParseFirstSymbol(ParseTaskFactory.create(ifile));
		if (moduleSymbol == null) {
			return null;
		}
		return moduleSymbol.getName();
    }
}
