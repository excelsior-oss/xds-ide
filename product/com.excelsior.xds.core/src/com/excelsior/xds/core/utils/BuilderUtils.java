package com.excelsior.xds.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.excelsior.xds.core.builders.XdsBuildResult;
import com.excelsior.xds.core.builders.XdsSourceBuilderConstants;
import com.excelsior.xds.core.compiler.libset.LibraryFileSetManager;
import com.excelsior.xds.core.internal.nls.Messages;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.marker.MarkerUtils;
import com.excelsior.xds.core.resource.ResourceUtils;

public final class BuilderUtils {
    public static final String MULTIPLE_BUILD_ITEM_ID = "MULTIPLE_BUILD_ITEM_ID";  //$NON-NLS-1$
    public static final String BUILD_JOB_FAMILY = "BUILD_JOB_FAMILY"; //$NON-NLS-1$
    
    private static Set<MultipleBuildGroup> multipleBuildsGroups = new HashSet<MultipleBuildGroup>();
    
    public static enum BuildAction {
    	REFRESH_PROJECT,
    	BUILD_PROJECT,
    	REBUILD_PROJECT,
    	REBUILD_LIBRARY_FILESET,
    	GET_COMPILATION_SET
    }
    
    /**
     * Only static methods are allowed
     */
    private BuilderUtils(){}
    
    public static Job invokeBuilder(IProject project, EnumSet<BuildAction> actions, IProgressMonitor monitor) throws CoreException {
    	return configureAndSchedule(createBuildJob(project, actions));
    }

	public static Job createBuildJob(IProject project,
			EnumSet<BuildAction> actions) {
		BuildJob buildJob = new BuildJob(project, Messages.BuilderUtils_BuildingProject) {
    		
    		private void onAction(BuildAction action, EnumSet<BuildAction> actions, Map<String, String> args, String arg) {
    			if (actions.contains(action)) {
    				args.put(arg, Boolean.TRUE.toString());
    			}
    		}
    		
			@Override
			protected void doBuild(IProject project, IProgressMonitor monitor) throws CoreException {
				Map<String, String> args = new HashMap<>();
				
				if (actions.contains(BuildAction.REBUILD_LIBRARY_FILESET)) {
					LibraryFileSetManager.getInstance().updateFrom(project);
				}
				if (actions.contains(BuildAction.REFRESH_PROJECT)) {
					ResourceUtils.refreshLocalSync(project);
				}
				
				onAction(BuildAction.BUILD_PROJECT, actions, args, XdsSourceBuilderConstants.BUILD_PROJECT_KEY);
				onAction(BuildAction.REBUILD_PROJECT, actions, args, XdsSourceBuilderConstants.REBUILD_PROJECT_KEY);
				onAction(BuildAction.REBUILD_LIBRARY_FILESET, actions, args, XdsSourceBuilderConstants.GET_LIBRARY_FILE_SET_KEY);
				onAction(BuildAction.GET_COMPILATION_SET, actions, args, XdsSourceBuilderConstants.GET_COMPILATION_SET_ONLY_KEY);
				project.build(IncrementalProjectBuilder.FULL_BUILD, XdsSourceBuilderConstants.BUILDER_ID, args, monitor);
			}
		};
		buildJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		return buildJob;
	}

	public static void invokeRebuild(IProject project, IProgressMonitor monitor) throws CoreException {
		invokeBuilder(project, EnumSet.of(BuildAction.REBUILD_PROJECT), monitor);
	}
	
	public static void invokeRebuildAndGetLibraryFileSet(IProject project, IProgressMonitor monitor) throws CoreException {
		invokeBuilder(project, EnumSet.of(BuildAction.REBUILD_PROJECT, BuildAction.REBUILD_LIBRARY_FILESET), monitor);
	}

	public static void invokeBuild(IProject project, IProgressMonitor monitor) throws CoreException {
	    invokeBuilder(project, EnumSet.of(BuildAction.BUILD_PROJECT), monitor);
	}
    
    public static Job invokeGetCompilationSet(IProject project, IProgressMonitor monitor) throws CoreException {
    	return invokeBuilder(project, EnumSet.of(BuildAction.GET_COMPILATION_SET), monitor);
    }
    
    public static void invokeGetCompilationSetAndGetLibraryFileSet(IProject project, IProgressMonitor monitor) throws CoreException {
    	invokeBuilder(project, EnumSet.of(BuildAction.GET_COMPILATION_SET, BuildAction.REBUILD_LIBRARY_FILESET), monitor);
    }
    
    public static void invokeMultipleBuild(List<MultipleBuildItem> itemsToBuild, IMiltipleBuildFinishListener listener) {
        final MultipleBuildGroup group = new MultipleBuildGroup(listener);
        multipleBuildsGroups.add(group);
        for (final MultipleBuildItem item : itemsToBuild) {
        	BuildJob buildJob= new BuildJob(item.project, Messages.BuilderUtils_BuildingProject) {
    			@Override
    			protected void doBuild(IProject project, IProgressMonitor monitor) throws CoreException {
    				item.args.put(MULTIPLE_BUILD_ITEM_ID, item.itemUnicalId);
                    group.addItem(item);
                    project.build(item.buildKind, XdsSourceBuilderConstants.BUILDER_ID, item.args, monitor);
    			}
    		};
    		configureAndSchedule(buildJob);
        }
        
    }
    
    /**
     * TRUE when this item is in MultipleBuildGroup and some items in this
     * group are finished. (used to don't clear console output between builds in a group) 
     */
    public static boolean isNotFirstInMultipleBuild(String itemId) {
        if (itemId != null) {
            for (MultipleBuildGroup group : multipleBuildsGroups) {
                if (group.findItem(itemId) != null) {
                    return group.hasFinishedItems();
                }
            }
        }
        return false;
    }

