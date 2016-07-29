package com.excelsior.xds.ui.navigator.project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.actions.MoveFilesAndFoldersOperation;
import org.eclipse.ui.actions.ReadOnlyStateChecker;
import org.eclipse.ui.ide.dialogs.ImportTypeDialog;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.eclipse.ui.part.ResourceTransfer;

import com.excelsior.xds.core.model.IXdsExternalCompilationUnit;
import com.excelsior.xds.core.model.IXdsFolderContainer;
import com.excelsior.xds.core.model.IXdsResource;

/**
 * 
 * Customizes the drop part of the drag-n-drop operation.<br><br><br>
 * @author lsa80
 */
public class ProjectExplorerDropAdapterAssistant extends CommonDropAdapterAssistant {
	private static final IResource[] NO_RESOURCES = new IResource[0];
	
	public ProjectExplorerDropAdapterAssistant() {
	}
	
	/*
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#isSupportedType(org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public boolean isSupportedType(TransferData transferType) {
		return super.isSupportedType(transferType)
				|| ResourceTransfer.getInstance().isSupportedType(transferType)
				|| FileTransfer.getInstance().isSupportedType(transferType);
	}

	@Override
	public IStatus validateDrop(Object target, int operation,
			TransferData transferType) {
		// drop in folder
		if (target instanceof IXdsFolderContainer
				|| target instanceof IProject
				|| target instanceof IContainer
				|| (operation == DND.DROP_COPY && (target instanceof IFile || target instanceof IXdsResource))) {
			IContainer destination = getDestination(target);
			if (LocalSelectionTransfer.getTransfer().isSupportedType(
					transferType)) {
				IResource[] selectedResources = getSelectedResources();
				if (selectedResources.length > 0) {
					for (IResource res : selectedResources) {
						if (res instanceof IProject) {
							return Status.CANCEL_STATUS;
						}
					}
					if (operation == DND.DROP_COPY
							|| operation == DND.DROP_LINK) {
						CopyFilesAndFoldersOperation op = new CopyFilesAndFoldersOperation(
								getShell());
						if (op.validateDestination(destination,
								selectedResources) == null) {
							return Status.OK_STATUS;
						}
					} else {
						MoveFilesAndFoldersOperation op = new MoveFilesAndFoldersOperation(
								getShell());
						if (op.validateDestination(destination,
								selectedResources) == null) {
							return Status.OK_STATUS;
						}
					}
				}
			} else if (FileTransfer.getInstance().isSupportedType(transferType)) {
				String[] sourceNames = (String[]) FileTransfer.getInstance()
						.nativeToJava(transferType);
				if (sourceNames == null) {
					// source names will be null on Linux. Use empty names to do
					// destination validation.
					// Fixes bug 29778
					sourceNames = new String[0];
				}
				CopyFilesAndFoldersOperation copyOperation = new CopyFilesAndFoldersOperation(
						getShell());
				if (null != copyOperation.validateImportDestination(
						destination, sourceNames)) {
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		}
		return Status.CANCEL_STATUS;
	}

	public static IXdsResource[] getXdsResources(ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}
		List<?> elements = ((IStructuredSelection)selection).toList();
		List<Object> resources= new ArrayList<Object>(elements.size());
		for (Object element : elements) {
			if (element instanceof IXdsResource)
				resources.add(element);
		}
		return resources.toArray(new IXdsResource[resources.size()]);
	}

	/*
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#handleDrop(org.eclipse.ui.navigator.CommonDropAdapter, org.eclipse.swt.dnd.DropTargetEvent, java.lang.Object)
	 */
	@Override
	public IStatus handleDrop(CommonDropAdapter dropAdapter,
			DropTargetEvent event, Object target) {

		try {
			// drop in folder
			if (target instanceof IXdsFolderContainer || 
					target instanceof IProject || 
					target instanceof IContainer ||
					(dropAdapter.getCurrentOperation() == DND.DROP_COPY && (
							target instanceof IFile ||
							target instanceof IXdsResource))) {
	
				final Object data= event.data;
				if (data == null) {
					return Status.CANCEL_STATUS;
				}
				final IContainer destination= getDestination(target);
				if (destination == null) {
					return Status.CANCEL_STATUS;
				}
				IResource[] resources = null;
				TransferData currentTransfer = dropAdapter.getCurrentTransfer();
				final int dropOperation = dropAdapter.getCurrentOperation();
				if (LocalSelectionTransfer.getTransfer().isSupportedType(
						currentTransfer)) {
					resources = getSelectedResources();
				} else if (ResourceTransfer.getInstance().isSupportedType(
						currentTransfer)) {
					resources = (IResource[]) event.data;
				}
				if (FileTransfer.getInstance().isSupportedType(currentTransfer)) {
					final String[] names = (String[]) data;
					// Run the import operation asynchronously. 
					// Otherwise the drag source (e.g., Windows Explorer) will be blocked 
					Display.getCurrent().asyncExec(new Runnable() {
						public void run() {
							getShell().forceActive();
							CopyFilesAndFoldersOperation op= new CopyFilesAndFoldersOperation(getShell());
							op.copyOrLinkFiles(names, destination, dropOperation);
						}
					});
				} else if (event.detail == DND.DROP_COPY || event.detail == DND.DROP_LINK) {
					return performResourceCopy(dropAdapter, getShell(), resources);
				} else {
					ReadOnlyStateChecker checker = new ReadOnlyStateChecker(
						getShell(), 
						"Move Resource Action",	//$NON-NLS-1$
						"Move Resource Action");//$NON-NLS-1$	
					resources = checker.checkReadOnlyResources(resources);
					MoveFilesAndFoldersOperation operation = new MoveFilesAndFoldersOperation(getShell());
					operation.copyResources(resources, destination);
				}
				return Status.OK_STATUS;
			}
		} 
		finally {
			// The drag source listener must not perform any operation
			// since this drop adapter did the remove of the source even
			// if we moved something.
			event.detail= DND.DROP_NONE;
		}
		return Status.CANCEL_STATUS;
	}
	
