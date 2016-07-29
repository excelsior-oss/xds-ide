package com.excelsior.xds.ui.todos;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.MultiRule;

import com.excelsior.xds.builder.buildsettings.BuildSettingsCache;
import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.compiler.compset.CompilationSetManager;
import com.excelsior.xds.core.compiler.compset.ICompilationSetListener;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.model.CompilationUnitType;
import com.excelsior.xds.core.model.IXdsResource;
import com.excelsior.xds.core.model.IXdsWorkspaceCompilationUnit;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.todotask.TodoTaskMarkerManager;
import com.excelsior.xds.parser.commons.ParserCriticalErrorReporter;
import com.excelsior.xds.parser.commons.TodoTaskParser.TaskEntry;
import com.excelsior.xds.parser.modula.XdsParserManager;
import com.excelsior.xds.ui.internal.nls.Messages;

/**
 * The builder of 'to-do' task markers for all Modula-2/Oberon-2 source files from workspace.
 * The comment parser is invoked for source files to search 'to-do' task. 
 * 
 * @author fsa, lion
 */
public class TodoTaskMarkerBuilder implements IResourceChangeListener, ICompilationSetListener  
{
    private volatile boolean isActive; 
    
    private static class TodoTaskMarkerBuilderHolder{
        static TodoTaskMarkerBuilder INSTANCE = new TodoTaskMarkerBuilder();
    }
    
    public TodoTaskMarkerBuilder() {
	}

