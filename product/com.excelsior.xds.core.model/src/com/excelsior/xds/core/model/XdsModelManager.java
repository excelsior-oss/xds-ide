package com.excelsior.xds.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;

import com.excelsior.xds.core.model.internal.XdsDbgScriptBundleFile;
import com.excelsior.xds.core.model.internal.XdsDdgScriptUnitFile;
import com.excelsior.xds.core.model.internal.XdsFolderContainer;
import com.excelsior.xds.core.model.internal.XdsModel;
import com.excelsior.xds.core.model.internal.XdsProject;
import com.excelsior.xds.core.model.internal.XdsProjectDescriptor;
import com.excelsior.xds.core.model.internal.XdsSymbolFile;
import com.excelsior.xds.core.model.internal.XdsTextFile;
import com.excelsior.xds.core.model.internal.XdsWorkspaceCompilationUnit;
import com.excelsior.xds.core.natures.NatureIdRegistry;
import com.excelsior.xds.core.project.NatureUtils;
import com.excelsior.xds.core.project.launcher.LaunchConfigurationsRemover;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.resource.XdsResourceChangeListener;
import com.excelsior.xds.core.utils.XdsFileUtils;

public class XdsModelManager extends XdsResourceChangeListener 
{
    private List<IXdsProject> xdsProjects;
    private final ReadWriteLock modelLock = new ReentrantReadWriteLock(); 
    private XdsModel model;
    
    // TODO : rewrite using subscriber/publisher pattern
    private final Queue<IProject> refreshDecoratorProjectsQueue = new LinkedBlockingQueue<IProject>();
    
    private XdsModelManager() {
    	super();
    }

    private static class XdsModelManagerHolder{
        static XdsModelManager INSTANCE = new XdsModelManager();
    }
    
    public static XdsModelManager getInstance(){
        return XdsModelManagerHolder.INSTANCE;
    }
    
    public static IXdsModel getModel() {
        return XdsModelManager.getInstance().doGetModel();
    }
    
    public static IEditableXdsModel getEditableModel() {
        return (IEditableXdsModel)XdsModelManager.getModel();
    }
    
    public static IXdsProject refreshProject(IProject p) {
    	IEditableXdsModel model = (IEditableXdsModel)XdsModelManager.getModel();
		try{
			return XdsModelManager.getInstance().doRefreshProject(p);
		}
		finally{
			model.notifyChanged();
		}
    	
    }
    
    /**
     * Refreshes 'External dependencies' and 'SDK Libraries' nodes.
     * @param p
     */
    public static void refreshExternals(IProject p) {
    	IEditableXdsModel model = (IEditableXdsModel)XdsModelManager.getModel();
		try{
			XdsModelManager.getInstance().doRefreshExternalDependencies(p);
			XdsModelManager.getInstance().doRefreshSdkLibrary(p);
		}
		finally{
			model.notifyChanged();
		}
    }
    
    private IXdsProject doRefreshProject(IProject p) {
    	XdsModel model = doGetModel();
		model.refreshProject(p);
    	return model.getXdsProjectBy(p);
    }
    
    private void doRefreshExternalDependencies(IProject p) {
    	XdsModel model = doGetModel();
        model.refreshExternalDependencies(p);
    }
    
    private void doRefreshSdkLibrary(IProject p) {
    	XdsModel model = doGetModel();
        model.refreshSdkLibrary(p);
    }
    
    private XdsModel doGetModel() {
    	Lock readLock = modelLock.readLock();
    	try{
    		readLock.lock();
    		if (model != null) {
    			return model;
    		}
    	}
    	finally{
    		readLock.unlock();
    	}
    	
    	Lock writeLock = modelLock.writeLock();
    	try{
    		writeLock.lock();
    		if (model != null) {
    			return model;
    		}
    		
    		xdsProjects = new ArrayList<IXdsProject>();
            Map<String, XdsProject> name2XdsProject = new HashMap<String, XdsProject>();
            IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
            for (IProject p : allProjects) {
                if (NatureUtils.hasNature(p, NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID)) {
                    XdsProject xdsProject = new XdsProject(p, null);
                    xdsProjects.add(xdsProject);  
                    name2XdsProject.put(p.getName(), xdsProject);
                }
            } 
            model = new XdsModel(xdsProjects, name2XdsProject);
            for (IXdsProject xdsProject : xdsProjects) {
            	((XdsProject)xdsProject).setModel(model);
			}
            model.expandModelNodes();
    	}
    	finally{
    		writeLock.unlock();
    	}
    	
        return model;
    }
    