	private IContainer getDestination(Object dropTarget) {
		if (dropTarget instanceof IContainer) {
			return (IContainer)dropTarget;
		} else if (dropTarget instanceof IXdsResource) {
			return getDestination(((IXdsResource)dropTarget).getResource());
		} else if (dropTarget instanceof IFile) {
			return ((IFile)dropTarget).getParent();
		}
		return null;
	}
	
	/**
	 * Returns the resource selection from the LocalSelectionTransfer.
	 * 
	 * @return the resource selection from the LocalSelectionTransfer
	 */
	private IResource[] getSelectedResources() {

		ISelection selection = LocalSelectionTransfer.getTransfer()
				.getSelection();
		if (selection instanceof IStructuredSelection) {
			return getSelectedResources((IStructuredSelection)selection);
		} 
		return NO_RESOURCES;
	}

	/**
	 * Returns the resource selection from the LocalSelectionTransfer.
	 * 
	 * @return the resource selection from the LocalSelectionTransfer
	 */
	private IResource[] getSelectedResources(IStructuredSelection selection) {
		List<Object> selectedResources = new ArrayList<Object>();

		for (Iterator<?> i = selection.iterator(); i.hasNext();) {
			Object o = i.next();
			if (o instanceof IXdsExternalCompilationUnit) {
				continue;
			}
			else if (o instanceof IXdsResource) {
				IXdsResource xr = (IXdsResource) o;
				selectedResources.add(xr.getResource());
			}
			else if (o instanceof IResource) {
				selectedResources.add(o);
			} else if (o instanceof IAdaptable) {
				IAdaptable a = (IAdaptable) o;
				IResource r = (IResource) a.getAdapter(IResource.class);
				if (r != null) {
					selectedResources.add(r);
				}
			}
		}
		return selectedResources
				.toArray(new IResource[selectedResources.size()]);
	}
	
	/**
	 * Performs a resource copy.
	 * Cloned from ResourceDropAdapterAssistant to support linked resources (bug 319405).
	 */
	private IStatus performResourceCopy(CommonDropAdapter dropAdapter,
			Shell shell, IResource[] sources) {
		IContainer target = getDestination(dropAdapter.getCurrentTarget());
		if (target == null) {
			return Status.CANCEL_STATUS;
		}
		
		boolean shouldLinkAutomatically = false;
		if (target.isVirtual()) {
			shouldLinkAutomatically = true;
			for (int i = 0; i < sources.length; i++) {
				if ((sources[i].getType() != IResource.FILE) && (sources[i].getLocation() != null)) {
					// If the source is a folder, but the location is null (a
					// broken link, for example),
					// we still generate a link automatically (the best option).
					shouldLinkAutomatically = false;
					break;
				}
			}
		}

		CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(shell);
		// if the target is a virtual folder and all sources are files, then
		// automatically create links
		if (shouldLinkAutomatically) {
			operation.setCreateLinks(true);
			operation.copyResources(sources, target);
		} else {
			boolean allSourceAreLinksOrVirtualFolders = true;
			for (int i = 0; i < sources.length; i++) {
				if (!sources[i].isVirtual() && !sources[i].isLinked()) {
					allSourceAreLinksOrVirtualFolders = false;
					break;
				}
			}
			// if all sources are either links or groups, copy then normally,
			// don't show the dialog
			if (!allSourceAreLinksOrVirtualFolders) {
				ImportTypeDialog dialog = new ImportTypeDialog(getShell(), dropAdapter.getCurrentOperation(), sources, target);
				dialog.setResource(target);
				if (dialog.open() == Window.OK) {
					if (dialog.getSelection() == ImportTypeDialog.IMPORT_VIRTUAL_FOLDERS_AND_LINKS)
						operation.setVirtualFolders(true);
					if (dialog.getSelection() == ImportTypeDialog.IMPORT_LINK)
						operation.setCreateLinks(true);
					if (dialog.getVariable() != null)
						operation.setRelativeVariable(dialog.getVariable());
					operation.copyResources(sources, target);
				} else
					return Status.CANCEL_STATUS;
			} else
				operation.copyResources(sources, target);
		}

		return Status.OK_STATUS;
	}
}