	public static void install() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(TodoTaskMarkerBuilderHolder.INSTANCE);
		CompilationSetManager.getInstance().addCompilationSetListener(TodoTaskMarkerBuilderHolder.INSTANCE);
		TodoTaskMarkerBuilderHolder.INSTANCE.isActive = true;
    }

    public static void uninstall() {
    	TodoTaskMarkerBuilderHolder.INSTANCE.isActive = false;
    	CompilationSetManager.getInstance().removeCompilationSetListener(TodoTaskMarkerBuilderHolder.INSTANCE);
    }

    @Override
    public void added(String projectName, Collection<String> compilationSet) {
    	peformSearchOnCompilationSet(projectName, compilationSet);
    }

    @Override
    public void removed(String projectName, Collection<String> compilationSet) {
    	// no need to update todo task markers since resource doesnot exist anymore - 
    	// markers are deleted with the resource which contains them automatically
    }
    
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
    	IResourceDelta delta = event.getDelta();
    	if (delta != null) {
    		final List<IResource> affectedResources = new ArrayList<IResource>();
    		try {
				delta.accept(new IResourceDeltaVisitor() {
					@Override
					public boolean visit(IResourceDelta delta) throws CoreException {
						IResource resource = delta.getResource();
						if (resource instanceof IFile) {
							boolean isContentChanged = 0 != (delta.getFlags() & IResourceDelta.CONTENT);
							if (delta.getKind() == IResourceDelta.ADDED || ((delta.getKind() == IResourceDelta.CHANGED) && isContentChanged)) {
								affectedResources.add(resource);
							}
						}
						
						return true;
					}
				}, false);
				performSearchOnResources(affectedResources.iterator());
			} catch (CoreException e) {
				LogHelper.logError(e);
			}
    	}
	}

    private void peformSearchOnCompilationSet(String projectName, Collection<String> compilationSet) {
    	if (compilationSet != null) {
    		final Iterator<String> iterator = compilationSet.iterator();
    		final IProject project = ResourceUtils.getProject(projectName);
    		if (!project.exists() || !project.isOpen()) {
    			return;
    		}
    		performSearchOnResources(new Iterator<IResource>() {
    			@Override
    			public boolean hasNext() {
    				return iterator.hasNext();
    			}
    			
    			@Override
    			public IResource next() {
    				String path = iterator.next();
    				return ResourceUtils.getResource(project, new File(path).toURI());
    			}
    			
    			@Override
    			public void remove() {
    			}
    		});
    	}
    }
    
    private void performSearchOnResources(Iterator<IResource> resourcesIterator) {
		List<IResource> targetResources = new ArrayList<IResource>();
		
		for (; resourcesIterator.hasNext(); ) {
			IResource resource = resourcesIterator.next();
			if (resource != null) { 
				// resource can be null at the moment, when path to file
				// belongs to the external compilation unit, and it not yet have been mapped to the
				// workspace by builder.
				// @see com.excelsior.xds.core.compiler.compset.ICompilationSetListener
				
				// add resource even if it is not IXdsWorkspaceCompilationUnit, because we avoid accessing the XdsModel in this thread.
				targetResources.add(resource);
			}
		}
		
		if (!targetResources.isEmpty()) {
			TodoTaskSearchJob todoTaskSearchJob = new TodoTaskSearchJob(targetResources);
			todoTaskSearchJob.setRule(MultiRule.combine(targetResources.toArray(new IResource[0])));
			todoTaskSearchJob.schedule();
		}
	}
   
    private class TodoTaskSearchJob extends WorkspaceJob {
    	private List<IXdsWorkspaceCompilationUnit> affectedCompilationUnits;
        public TodoTaskSearchJob(List<IResource> targetResources) {
            super(Messages.TodoMarkerBuilder_JobName);
            setPriority(DECORATE);
            this.affectedCompilationUnits = targetResources.stream().map( r -> {
				IXdsResource xdsElement = XdsModelManager.getModel().getXdsElement(r);
				if (xdsElement instanceof IXdsWorkspaceCompilationUnit) {
					return (IXdsWorkspaceCompilationUnit)xdsElement;
				}
				else {
					return null;
				}
            }).filter(c -> c != null).collect(Collectors.toList());
        }

        public IStatus runInWorkspace(IProgressMonitor monitor) {
        	for (IXdsWorkspaceCompilationUnit cu : affectedCompilationUnits) {
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                
                try {
                    IResource res = cu.getResource();
                    if (!res.exists() || !(res instanceof IFile)) {
                        continue;
                    }

                    IFile iFile = (IFile)res;

                    if (isActive) {
                        String sourceText = ResourceUtils.getTextContent(iFile);
                        BuildSettings buildSettings = BuildSettingsCache.createBuildSettings(iFile);
                        
                        IMarker oldMarkers[] = TodoTaskMarkerManager.findMarkers(iFile);
                        if (cu.getCompilationUnitType() != CompilationUnitType.SYMBOL_FILE
        						&& cu.isInCompilationSet()) {
                        	TaskEntry[] taskEntries = XdsParserManager.parseTodoTaks(
                        			ResourceUtils.toFileStore(iFile), sourceText, buildSettings, 
                        			ParserCriticalErrorReporter.getInstance()
                        			);
                        	
                        	if (monitor.isCanceled()) {
                        		return Status.CANCEL_STATUS;
                        	}
                        	
                        	for (TaskEntry entry : taskEntries) {
                        		try {
                        			TodoTaskMarkerManager.updateMarkers(
                        					iFile, entry.getPosition(), entry.getEndOffset(),
                        					entry.getTask(), entry.getMessage(), oldMarkers
                        					);
                        		} catch (CoreException e) {
                        			LogHelper.logError(e);
                        		}
                        	}
                        	
                        	if (monitor.isCanceled()) {
                        		return Status.CANCEL_STATUS;
                        	}
        				}
                        
                        for (IMarker marker : oldMarkers) {
                        	if (monitor.isCanceled()) {
                        		return Status.CANCEL_STATUS;
                        	}
                            if (marker != null) {
                                try {
                                    marker.delete();
                                } catch (CoreException e) {
                                	LogHelper.logError(e);
                                }
                            }
                        } 
                    }
                    
                } catch (Exception e) {
                    LogHelper.logError(e);
                }
            }
        	return Status.OK_STATUS;
        }
    }
}
