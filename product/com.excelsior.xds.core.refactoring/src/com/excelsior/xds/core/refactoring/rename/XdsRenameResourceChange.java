package com.excelsior.xds.core.refactoring.rename;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;
import org.eclipse.ltk.core.refactoring.resource.ResourceChange;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

import com.excelsior.xds.core.resource.ResourceUtils;

/**
 * {@link Change} that renames a resource. Takes special care of linked file, renaming underlying file too.
 *
 * @since 3.4
 */
@SuppressWarnings("restriction")
public class XdsRenameResourceChange extends ResourceChange {

	private final String fNewName;
	private final IPath fResourcePath;
	private final long fStampToRestore;

	private ChangeDescriptor fDescriptor;

	private IPath fConflictingResourcePath; // file already existing at destination
	/**
	 * Creates the change.
	 *
	 * @param resourcePath the path of the resource to rename
	 * @param newName the new name. Must not be empty.
	 */
	public XdsRenameResourceChange(IPath resourcePath, IPath conflictingResourcePath, String newName) {
		this(resourcePath, newName, IResource.NULL_STAMP);
		fConflictingResourcePath = conflictingResourcePath;
	}

	/**
	 * Creates the change with a time stamp to restore.
	 *
	 * @param resourcePath  the path of the resource to rename
	 * @param newName the new name. Must not be empty.
	 * @param stampToRestore the time stamp to restore or {@link IResource#NULL_STAMP} to not restore the
	 * time stamp.
	 */
	protected XdsRenameResourceChange(IPath resourcePath, String newName, long stampToRestore) {
		if (resourcePath == null || newName == null || newName.length() == 0) {
			throw new IllegalArgumentException();
		}

		fResourcePath= resourcePath;
		fNewName= newName;
		fStampToRestore= stampToRestore;
		fDescriptor= null;
		setValidationMethod(VALIDATE_NOT_DIRTY);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getDescriptor()
	 */
	public ChangeDescriptor getDescriptor() {
		return fDescriptor;
	}

	/**
	 * Sets the change descriptor to be returned by {@link Change#getDescriptor()}.
	 *
	 * @param descriptor the change descriptor
	 */
	public void setDescriptor(ChangeDescriptor descriptor) {
		fDescriptor= descriptor;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.resource.ResourceChange#getModifiedResource()
	 */
	protected IResource getModifiedResource() {
		return getResource();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	public String getName() {
		String renameResourceMsg = Messages.format(RefactoringCoreMessages.RenameResourceChange_name, new String[] { BasicElementLabels.getPathLabel(fResourcePath, false), BasicElementLabels.getResourceName(fNewName) });
		if (fConflictingResourcePath == null) {
			return renameResourceMsg;
		}
		else {
			String deleteResourceMsg = Messages.format(RefactoringCoreMessages.DeleteResourceChange_name, BasicElementLabels.getPathLabel(fConflictingResourcePath.makeRelative(), false));
			return deleteResourceMsg + " AND " + renameResourceMsg;
		}
	}

	/**
	 * Returns the new name.
	 *
	 * @return return the new name
	 */
	public String getNewName() {
		return fNewName;
	}

	private IResource getResource() {
		return ResourcesPlugin.getWorkspace().getRoot().findMember(fResourcePath);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		try {
			pm.beginTask(RefactoringCoreMessages.RenameResourceChange_progress_description, 1);

			IResource resource= getResource();
			File absoluteFile = ResourceUtils.getAbsoluteFile(resource);
			IPath destPath = renamedResourcePath(resource.getRawLocation(), fNewName);
			
			boolean isLinked = resource.isLinked();
			
			long currentStamp= resource.getModificationStamp();
			IPath newPath= renamedResourcePath(fResourcePath, fNewName);
			IFile destIFile = ResourceUtils.getWorkspaceRoot().getFile(newPath);
			Change undoDeleteChange = null;
			if (destIFile.exists()) { // handle rename conflict 
				DeleteResourceChange deleteChange = new DeleteResourceChange(destIFile.getFullPath(), true);
				undoDeleteChange = deleteChange.perform(pm);
			}
			
			resource.move(newPath, IResource.SHALLOW, pm);
			
			if (isLinked) {
				File dest = destPath.toFile();
				FileUtils.deleteQuietly(dest);
				absoluteFile.renameTo(dest);
				
				// get the resource again since after move resource can be inadequate
				IWorkspaceRoot workspaceRoot = ResourceUtils.getWorkspaceRoot();
				resource = workspaceRoot.getFile(newPath);
				((IFile)resource).createLink(destPath, IResource.REPLACE, new NullProgressMonitor());
			}
			if (fStampToRestore != IResource.NULL_STAMP) {
				IResource newResource= ResourcesPlugin.getWorkspace().getRoot().findMember(newPath);
				newResource.revertModificationStamp(fStampToRestore);
			}
			String oldName= fResourcePath.lastSegment();
			XdsRenameResourceChange undoRenameChange = new XdsRenameResourceChange(newPath, oldName, currentStamp);
			if (undoDeleteChange == null) {
				return undoRenameChange;
			}
			else {
				return new CompositeChange(getName(), new Change[]{undoRenameChange, undoDeleteChange}); // constructing undo changes
			}
		} finally {
			pm.done();
		}
	}

	private static IPath renamedResourcePath(IPath path, String newName) {
		return path.removeLastSegments(1).append(newName);
	}

}