    /**
     * TRUE when this item is in MultipleBuildGroup this group contains more than one this item
     */
    public static boolean isInRealMultipleBuild(String itemId) {
        for (MultipleBuildGroup group : multipleBuildsGroups) {
            if (group.findItem(itemId) != null) {
                return group.getItemsCount() > 1;
            }
        }
        return false;
    }
    
    public static void multipleBuildItemFinished(String itemId, XdsBuildResult buildResult, String summaryString) {
        for (MultipleBuildGroup group : multipleBuildsGroups) {
            if (group.finishItem(itemId, buildResult, summaryString)) {
                if (group.isAllFinished()) {
                    group.callListener();
                    multipleBuildsGroups.remove(group);
                    break;
                }
                
            }
        }
        
    }
    
    /**
	 * Add this builder to the specified project if possible. Do nothing if the
	 * builder has already been added.
	 * 
	 * @param project
	 *            the project (not <code>null</code>)
	 */
	public static void addBuilderToProject(IProject project, String BUILDER_ID) {
		// Cannot modify closed projects.
	      if (!project.isOpen())
	         return;

	      // Get the description.
	      IProjectDescription description;
	      try {
	         description = project.getDescription();
	      }
	      catch (CoreException e) {
	    	  LogHelper.logError(e);
	         return;
	      }

	      // Look for builder already associated.
	      ICommand[] cmds = description.getBuildSpec();
	      for (int j = 0; j < cmds.length; j++)
	         if (cmds[j].getBuilderName().equals(BUILDER_ID))
	            return;

	      // Associate builder with project.
	      ICommand newCmd = description.newCommand();
	      newCmd.setBuilderName(BUILDER_ID);
	      List<ICommand> newCmds = new ArrayList<ICommand>();
	      newCmds.addAll(Arrays.asList(cmds));
	      newCmds.add(newCmd);
	      description.setBuildSpec((ICommand[]) newCmds.toArray(new ICommand[newCmds.size()]));
	      try {
	         project.setDescription(description, null);
	      }
	      catch (CoreException e) {
	    	  LogHelper.logError(e);
	      }
	}
	
	// ---------- Multiple build support
	
	private static class MultipleBuildGroup {
	    private ArrayList<MultipleBuildItem> items;
	    private IMiltipleBuildFinishListener listener;
	    
	    public MultipleBuildGroup(IMiltipleBuildFinishListener listener) {
	        items = new ArrayList<MultipleBuildItem>();
	        this.listener = listener;
	    }
	    
        public void addItem(MultipleBuildItem it) {
            items.add(it);
        }
        
	    public boolean finishItem(String itemId, XdsBuildResult buildResult, String summaryString) {
	        MultipleBuildItem it = findItem(itemId);
            if (it != null) {
                it.buildResult = buildResult;
                it.summaryString = summaryString;
                return true;
            }
	        return false;
	    }
	    
	    public MultipleBuildItem findItem(String itemId) {
            for (MultipleBuildItem it : items) {
                if (it.itemUnicalId.equals(itemId)) {
                    return it;
                }
            }
            return null;
	    }
	    
        public boolean isAllFinished() {
            for (MultipleBuildItem it : items) {
                if (it.summaryString == null)
                    return false;
            }
            return true;
        }
        
        public boolean hasFinishedItems() {
            for (MultipleBuildItem it : items) {
                if (it.summaryString != null)
                    return true;
            }
            return false;
        }
        
        public int getItemsCount() {
            return items.size();
        }
        
        public void callListener() {
            if (listener != null) {
                listener.allItemsAreFinished();
            }
        }
	}
	
	public static class MultipleBuildItem {
	    private int buildKind;
	    private IProject project;
	    private Map<String, String> args;
	    private final String itemUnicalId;
	    // Build results:
	    private XdsBuildResult buildResult;
	    private String summaryString; // null => not finished
	    
	    private static int idcnt = 0;
	    
	    public MultipleBuildItem(int buildKind, IProject project, Map<String, String> args) {
	        this.buildKind = buildKind;
	        this.project = project;
	        this.args = args;
	        this.itemUnicalId = "mbi_"+idcnt++; //$NON-NLS-1$
	    }
	    
	    public XdsBuildResult getBuildResult() {
	        return buildResult;
	    }
	    
	    public String getSummaryString() {
	        return summaryString;
	    }
	}
	
	public interface IMiltipleBuildFinishListener {
	    public void allItemsAreFinished();
	}
	
	public static boolean setAutoBuilding(boolean state) throws CoreException {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceDescription desc = workspace.getDescription();
        boolean isAutoBuilding = desc.isAutoBuilding();
        if (isAutoBuilding != state) {
            desc.setAutoBuilding(state);
            workspace.setDescription(desc);
        }
        return isAutoBuilding;
    }
	
	public static void applySdkToProject(final IProject p, boolean isRebuild, IProgressMonitor monitor) throws CoreException {
		LibraryFileSetManager.getInstance().updateFrom(p);
		
		if (isRebuild) {
			invokeRebuildAndGetLibraryFileSet(p, monitor);
		}
		else {
			// since we have changed SDK - remove all markers from the previous build
			MarkerUtils.scheduleDeleteMarkers(p);
			invokeGetCompilationSetAndGetLibraryFileSet(p, monitor);
		}
	}
	
	private static Job configureAndSchedule(Job buildJob) {
		buildJob.setUser(true);
		buildJob.schedule();
		return buildJob;
	}
}
