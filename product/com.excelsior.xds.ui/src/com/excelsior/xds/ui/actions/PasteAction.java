package com.excelsior.xds.ui.actions;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.actions.CopyProjectOperation;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;
import org.eclipse.ui.part.ResourceTransfer;

@SuppressWarnings("restriction")
public class PasteAction extends SelectionListenerAction {

    /**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID + ".PasteAction";//$NON-NLS-1$

    /**
     * The shell in which to show any dialogs.
     */

    /**
     * System clipboard
     */
    private Clipboard clipboard;

	private IShellProvider shellProvider;

	public PasteAction(IShellProvider shellProvider, Clipboard clipboard) {
        super(ResourceNavigatorMessages.PasteAction_title);
        this.shellProvider = shellProvider;
        this.clipboard = clipboard;
        setToolTipText(ResourceNavigatorMessages.PasteAction_toolTip);
        setId(PasteAction.ID);
    }

    private IResource getTarget() {
        List<?> selectedResources = getSelectedResources();

        for (int i = 0; i < selectedResources.size(); i++) {
            IResource resource = (IResource) selectedResources.get(i);

            if (resource instanceof IProject && !((IProject) resource).isOpen()) {
				return null;
			}
            if (resource.getType() == IResource.FILE) {
				resource = resource.getParent();
			}
            if (resource != null) {
				return resource;
			}
        }
        return null;
    }

    private boolean isLinked(IResource[] resources) {
        for (int i = 0; i < resources.length; i++) {
            if (resources[i].isLinked()) {
				return true;
			}
        }
        return false;
    }

    public void run() {
        // try a resource transfer
        ResourceTransfer resTransfer = ResourceTransfer.getInstance();
        IResource[] resourceData = (IResource[]) clipboard
                .getContents(resTransfer);

        if (resourceData != null && resourceData.length > 0) {
            if (resourceData[0].getType() == IResource.PROJECT) {
                // enablement checks for all projects
                for (int i = 0; i < resourceData.length; i++) {
                    CopyProjectOperation operation = new CopyProjectOperation(
                            this.shellProvider.getShell());
                    operation.copyProject((IProject) resourceData[i]);
                }
            } else {
                // enablement should ensure that we always have access to a container
                IContainer container = getContainer();

                CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(
                        this.shellProvider.getShell());
                operation.copyResources(resourceData, container);
            }
            return;
        }

        // try a file transfer
        FileTransfer fileTransfer = FileTransfer.getInstance();
        String[] fileData = (String[]) clipboard.getContents(fileTransfer);

        if (fileData != null) {
            // enablement should ensure that we always have access to a container
            IContainer container = getContainer();

            CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(
                    this.shellProvider.getShell());
            operation.copyFiles(fileData, container);
        }
    }

    private IContainer getContainer() {
        List<?> selection = getSelectedResources();
        if (selection.get(0) instanceof IFile) {
			return ((IFile) selection.get(0)).getParent();
		} else {
			return (IContainer) selection.get(0);
		}
    }

    protected boolean updateSelection(IStructuredSelection selection) {
        if (!super.updateSelection(selection)) {
			return false;
		}

        final IResource[][] clipboardData = new IResource[1][];
        shellProvider.getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
            	if (clipboard == null) {
            		clipboard = new Clipboard(shellProvider.getShell().getDisplay());
            	}
                // clipboard must have resources or files
                ResourceTransfer resTransfer = ResourceTransfer.getInstance();
                clipboardData[0] = (IResource[]) clipboard
                        .getContents(resTransfer);
            }
        });
        IResource[] resourceData = clipboardData[0];
        boolean isProjectRes = resourceData != null && resourceData.length > 0
                && resourceData[0].getType() == IResource.PROJECT;

        if (isProjectRes) {
            for (int i = 0; i < resourceData.length; i++) {
                // make sure all resource data are open projects
                // can paste open projects regardless of selection
                if (resourceData[i].getType() != IResource.PROJECT
                        || ((IProject) resourceData[i]).isOpen() == false) {
					return false;
				}
            }
            return true;
        }

        if (getSelectedNonResources().size() > 0) {
			return false;
		}

        IResource targetResource = getTarget();
        // targetResource is null if no valid target is selected (e.g., open project) 
        // or selection is empty	
        if (targetResource == null) {
			return false;
		}

        // can paste files and folders to a single selection (file, folder, 
        // open project) or multiple file selection with the same parent
        List<?> selectedResources = getSelectedResources();
        if (selectedResources.size() > 1) {
            for (int i = 0; i < selectedResources.size(); i++) {
                IResource resource = (IResource) selectedResources.get(i);
                if (resource.getType() != IResource.FILE) {
					return false;
				}
                if (!targetResource.equals(resource.getParent())) {
					return false;
				}
            }
        }
        if (resourceData != null) {
            // linked resources can only be pasted into projects
            if (isLinked(resourceData)
                    && targetResource.getType() != IResource.PROJECT
                    && targetResource.getType() != IResource.FOLDER) {
				return false;
			}

            if (targetResource.getType() == IResource.FOLDER) {
                // don't try to copy folder to self
                for (int i = 0; i < resourceData.length; i++) {
                    if (targetResource.equals(resourceData[i])) {
						return false;
					}
                }
            }
            return true;
        }
        TransferData[] transfers = clipboard.getAvailableTypes();
        FileTransfer fileTransfer = FileTransfer.getInstance();
        for (int i = 0; i < transfers.length; i++) {
            if (fileTransfer.isSupportedType(transfers[i])) {
				return true;
			}
        }
        return false;
    }
}