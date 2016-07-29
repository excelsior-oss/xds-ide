package com.excelsior.xds.core.filesystems.history;

import java.io.File;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class HistoryFileStore extends FileStore implements IFileStore {
	private final URI uri;
	private final IFileState fileState;
	
	public HistoryFileStore(URI uri, IFileState fileState) {
		this.fileState = fileState;
		this.uri = uri;
	}

	@Override
	public IFileInfo[] childInfos(int options, IProgressMonitor monitor)
			throws CoreException {
		return new IFileInfo[0];
	}

	@Override
	public String[] childNames(int options, IProgressMonitor monitor)
			throws CoreException {
		return new String[0];
	}

	@Override
	public IFileStore[] childStores(int options, IProgressMonitor monitor)
			throws CoreException {
		return new IFileStore[0];
	}

	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor)
			throws CoreException {
		FileInfo fileInfo = new FileInfo(getName());
		fileInfo.setDirectory(false);
		fileInfo.setLastModified(fileState.getModificationTime());
		fileInfo.setExists(true);
		fileInfo.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
		return fileInfo;
	}

	@Override
	public IFileStore getChild(IPath path) {
		return null;
	}

	@Override
	public IFileStore getFileStore(IPath path) {
		return null;
	}

	@Override
	public IFileStore getChild(String name) {
		return null;
	}

	@Override
	public String getName() {
		return org.eclipse.core.runtime.URIUtil.lastSegment(uri);
	}

	@Override
	public IFileStore getParent() {
		return null;
	}

	@Override
	public boolean isParentOf(IFileStore other) {
		return false;
	}

	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor)
			throws CoreException {
		return fileState.getContents();
	}

	@Override
	public File toLocalFile(int options, IProgressMonitor monitor)
			throws CoreException {
		return null;
	}

	@Override
	public URI toURI() {
		return uri;
	}

}
