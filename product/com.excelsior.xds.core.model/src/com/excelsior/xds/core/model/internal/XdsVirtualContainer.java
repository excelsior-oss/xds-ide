package com.excelsior.xds.core.model.internal;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.utils.XdsElementUtils;
import com.excelsior.xds.core.resource.IResourceAccess;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;

/**
 * Logical grouping of xds elements - doesnot corresponds to any physical folder
 * @author lsa80
 */
public class XdsVirtualContainer implements IXdsContainer, IAdaptable {
    private final IXdsProject xdsProject;
    private String name;
    private IXdsContainer parent;
    private List<IXdsElement> children;
    private final String path;
    private IResource resource;
    
    public XdsVirtualContainer(IXdsProject xdsProject, String name, String path, IXdsContainer parent, List<IXdsElement> children) {
        this.xdsProject = xdsProject;
        this.name = name;
        this.path = path;
        this.parent = parent;
        this.children = children;
    }
    
    @Override
    public IXdsProject getXdsProject() {
        return xdsProject;
    }

    @Override
    public String getElementName() {
        return name;
    }
    
    public void setElementName(String name) {
        this.name = name;
    }
    
    public String getPath() {
		return path;
	}

	@Override
    public synchronized IXdsContainer getParent() {
        return parent;
    }
    
    public synchronized void setParent(IXdsContainer parent) {
		this.parent = parent;
	}

    public synchronized void setResource(IResource resource) {
        this.resource = resource;
    }

    @Override
    public synchronized Collection<IXdsElement> getChildren() {
    	return CollectionsUtils.unmodifiableArrayList(children);
    }
    
    public synchronized void addChild(IXdsElement child) {
    	children.add(child);
    }
    
    public synchronized void removeChild(IXdsElement child) {
    	children.remove(child);
    }
    
	@Override
    public String toString() {
        return "XdsVirtualContainer [path=" + path + ", resource=" + resource //$NON-NLS-1$ //$NON-NLS-2$
                + "]"; //$NON-NLS-1$
    }

	@Override
	public synchronized Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		Object adapted = XdsElementCommons.adaptToResource(new IResourceAccess() {
			@Override
			public IResource getResource() throws CoreException {
				return resource;
			}
		}, adapter);
		if (adapted != null){
			return adapted;
		}
		
		if (ResourceMapping.class.equals(adapter)) {
			return XdsElementUtils.createResourceMappingFrom(children, this);
		}
		else if (File.class.equals(adapter)) {
			return new File(getPath()); // TODO : convert path to java.io.File
		}
		
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((xdsProject == null) ? 0 : xdsProject.hashCode());
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
		XdsVirtualContainer other = (XdsVirtualContainer) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (xdsProject == null) {
			if (other.xdsProject != null)
				return false;
		} else if (!xdsProject.equals(other.xdsProject))
			return false;
		return true;
	}
}
