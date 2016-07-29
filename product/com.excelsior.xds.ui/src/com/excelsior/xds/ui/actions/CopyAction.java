package com.excelsior.xds.ui.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * Standard action for copying the currently selected resources to the clipboard.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class CopyAction extends SelectionListenerAction {

    /**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID + ".CopyAction"; //$NON-NLS-1$

    /**
     * The shell in which to show any dialogs.
     */
    /**
     * System clipboard
     */
    private Clipboard clipboard;

    /**
     * Associated paste action. May be <code>null</code>
     */
    private PasteAction pasteAction;

	private IShellProvider shellProvider;

    /**
     * Creates a new action.
     *
     * @param shell the shell for any dialogs
     * @param clipboard a platform clipboard
     */
	public CopyAction(IShellProvider shellProvider, Clipboard clipboard) {
        super(ResourceNavigatorMessages.CopyAction_title);
        this.clipboard = clipboard;
        this.shellProvider = shellProvider;
        setToolTipText(ResourceNavigatorMessages.CopyAction_toolTip);
        setId(CopyAction.ID);
    }

    /**
     * Creates a new action.
     *
     * @param shell the shell for any dialogs
     * @param clipboard a platform clipboard
     * @param pasteAction a paste action
     * 
     * @since 2.0
     */
    public CopyAction(IShellProvider shellProvider, Clipboard clipboard, PasteAction pasteAction) {
        this(shellProvider, clipboard);
        this.pasteAction = pasteAction;
    }

   
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
    	 /**
         * The <code>CopyAction</code> implementation of this method defined 
         * on <code>IAction</code> copies the selected resources to the 
         * clipboard.
         */
        List<?> selectedResources = getSelectedResources();
        IResource[] resources = (IResource[]) selectedResources
                .toArray(new IResource[selectedResources.size()]);

        // Get the file names and a string representation
        final int length = resources.length;
        int actualLength = 0;
        String[] fileNames = new String[length];
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++) {
            IPath location = resources[i].getLocation();
            // location may be null. See bug 29491.
            if (location != null) {
				fileNames[actualLength++] = location.toOSString();
			}
            if (i > 0) {
				buf.append("\n"); //$NON-NLS-1$
			}
            buf.append(resources[i].getName());
        }
        // was one or more of the locations null?
        if (actualLength < length) {
            String[] tempFileNames = fileNames;
            fileNames = new String[actualLength];
            for (int i = 0; i < actualLength; i++) {
				fileNames[i] = tempFileNames[i];
			}
        }
        setClipboard(resources, fileNames, buf.toString());

        // update the enablement of the paste action
        // workaround since the clipboard does not suppot callbacks
        if (pasteAction != null && pasteAction.getStructuredSelection() != null) {
			pasteAction.selectionChanged(pasteAction.getStructuredSelection());
		}
    }

    /**
     * Set the clipboard contents. Prompt to retry if clipboard is busy.
     * 
     * @param resources the resources to copy to the clipboard
     * @param fileNames file names of the resources to copy to the clipboard
     * @param names string representation of all names
     */
    private void setClipboard(IResource[] resources, String[] fileNames,
            String names) {
        try {
        	if (clipboard == null) {
        		clipboard = new Clipboard(shellProvider.getShell().getDisplay());
        	}
            // set the clipboard contents
            if (fileNames.length > 0) {
                clipboard.setContents(new Object[] { resources, fileNames,
                        names },
                        new Transfer[] { ResourceTransfer.getInstance(),
                                FileTransfer.getInstance(),
                                TextTransfer.getInstance() });
            } else {
                clipboard.setContents(new Object[] { resources, names },
                        new Transfer[] { ResourceTransfer.getInstance(),
                                TextTransfer.getInstance() });
            }
        } catch (SWTError e) {
            if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
				throw e;
			}
            
            if (MessageDialog
                    .openQuestion(
                    		shellProvider.getShell(),
                            ResourceNavigatorMessages.CopyToClipboardProblemDialog_title, ResourceNavigatorMessages.CopyToClipboardProblemDialog_message)) {
				setClipboard(resources, fileNames, names);
			}
        }
    }

   
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
     */
    protected boolean updateSelection(IStructuredSelection selection) {
    	
    	 /**
         * The <code>CopyAction</code> implementation of this
         * <code>SelectionListenerAction</code> method enables this action if 
         * one or more resources of compatible types are selected.
         */
    	
        if (!super.updateSelection(selection)) {
			return false;
		}

        if (getSelectedNonResources().size() > 0) {
			return false;
		}

        List<?> selectedResources = getSelectedResources();
        if (selectedResources.size() == 0) {
			return false;
		}

        boolean projSelected = selectionIsOfType(IResource.PROJECT);
        boolean fileFoldersSelected = selectionIsOfType(IResource.FILE
                | IResource.FOLDER);
        if (!projSelected && !fileFoldersSelected) {
			return false;
		}

        // selection must be homogeneous
        if (projSelected && fileFoldersSelected) {
			return false;
		}

        // must have a common parent	
        IContainer firstParent = ((IResource) selectedResources.get(0))
                .getParent();
        if (firstParent == null) {
			return false;
		}

        Iterator<?> resourcesEnum = selectedResources.iterator();
        while (resourcesEnum.hasNext()) {
            IResource currentResource = (IResource) resourcesEnum.next();
            if (!currentResource.getParent().equals(firstParent)) {
				return false;
			}
        }

        return true;
    }

}

