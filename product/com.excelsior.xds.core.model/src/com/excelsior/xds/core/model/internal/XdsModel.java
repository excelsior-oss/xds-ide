package com.excelsior.xds.core.model.internal;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;

import com.excelsior.xds.core.compiler.compset.CompilationSetManager;
import com.excelsior.xds.core.filesystems.history.HistoryFs;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.model.IEditableXdsModel;
import com.excelsior.xds.core.model.IElementChangedListener;
import com.excelsior.xds.core.model.IXdsCompilationUnit;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsElementOperation;
import com.excelsior.xds.core.model.IXdsNonWorkspaceCompilationUnit;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsResource;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.core.model.utils.TraverseUtils;
import com.excelsior.xds.core.natures.NatureIdRegistry;
import com.excelsior.xds.core.resource.PathWithLocationType;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.utils.AdapterUtilities;
import com.excelsior.xds.core.utils.BuilderUtils;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;

/**
 * @author lsa80
 */
public class XdsModel implements IEditableXdsModel 
{
    private static boolean DEBUG_PRINT_OF_THE_CHANGE_NOTIFICATIONS = false;
    
    private final ReadWriteLock instanceLock = new ReentrantReadWriteLock(); 
    
    private List<IXdsProject> xdsProjects;
    private Map<String, XdsProject> name2XdsProject;
    
    private final Map<PathWithLocationType, IXdsElement> location2XdsElement  = new HashMap<PathWithLocationType, IXdsElement>();
    
    private final Set<IElementChangedListener> elementChangedListeners = CollectionsUtils.newConcurentHashSet();
    private final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    
	/**
	 * These projects will be rebuilt on the appropriate time. 
	 */
	private final Set<ProjectAndResourceDelta> projectsScheduledForRebuild = CollectionsUtils.newConcurentHashSet();
    
    public XdsModel(List<IXdsProject> xdsProjects, Map<String, XdsProject> name2XdsProject){
        this.xdsProjects = xdsProjects;
        for (IXdsProject p : xdsProjects) {
			putWorkspaceXdsElement(p.getProject(), p);
		}
        
        this.name2XdsProject = name2XdsProject;
    }

    /**
     * Walks every node and asks for the children - make them cash it - thus fully building the model
     */
    public XdsModel expandModelNodes() {
    	Lock writeLock = instanceLock.writeLock();
    	try{
    		writeLock.lock();
    		for (IXdsProject xdsProject : xdsProjects) {
    			expandNodes(xdsProject);
    		}
    	}
    	finally {
    		writeLock.unlock();
    	}
        return this;
    }
    
    @Override
    public String getElementName() {
        return "The Model"; //$NON-NLS-1$
    }

    @Override
    public List<IXdsProject> getXdsProjects() {
    	Lock readLock = instanceLock.readLock();
    	try{
    		readLock.lock();
    		return CollectionsUtils.unmodifiableArrayList(xdsProjects);
    	}
    	finally {
    		readLock.unlock();
    	}
    }

    @Override
    public IResource getResource() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    @Override
    public XdsProject getXdsProjectBy(IProject p) {
        IProjectNature nature;
        try {
            nature = p.isOpen() ? p.getNature(NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID) : null;
            if (nature == null){
                return null;
            }
        } catch (CoreException e) {
            LogHelper.logError(e);
            return null;
        }
        Lock readLock = instanceLock.readLock();
        try{
        	readLock.lock();
        	return name2XdsProject.get(p.getName());
        }
        finally {
        	readLock.unlock();
        }
    }
    
    @Override
    public IXdsProject getXdsProjectBy(IResource r) {
        IProject project = r.getProject();
        if (project == null) {
            return null;
        }
        return getXdsProjectBy(project);
    }
    
