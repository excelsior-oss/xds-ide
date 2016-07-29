package com.excelsior.xds.core.resource;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public final class PathWithLocationType 
{
    /** File system handle of the resource */
    private final IFileStore store;
    
    /** Workspace-relative path of the resource */
    private final IPath fullPath; 
    
    /** The type of the given location */
    private final LocationType locationType;

    private PathWithLocationType(IPath fullPath, IFileStore store, LocationType locationType) {
        this.store = store;
        this.fullPath = fullPath;
        this.locationType = locationType;
    }

    public PathWithLocationType(IResource res) {
        this(res.getFullPath(), null, LocationType.WORKSPACE);
    }

    public PathWithLocationType(IFileStore store) {
        this(null, store, LocationType.NONWORKSPACE);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fullPath == null) ? 0 : fullPath.hashCode());
		result = prime * result
				+ ((locationType == null) ? 0 : locationType.hashCode());
		result = prime * result + ((store == null) ? 0 : store.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathWithLocationType other = (PathWithLocationType) obj;
		if (fullPath == null) {
			if (other.fullPath != null)
				return false;
		} else if (!fullPath.equals(other.fullPath))
			return false;
		if (locationType != other.locationType)
			return false;
		if (store == null) {
			if (other.store != null)
				return false;
		} else if (!store.equals(other.store))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String
				.format("PathWithLocationType [store=%s, fullPath=%s, locationType=%s]",
						store, fullPath, locationType); //$NON-NLS-1$
	}
}
