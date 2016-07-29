package com.excelsior.xds.core.model.internal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsExternalCompilationUnit;
import com.excelsior.xds.core.model.IXdsProject;

public class XdsExternalCompilationUnit extends    XdsWorkspaceCompilationUnit 
                                        implements IXdsExternalCompilationUnit 
{
    private final String absolutePath;
    private final String name;
    
    /**
     * Relative path from the mount point
     */
    private String mountPointRelativePath;

    public XdsExternalCompilationUnit( IXdsProject xdsProject, IXdsContainer parent
                                     , String absolutePath, String name ) 
    {
        super(xdsProject, null, parent);
        this.absolutePath = absolutePath;
        determineCompilationUnitType(absolutePath);
        this.name = name;
    }
    
    @Override
    public String getElementName() {
        return name;
    }
    
    public void setParent(IXdsContainer parent) {
    	super.setParent(parent);
    }
    
    public void setResource(IResource resource) {
		super.setResource(resource);
	}
    
	/**
	 * Relative path from the mount point
	 */
	public synchronized String getMountPointRelativePath() {
		return mountPointRelativePath;
	}
	
	/**
	 * Relative path from the mount point
	 */
	public synchronized void setMountPointRelativePath(String mountPointRelativePath) {
		this.mountPointRelativePath = mountPointRelativePath;
	}

	@Override
    public InputStream getContents() throws CoreException {
        try {
            return new BufferedInputStream(new FileInputStream(new File(absolutePath)));
        } catch (FileNotFoundException e) {
            LogHelper.logError(e);
            throw new CoreException(LogHelper.createStatus(IStatus.ERROR, 0, "File Not Found", e)); //$NON-NLS-1$
        }
    }

    @Override
    public IPath getFullPath() {
        return new Path(absolutePath);
    }

    @Override
    public String getName() {
        return FilenameUtils.getName(absolutePath);
    }

    @Override
    public boolean isReadOnly() {
        return !new File(absolutePath).canWrite();
    }
}
