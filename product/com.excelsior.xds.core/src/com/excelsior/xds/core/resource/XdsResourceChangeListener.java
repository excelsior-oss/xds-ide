package com.excelsior.xds.core.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.natures.NatureIdRegistry;
import com.excelsior.xds.core.project.NatureUtils;
import com.excelsior.xds.core.project.SpecialFolderNames;
import com.excelsior.xds.core.utils.XdsFileUtils;

public abstract class XdsResourceChangeListener implements IResourceChangeListener {

	/**
	 * Installs the {@link IWorkspace#addResourceChangeListener(IResourceChangeListener)}
	 */
	public XdsResourceChangeListener() {
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			if (workspace != null) {
				workspace.addResourceChangeListener(this);
			}
		} catch (java.lang.IllegalStateException e) {
		}
	}
	
	/**
	 * Uninstalls the {@link IWorkspace#removeResourceChangeListener(IResourceChangeListener)}
	 */
	public void stop(){
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			if (workspace != null) {
				workspace.removeResourceChangeListener(this);
			}
		} catch (java.lang.IllegalStateException e) {
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		doVisit(event);
	}

	protected VisitResult doVisit(IResourceChangeEvent event) {
		final VisitResult visitResult = new VisitResult();
		IResourceDelta delta = event.getDelta();
        try {
            if (delta != null) { // it can be null for project removals
//            	printDelta(delta);
                beginDeltaProcessing(delta);
                delta.accept(new IResourceDeltaVisitor() {
                    @Override
                    public boolean visit(IResourceDelta childDelta) throws CoreException {
                        IResource affectedResource = childDelta.getResource();
                        if (!(affectedResource instanceof IWorkspaceRoot)) {
                            if (!NatureUtils.hasNature(affectedResource.getProject(), NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID)) {
                                return false; // DONT visit children of non-XDS nature project
                            }
                            
                            if (ResourceUtils.isInsideFolder(SpecialFolderNames.SETTINGS_DIR_NAME, affectedResource) ||
                            	ResourceUtils.isInsideFolder(SpecialFolderNames.EXTERNAL_DEPENDENCIES_DIR_NAME, affectedResource)) {
                                return false; // DONT visit children of .settings and .mnt directories
                            }
                            
                            boolean isChildrenShouldBeVisited;
                            switch (childDelta.getKind()) {
                            case IResourceDelta.ADDED:
                                isChildrenShouldBeVisited = handleResourceAdded(delta, childDelta, affectedResource);
                                visitResult.addedResourceDeltas.add(childDelta);
                                break;
                            case IResourceDelta.REMOVED:
                                isChildrenShouldBeVisited = handleResourceRemoved(delta, childDelta, affectedResource);
                            	visitResult.removedResourceDeltas.add(childDelta);
                                break;
                            case IResourceDelta.CHANGED:
                            	isChildrenShouldBeVisited = handleResourceChanged(delta, childDelta, affectedResource);
                            	visitResult.changedResourceDeltas.add(childDelta);
                                break;
                            default:
                            	isChildrenShouldBeVisited = true;
                                break;
                            }
                            return isChildrenShouldBeVisited;
                        }
                        return true;
                    }
                });
            } else {
            	boolean isPreDeleteEvent = event.getType() == IResourceChangeEvent.PRE_DELETE;
                if (isPreDeleteEvent || event.getType() == IResourceChangeEvent.PRE_CLOSE) {
                    IResource resource = event.getResource();
                    if (resource instanceof IProject) {
                        IProject project = (IProject)resource;
                        if (NatureUtils.hasNature(project, NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID)) {
                            handleProjectRemoved(delta, project, isPreDeleteEvent);
                            visitResult.removedResourceDeltas.add(delta);
                        }
                    }
            		
            	}
            }
        } catch (CoreException e) {
            LogHelper.logError(e);
        }
        finally{
        	endDeltaProcessing(delta);
        }
        return visitResult;
	}

	/**
	 * TODO : use this method to improve performance of the resource change listening.
	 * @param delta
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean hasModula2Projects(IResourceDelta delta) {
		IResourceDelta[] affectedProjects = delta.getAffectedChildren();
		return Arrays.stream(affectedProjects)
				.map(rd -> rd.getResource().getProject())
				.filter(p -> NatureUtils.hasModula2Nature(p))
				.findAny().isPresent();
	}
	
	protected static class VisitResult{
		public final List<IResourceDelta> addedResourceDeltas = new ArrayList<IResourceDelta>();
		public final List<IResourceDelta> changedResourceDeltas = new ArrayList<IResourceDelta>();
		public final List<IResourceDelta> removedResourceDeltas = new ArrayList<IResourceDelta>();
	}
	
	protected void printDelta(IResourceDelta delta) {
		if (delta == null) {
			return;
		}
		final StringBuffer buf = new StringBuffer(80);
		final String className = this.getClass().getName();
		try {
			delta.accept(new IResourceDeltaVisitor() {
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					if (isContentChanged(delta)) {
						buf.append(className + "\r\n");
						buf.append("/");
						switch (delta.getKind()) {
						case IResourceDelta.ADDED:
							buf.append("ADDED"); //$NON-NLS-1$
							break;
						case IResourceDelta.REMOVED:
							buf.append("REMOVED"); //$NON-NLS-1$
							break;
						case IResourceDelta.CHANGED:
							buf.append("CHANGED"); //$NON-NLS-1$
							break;
						default:
							buf.append("["); //$NON-NLS-1$
							buf.append(delta.getKind());
							buf.append("]"); //$NON-NLS-1$
							break;
						}
						buf.append(" (( "); //$NON-NLS-1$
						buf.append(delta.getResource());
						buf.append(" ))"); //$NON-NLS-1$
						buf.append("\r\n");
					}
					return true;
				}
			});
		} catch (CoreException e) {
		}
		System.out.println(buf);
	}

	/**
	 * Called before the traversing of the delta
	 * @param rootDelta root of the delta
	 */
	protected void beginDeltaProcessing(IResourceDelta rootDelta) {
	}
	
	/**
	 * Called after the traversing of the delta
	 * @param rootDelta root of the delta
	 */
	protected void endDeltaProcessing(IResourceDelta rootDelta) {
	}

	/**
	 * Called for each project removed in this delta 
	 * @param delta
	 * @param affectedProject
	 * @param isPreDeleteEvent
	 */
	protected void handleProjectRemoved(IResourceDelta delta, IProject affectedProject, boolean isPreDeleteEvent) {
	}
	
	/**
	 * Called for each resource added in this delta 
	 * @param rootDelta root of the reported delta
	 * @param childDelta child delta
	 * @param affectedResource resource from the child delta 
	 * @return true if continue with traversing of the child deltas
	 */
	protected boolean handleResourceAdded(IResourceDelta rootDelta, IResourceDelta childDelta, IResource affectedResource) {
		return !(affectedResource instanceof IProject);
	}
	
	/**
	 * Called for each resource changed in this delta 
	 * @param rootDelta root of the reported delta
	 * @param childDelta child delta
	 * @param affectedResource resource from the child delta 
	 * @return true if continue with traversing of the child deltas
	 */
	protected boolean handleResourceChanged(IResourceDelta rootDelta, IResourceDelta childDelta, IResource affectedResource) {
		return true;
	}

	/**
	 * Called for each resource removed in this delta 
	 * @param rootDelta root of the reported delta
	 * @param childDelta child delta
	 * @param affectedResource resource from the child delta 
	 * @return true if continue with traversing of the child deltas
	 */
	protected boolean handleResourceRemoved(IResourceDelta rootDelta, IResourceDelta childDelta, IResource affectedResource) {
		return !(affectedResource instanceof IProject);
	}
	
	protected boolean isCompilationUnitFile(IResource r) {
		if (r instanceof IFile) {
			IFile ifile = (IFile) r;
			return XdsFileUtils.isCompilationUnitFile(ifile);
		}
		
		return false;
	}
	
	protected boolean isContentChanged(IResourceDelta delta) {
		return (delta.getFlags() & IResourceDelta.CONTENT) != 0;
	}
}