	@Override
	protected void beginDeltaProcessing(IResourceDelta delta) {
		doGetModel();
	}
	
	@Override
	protected boolean handleResourceAdded(IResourceDelta rootDelta, IResourceDelta delta, IResource affectedResource) {
		XdsModel model = doGetModel();
		model.handleAddResource(rootDelta, affectedResource);
		return super.handleResourceAdded(rootDelta, delta, affectedResource);
	}
	
	@Override
	protected boolean handleResourceChanged(IResourceDelta rootDelta, IResourceDelta delta, IResource affectedResource) {
		XdsModel model =  doGetModel();
		boolean isContentChanged = isContentChanged(delta);
        model.handleChangeResource(rootDelta, affectedResource, isContentChanged);
		return super.handleResourceChanged(rootDelta, delta, affectedResource);
	}

	@Override
	protected boolean handleResourceRemoved(IResourceDelta rootDelta, IResourceDelta delta, IResource affectedResource) {
		XdsModel model =  doGetModel();
		model.handleRemoveResource(rootDelta, affectedResource);
		return super.handleResourceRemoved(rootDelta,delta, affectedResource);
	}
	
	@Override
	protected void handleProjectRemoved(IResourceDelta delta, IProject project, boolean isPreDeleteEvent) {
		if (isPreDeleteEvent) {
            LaunchConfigurationsRemover.removeAll(project);
        }
		XdsModel model =  doGetModel();
		model.handleRemoveResource(delta, project);
	}
	
	@Override
	protected void endDeltaProcessing(IResourceDelta rootDelta) {
		final IEditableXdsModel model = (IEditableXdsModel) doGetModel();
		model.endDeltaProcessing(rootDelta);
		model.notifyChanged();
	}

	public void enqueProjectForDecoratorRefresh(IProject p) {
    	refreshDecoratorProjectsQueue.add(p);
    }
    
    public IProject getNextProjectForDecoratorRefresh() {
    	return refreshDecoratorProjectsQueue.remove();
    }

    public static IXdsElement createFrom( XdsModel model, XdsProject xdsProject
                                 , IResource resource, IXdsContainer parent ) 
    {
    	String absolutePath = ResourceUtils.getAbsolutePath(resource);
        IProject project = resource.getProject();
        Assert.isTrue(NatureUtils.hasNature(project, NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID), "Xds model elements could only be created inside project with XDS nature"); //$NON-NLS-1$
        if (resource instanceof IProject) {
            return new XdsProject(project, model);
        } 
        else if (resource instanceof IContainer) {
            return new XdsFolderContainer(xdsProject, resource, parent);
        } 
        else if (XdsFileUtils.isCompilationUnitFile(absolutePath)) {
            return new XdsWorkspaceCompilationUnit(xdsProject, resource, parent);
        }
        else if (XdsFileUtils.isSymbolFile(absolutePath)) {
            return new XdsSymbolFile(xdsProject, resource, parent);
        }
        else if (XdsFileUtils.isXdsProjectFile(absolutePath)) {
            return new XdsProjectDescriptor(xdsProject, resource, parent);
        }
        else if (XdsFileUtils.isDbgScriptFile(absolutePath)) {
        	return new XdsDdgScriptUnitFile(xdsProject, resource, parent);
        }
        else if (XdsFileUtils.isDbgScriptBundleFile(absolutePath)) {
            return new XdsDbgScriptBundleFile(xdsProject, resource, parent);
        }
        else {
            return new XdsTextFile(xdsProject, resource, parent);
        }
    }
}