    @Override
    public IXdsResource getXdsElement(final IResource resource) {
    	Lock readLock = instanceLock.readLock();
    	try{
    		readLock.lock();
    		PathWithLocationType pathWithLocationType = new PathWithLocationType(resource);
    		return (IXdsResource)location2XdsElement.get(pathWithLocationType);
    	}
    	finally {
    		readLock.unlock();
    	}
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
	public IXdsElement getXdsElement(IEditorInput input) {
    	IXdsElement xdsElement = null;
        if (input instanceof IFileEditorInput) {
        	IFileEditorInput fileEditorInput = (IFileEditorInput)input;
        	IFile file = fileEditorInput.getFile();
            if (file != null) {
                xdsElement = XdsModelManager.getModel().getXdsElement(file);
            }
        }
        else {
        	URI uri = toURI(input);
        	IFileStore fileStore = ResourceUtils.toFileStore(uri);
        	xdsElement = XdsModelManager.getModel().getNonWorkspaceXdsElement(fileStore);
        }
        
		return xdsElement;
	}
    
    /**
     * TODO : common with CoreEditorUtils
     * @param input
     * @return
     */
    private static URI toURI(IEditorInput input) {
    	URI uri = null;
    	if (input instanceof IStorageEditorInput) {
    		IStorageEditorInput storageEditorInput = (IStorageEditorInput)input;
    		IFileRevision state = AdapterUtilities.getAdapter(storageEditorInput, IFileRevision.class);
    		if (state != null) {
    			uri = HistoryFs.toURI(state);
    		}
    	}
    	else if (input instanceof IURIEditorInput) {

    		IURIEditorInput uriEditorInput = (IURIEditorInput) input;
    		uri = uriEditorInput.getURI();
    	}

    	return uri;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IXdsElement getXdsElement(PathWithLocationType location) {
    	Lock readLock = instanceLock.readLock();
    	try{
    		readLock.lock();
    		return location2XdsElement.get(location);
    	}
    	finally {
    		readLock.unlock();
    	}
    }
    
    @Override
	public IXdsElement getNonWorkspaceXdsElement(IFileStore absoluteFile) {
		return getXdsElement(new PathWithLocationType(absoluteFile));
	}
    
    @Override
    public IXdsElement getParentXdsElement(IResource r) {
    	IXdsResource xdsElement = getXdsElement(r);
        return xdsElement != null ? xdsElement.getParent() : null;
    }
    
    public void refreshProject(final IProject p) {
    	Lock writeLock = instanceLock.writeLock();
    	try{
    		writeLock.lock();
    		XdsProject xdsProject = getXdsProjectBy(p);
    		if (xdsProject != null) {
    			removeProjectFromModel(xdsProject);
    		}
    		xdsProject = new XdsProject(p, this);
    		if (xdsProject != null) {
    			addProjectToModel(xdsProject);
    			expandNodes(xdsProject);
    		}
    	}
    	finally {
    		writeLock.unlock();
    	}
    }
    
    public void refreshExternalDependencies(IProject p) {
        IXdsProject xdsProject = getXdsProjectBy(p);
        Assert.isNotNull(xdsProject);
        xdsProject.refreshExternalDependencies();
    }
    
    public void refreshSdkLibrary(IProject p) {
        IXdsProject xdsProject = getXdsProjectBy(p);
        Assert.isNotNull(xdsProject);
        xdsProject.refreshSdkLibrary();
    }
    
    @Override
	public void endDeltaProcessing(IResourceDelta rootDelta) {
    	rebuildPostponedProjects(rootDelta);
	}

	/**
	 * @param rootDelta - current delta being processed. 
	 */
	private void rebuildPostponedProjects(IResourceDelta rootDelta) {
    	Set<ProjectAndResourceDelta> forRebuildScheduled;
    	
    	// Idea is the following : process add-project delta in the next change-delta (it always comes after the add-project delta).
    	synchronized (projectsScheduledForRebuild) {
    		forRebuildScheduled= projectsScheduledForRebuild
					.stream()
					.filter(e -> e.rootDelta != rootDelta
							|| e.rootDelta == null).collect(Collectors.toSet());
    		projectsScheduledForRebuild.removeAll(forRebuildScheduled);
		}
    	
    	forRebuildScheduled.forEach(e -> rebuild(e.project));
	}
    
    /**
     * Will postpone re-building of the project until the next delta.
     * @param rootDelta - when non-null, this {@link project} will be scheduled for rebuild on next delta. If null, will be scheduled during the processing of the current delta.
     * @param project - project to be scheduled for rebuild.
     */
    private void postponeProjectForRebuild(IResourceDelta rootDelta, IProject project) {
		synchronized (projectsScheduledForRebuild) {
			projectsScheduledForRebuild.add(new ProjectAndResourceDelta(project, rootDelta));
		}
	}
    
    /**
     * @param project
     */
    private void postponeProjectForRebuild(IProject project) {
    	postponeProjectForRebuild(null, project);
	}
    
	@Override
    public void handleAddResource(IResourceDelta rootDelta, final IResource affectedResource) {
        if (DEBUG_PRINT_OF_THE_CHANGE_NOTIFICATIONS) System.out.println("Add resource : " + affectedResource); //$NON-NLS-1$
        
        if (affectedResource instanceof IProject) {
            refreshProject(affectedResource.getProject());
            postponeProjectForRebuild(rootDelta, affectedResource.getProject());
        }
        else {
        	if (affectedResource.isVirtual()) {
        		return;
        	}
        	Lock writeLock = instanceLock.writeLock();
        	try{
        		writeLock.lock();
        		
                IXdsElement tmpElement = location2XdsElement.get(new PathWithLocationType(affectedResource.getParent()));
                // TODO : sometimes we will have null here (say for some descendant of .externals folder). 
                if (tmpElement instanceof IEditableXdsFolderContainer) {
                    IEditableXdsFolderContainer xdsElementParent = (IEditableXdsFolderContainer) tmpElement;
                    if (!xdsElementParent.getChildResources().contains(affectedResource)) {
                        XdsProject xdsProject = getXdsProjectBy(affectedResource.getProject());
                        IXdsElement xdsElement = XdsModelManager.createFrom(this, xdsProject, affectedResource, xdsElementParent);
                        if (xdsElement != null ) { // it can be null for say .project
                            xdsElementParent.addChild((IXdsResource)xdsElement); // it can only be instanceof IXdsResource
                        }
                    }
                }
        	}
        	finally {
        		writeLock.unlock();
        	}
        }
    }

    private IXdsElement addProjectToModel(XdsProject xdsProject) {
    	Lock writeLock = instanceLock.writeLock();
    	try{
    		writeLock.lock();
    		
    		IProject project = (IProject) xdsProject.getResource();
            xdsProjects.add(xdsProject);
            putWorkspaceXdsElement(project, xdsProject);
            return name2XdsProject.put(xdsProject.getResource().getName(), xdsProject);
    	}
    	finally {
    		writeLock.unlock();
    	}
    }
    
    private IXdsElement removeProjectFromModel(IXdsProject xdsProject) {
    	Lock writeLock = instanceLock.writeLock();
    	try{
    		writeLock.lock();
    		
    		IProject project = (IProject) xdsProject.getResource();
            xdsProjects.remove(xdsProject);
            removeWorkspaceXdsElement(project);
            return name2XdsProject.remove(xdsProject.getResource().getName());
    	}
    	finally {
    		writeLock.unlock();
    	}
    }

    @Override
    public void handleChangeResource(IResourceDelta rootDelta, IResource affectedResource, boolean isContentChanged) {
        if (DEBUG_PRINT_OF_THE_CHANGE_NOTIFICATIONS) System.out.println("Change resource : " + affectedResource); //$NON-NLS-1$
        if (workspaceRoot == affectedResource) { // ignore plain workspace changes
            return;
        }
        
        if (affectedResource instanceof IProject) {
            IProject p = (IProject)affectedResource;
            IXdsProject xdsProject = getXdsProjectBy(p);
            if (xdsProject == null) { // this will be the case when the project is re-opened
                refreshProject(p);
                postponeProjectForRebuild(affectedResource.getProject());
            }
        }
        else if (affectedResource instanceof IFile) {
            IXdsResource xdsElement = getWorkspaceXdsElement(affectedResource);
            if (xdsElement != null) {
                xdsElement.resourceChanged();
            }
        }
    }

	private void rebuild(IProject p) {
		try {
			BuilderUtils.invokeRebuild(p, new NullProgressMonitor());
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
	}

    @Override
    public void handleRemoveResource(IResourceDelta rootDelta, IResource affectedResource) {
        if (DEBUG_PRINT_OF_THE_CHANGE_NOTIFICATIONS) System.out.println("Remove resource : " + affectedResource); //$NON-NLS-1$
        
        Lock writeLock = instanceLock.writeLock();
        try{
        	writeLock.lock();
        	
            IXdsResource xdsElement = getWorkspaceXdsElement(affectedResource);
            if (xdsElement instanceof IXdsProject) { // just remove project
                removeProjectFromModel((IXdsProject)xdsElement);
                CompilationSetManager.getInstance().removeFromCompilationSet(affectedResource.getProject());
            }
            else if (xdsElement != null) { // it can be null for say .project resource
            	IXdsResource tmpElement = getWorkspaceXdsElement(affectedResource.getParent());
            	if (tmpElement instanceof IEditableXdsFolderContainer) {
            	    IEditableXdsFolderContainer xdsElementParent = (IEditableXdsFolderContainer) tmpElement;
                    Assert.isNotNull(xdsElementParent, "Non-project element should always has parent"); //$NON-NLS-1$
                    if (xdsElementParent.getChildResources().contains(affectedResource)) {
                        xdsElementParent.removeChild(xdsElement);
                    }
            	}
                
                if (xdsElement instanceof IXdsCompilationUnit) {
                    String path = ResourceUtils.getAbsolutePath(affectedResource);
                    CompilationSetManager.getInstance().removeFromCompilationSet(affectedResource.getProject(), path);
                }
            }
        }
        finally {
        	writeLock.unlock();
        }
    }
    
    @Override
    public IXdsContainer getParent() {
        return null;
    }
    
    public void addElementChangedListener(IElementChangedListener listener) {
        elementChangedListeners.add(listener);
    }
    
    public void removeElementChangedListener(IElementChangedListener listener) {
        elementChangedListeners.remove(listener);
    }
    
    private void expandNodes(IXdsResource xdsElement) {
        TraverseUtils.walk(xdsElement, e -> {});
    }
    
    private void notifyElementChangedListeners() {
    	for (IElementChangedListener listener : elementChangedListeners) {
    		listener.elementChanged();
		}
    }
    
    @Override
    public void notifyChanged() {
        if (DEBUG_PRINT_OF_THE_CHANGE_NOTIFICATIONS) System.out.println("END handle event:"+System.currentTimeMillis()); //$NON-NLS-1$
        notifyElementChangedListeners();
    }

//XXX    @Override
//    public String toString() {
//        return "The Model"; //$NON-NLS-1$
//    }

    @Override
    public void resourceChanged() {
    }

    /*
     * TODO : refactor model hierarchy to remove this method from here
     */
    @Override
    public IXdsProject getXdsProject() {
        throw new RuntimeException("Should never be called"); //$NON-NLS-1$
    }

    @Override
    public void editElement(IXdsElement element, IXdsElementOperation operation) {
        operation.invoke(element);
        notifyChanged();
    }
    
    void putWorkspaceXdsElement(IResource r, IXdsResource xdsElement) {
    	Lock writeLock = instanceLock.writeLock();
    	try{
    		writeLock.lock();
    		location2XdsElement.put(new PathWithLocationType(r) , xdsElement);
    	}
    	finally {
    		writeLock.unlock();
    	}
	}
    
    public void putNonWorkspaceXdsElement(IFileStore sourceFile,
    		IXdsElement xdsElement) {
    	Lock writeLock = instanceLock.writeLock();
    	try{
    		writeLock.lock();
    		location2XdsElement.put(new PathWithLocationType(sourceFile) , xdsElement);
    	}
    	finally {
    		writeLock.unlock();
    	}
	}
    
    private IXdsResource getWorkspaceXdsElement(IResource r) {
    	Lock readLock = instanceLock.readLock();
    	try{
    		readLock.lock();
    		return (IXdsResource)location2XdsElement.get(new PathWithLocationType(r));
    	}
    	finally {
    		readLock.unlock();
    	}
    }
    
    void removeWorkspaceXdsElement(IResource r) {
    	Lock writeLock = instanceLock.writeLock();
    	try{
    		writeLock.lock();
    		location2XdsElement.remove(new PathWithLocationType(r));
    	}
    	finally {
    		writeLock.unlock();
    	}
    }

	@Override
	public IXdsNonWorkspaceCompilationUnit createNonWorkspaceXdsElement(IFileStore sourceFile) {
		IXdsNonWorkspaceCompilationUnit nonWorkspaceXdsElement = new XdsNonWorkspaceCompilationUnit(sourceFile, null,
				null);
		putNonWorkspaceXdsElement(
				sourceFile, nonWorkspaceXdsElement);
		return nonWorkspaceXdsElement;
	}

	@Override
	public void removeNonWorkspaceXdsElement(
			IXdsNonWorkspaceCompilationUnit compilationUnit) {
		Lock writeLock = instanceLock.writeLock();
		try{
			writeLock.lock();
			location2XdsElement.remove(new PathWithLocationType(compilationUnit.getAbsoluteFile()));
		}
		finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * {@link #rootDelta} - represents moment of time (processing of the appropriate ResourceDelta), associated with this project.<br>
	 * @author lsa80
	 */
	private static class ProjectAndResourceDelta {
		final IProject project;
		final IResourceDelta rootDelta;
		
		ProjectAndResourceDelta(IProject project,
				IResourceDelta rootDelta) {
			this.project = project;
			this.rootDelta = rootDelta;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((project == null) ? 0 : project.hashCode());
			result = prime * result
					+ ((rootDelta == null) ? 0 : rootDelta.hashCode());
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
			ProjectAndResourceDelta other = (ProjectAndResourceDelta) obj;
			if (project == null) {
				if (other.project != null)
					return false;
			} else if (!project.equals(other.project))
				return false;
			if (rootDelta == null) {
				if (other.rootDelta != null)
					return false;
			} else if (!rootDelta.equals(other.rootDelta))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "<<project=" + project + ">>";
		}
	}
}
