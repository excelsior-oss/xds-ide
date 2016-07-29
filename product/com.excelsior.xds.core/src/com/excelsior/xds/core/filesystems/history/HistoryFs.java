package com.excelsior.xds.core.filesystems.history;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.history.IFileRevision;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.resource.ResourceUtils;

public class HistoryFs extends FileSystem {
	
	public static final String SCHEME_HISTORY = "history"; //$NON-NLS-1$

	public HistoryFs() {
	}
	
	@Override
	public boolean canWrite() {
		return false;
	}
	
	@Override
	public boolean canDelete() {
		return false;
	}

	@Override
	public IFileStore getStore(URI uri) {
		IFile f = ResourceUtils.getWorkspaceRoot().getFile(new Path(uri.getPath()));
		long time = getTime(uri.getQuery());
		IFileState[] history = null;
		try {
			history = f.getHistory(new NullProgressMonitor());
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
		IFileState fileState = findFileState(history, time);
		HistoryFileStore s = new HistoryFileStore(uri, fileState);
		return s;
	}
	
	public static URI toURI(IFileRevision r) {
		try {
			return new URI(String.format("%s://%s?time=%s", SCHEME_HISTORY, r.getURI().getPath(), r.getTimestamp()));
		} catch (URISyntaxException e) {
			LogHelper.logError(e);
			return null;
		}
	}
	
	private IFileState findFileState(IFileState[] history, long time) {
		for (IFileState s : history) {
			if (s.getModificationTime() == time) {
				return s;
			}
		}
		return null;
	}

	private static long getTime(String query) {
		return Long.parseLong(query.split("=")[1]);
	}
}
