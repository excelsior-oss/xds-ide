package com.excelsior.xds.core.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsResource;
import com.excelsior.xds.core.model.IXdsSymbolFile;
import com.excelsior.xds.core.model.IXdsWorkspaceCompilationUnit;
import com.excelsior.xds.core.project.SpecialFileNames;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.utils.XdsFileUtils;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;

public class XdsFolderContainer extends    PlatformObject 
                                implements IXdsResource
                                         , IEditableXdsFolderContainer 
{
	protected List<IXdsElement> children;
    protected final Set<IResource> childResources = new HashSet<IResource>();
    
    private final IResource resource;
    private final XdsProject xdsProject;
    private final IXdsContainer parent;
    
    public XdsFolderContainer(XdsProject xdsProject, IResource resource, IXdsContainer parent) 
    {
        this.xdsProject = xdsProject;
        this.resource = resource;
        this.parent = parent;
    }

    @Override
    public String getElementName() {
        return resource.getName();
    }
    
    @Override
    public XdsProject getXdsProject() {
        return xdsProject;
    }

    @Override
    public IResource getResource() {
        return resource;
    }
    
    @Override
    public synchronized Collection<IXdsElement> getChildren() {
        buildChildren(resource, getResourceFilter());
        return CollectionsUtils.unmodifiableArrayList(children, IXdsElement.class);
    }
    
    /**
     * Specifies resources that will be used as this folder`s children.
     * @return
     */
    protected Predicate<IResource> getResourceFilter() {
    	return getXdsProject().getResourceFilter();
    }
    
    /**
     * TODO : remove, modify and use {@link #getResourceFilter()} instead.
     * @return
     */
    protected Collection<String> getRelativeFolderPathesToSkip() {
    	return Collections.emptyList();
    }
    
    protected Predicate<IResource> debugPrintPredicate(Predicate<IResource> wrapped, String fmt) {
    	return r -> {
    		boolean result = wrapped.test(r);
			return result;
    	};
    }
    
    protected synchronized void buildChildren(final IResource root, Predicate<IResource> resourceFilter) {
        if (children == null) {
            makeChildrenContainer();
            try {
                root.accept(new IResourceVisitor() {
                    @Override
                    public boolean visit(IResource resource) throws CoreException {
                        if (root == resource) return true;
                        
                        // skip resources if they are inside specified skip folders
                        for (String relativeFolderPath : getRelativeFolderPathesToSkip()) {
                            if (ResourceUtils.isInsideFolder(relativeFolderPath, resource)) {
                                return false;
                            }
                        }
                        
                        if (!resourceFilter.test(resource)) {
                        	return false;
                        }
                        
                        if (resource instanceof IContainer) {
                            XdsFolderContainer container = new XdsFolderContainer(getXdsProject(), resource, XdsFolderContainer.this);
                            addChild(container);
                        }
                        else if (XdsFileUtils.isCompilationUnitFile(resource.getFullPath().lastSegment())) {
                        	IXdsWorkspaceCompilationUnit xdsCompilationUnit = new XdsWorkspaceCompilationUnit(getXdsProject(), resource, XdsFolderContainer.this);
                            addChild(xdsCompilationUnit);
                        }
                        else if (XdsFileUtils.isSymbolFile(resource.getFullPath().lastSegment())) {
                            IXdsSymbolFile xdsSymFile = new XdsSymbolFile(getXdsProject(), resource, XdsFolderContainer.this);
                            addChild(xdsSymFile);
                        }
                        else if (XdsFileUtils.isXdsProjectFile(resource.getFullPath()
                                .lastSegment())) {
                            addChild(new XdsProjectDescriptor(getXdsProject(), resource, XdsFolderContainer.this));
                        }
                        else if (XdsFileUtils.isDbgScriptFile(resource.getFullPath().lastSegment())) {
                            addChild(new XdsDdgScriptUnitFile(getXdsProject(), resource, XdsFolderContainer.this));
                        }
                        else if (XdsFileUtils.isDbgScriptBundleFile(resource.getFullPath().lastSegment())) {
                        	addChild(new XdsDbgScriptBundleFile(getXdsProject(), resource, XdsFolderContainer.this));
                        }
                        else {
                        	String fileName = FilenameUtils.getName(ResourceUtils.getAbsolutePath(resource));
                        	if (!SpecialFileNames.getSpecialFileNames().contains(fileName)) {
                        		addChild(new XdsTextFile(getXdsProject(), resource, XdsFolderContainer.this));
                        	}
                        }
                        return true;
                    }
                }, IResource.DEPTH_ONE, true);
            } catch (CoreException e) {
                LogHelper.logError(e);
            }
        }
    }

    @Override
    public IXdsContainer getParent() {
        return parent;
    }
    
    @Override
    public String toString() {
        return resource.toString();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getWorkspaceRelativePath() == null) ? 0 : getWorkspaceRelativePath().hashCode());
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
        XdsFolderContainer other = (XdsFolderContainer) obj;
        if (getWorkspaceRelativePath() == null) {
            if (other.getWorkspaceRelativePath() != null)
                return false;
        } else if (!getWorkspaceRelativePath().equals(other.getWorkspaceRelativePath()))
            return false;
        return true;
    }
    
    private String getWorkspaceRelativePath() {
        return ResourceUtils.getWorkspaceRelativePath(getResource());
    }

    @Override
    public synchronized void addChild(IXdsResource e) {
        if (children == null) {
            makeChildrenContainer();
        }
        children.add(e);
        IResource childResource = e.getResource();
        if (childResource != null) {
            childResources.add(childResource);
        }
        getXdsProject().getModel().putWorkspaceXdsElement(childResource, e);
    }
    
    @Override
    public synchronized void removeChild(IXdsResource e) {
        children.remove(e);
        
        IResource childResource = e.getResource();
        if (childResource != null) {
            childResources.remove(childResource);
        }
        
        getXdsProject().getModel().removeWorkspaceXdsElement(childResource);
    }

    @Override
    public synchronized Collection<IResource> getChildResources() {
        return CollectionsUtils.unmodifiableArrayList(childResources, IResource.class);
    }
    
    private void makeChildrenContainer() {
		children = new ArrayList<IXdsElement>();
	}

	@Override
	public void resourceChanged() {
	}
	
	/**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
    	IResource r = XdsElementCommons.adaptToResource(this, adapter);
    	if (r == null){
    		return super.getAdapter(adapter);
    	}
    	else {
    		return r;
    	}
    }
}