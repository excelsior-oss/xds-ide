package com.excelsior.xds.core.compiler.compset;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import com.excelsior.xds.core.internal.nls.Messages;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.marker.XdsMarkerConstants;
import com.excelsior.xds.core.project.SpecialFolderNames;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.utils.JavaUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public final class ExternalResourceManager {
	private static final String LINK_DIRECTORY_PROBLEM_MARKER_TYPE = XdsMarkerConstants.BUILD_PROBLEM_MARKER_TYPE;
	
	public static final void linkExternals(IProject p, Collection<File> externalDirectories, boolean isLinkSdkDirectory,
			boolean isLinkExternalDirectories, IProgressMonitor monitor)
			throws CoreException {
		Collection<IStatus> linkStatuses = new ArrayList<>();
		if (isLinkExternalDirectories) {
			linkStatuses.addAll(ExternalResourceManager.linkExternalDirectories(p, externalDirectories, SubMonitor.convert(monitor)));
        }
        
        if (isLinkSdkDirectory) {
        	linkStatuses.add(ExternalResourceManager.linkSdkDirectory(p, SubMonitor.convert(monitor)));
        }
        if (isLinkExternalDirectories || isLinkSdkDirectory) {
        	WorkspaceJob markersJob = new WorkspaceJob(Messages.ExternalResourceManager_UpdateOfProblemMarkers) {
    			@Override
    			public IStatus runInWorkspace(IProgressMonitor monitor)
    					throws CoreException {
    				p.deleteMarkers(LINK_DIRECTORY_PROBLEM_MARKER_TYPE, true, 1);
    	        	createLinkProblemMarkers(p, linkStatuses);
    				return Status.OK_STATUS;
    			}
    		};
    		markersJob.setRule(p);
    		markersJob.schedule();
        	ExternalResourceManager.cleanUnusedLinkedDirectories(p, externalDirectories);
        }
	}
	
	private static void createLinkProblemMarkers(IProject p, Collection<IStatus> linkStatuses) throws CoreException {
		for (IStatus status : linkStatuses) {
			if (!status.isOK()) {
				int statusSeverity = status.getSeverity();
				int markerSeverity;
				if (statusSeverity == IStatus.ERROR) {
					markerSeverity = IMarker.SEVERITY_ERROR;
				}
				else if (statusSeverity == IStatus.WARNING) {
					markerSeverity = IMarker.SEVERITY_WARNING;
				}
				else {
					markerSeverity = IMarker.SEVERITY_INFO;
				}
				
				IMarker marker = p.createMarker(LINK_DIRECTORY_PROBLEM_MARKER_TYPE);
				marker.setAttribute(IMarker.MESSAGE, String.format(Messages.ExternalResourceManager_ErrorWhenLinkingExternalDirectory, status.getMessage()));
				marker.setAttribute(IMarker.SEVERITY, markerSeverity);
			}
		}
	}
	
	private static final Collection<IStatus> linkExternalDirectories(IProject p, Collection<File> externalDirectories, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(Messages.ExternalResourceManager_LinkingExternalFiles, externalDirectories.size());
		
		Collection<IStatus> linkStatuses = new ArrayList<>();
		
		recreateExternalsFolder(p.getName(), monitor);
		IFolder externalsFolder = p.getFolder(SpecialFolderNames.VIRTUAL_MOUNT_ROOT_DIR_NAME);
		Path projectPath = new Path(ResourceUtils.getAbsolutePath(p));
		
		try{
			for (File dir : externalDirectories) {
				boolean isInsideProject = projectPath.isPrefixOf(new Path(dir.getAbsolutePath()));
				if (dir.isDirectory() && !isInsideProject) {
					linkStatuses.add(linkDirectory(p, externalsFolder, dir, monitor, true));
					monitor.worked(1);
				}
			}
		}
		finally{
			monitor.done();
		}
		
		return linkStatuses;
	}
	
	private static final IStatus linkSdkDirectory(IProject p, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(Messages.ExternalResourceManager_LinkingSdkFilesToResources, 1);
		
		IStatus status = Status.OK_STATUS;
		
		recreateExternalsFolder(p.getName(), monitor);
		IFolder externalsFolder = p.getFolder(SpecialFolderNames.VIRTUAL_MOUNT_ROOT_DIR_NAME);
		
		try{
			File libDefFile = getLibraryDefinitionsPath(p);
			if (libDefFile != null){
				status = linkDirectory(p, externalsFolder, libDefFile, monitor, false);
			}
		}
		finally{
			monitor.done();
		}
		
		return status;
	}
	
	private static File getLibraryDefinitionsPath(IProject p) {
		XdsProjectSettings projectSettings = XdsProjectSettingsManager.getXdsProjectSettings(p);
		Sdk sdk = projectSettings.getProjectSdk();
		if (sdk != null && sdk.getLibraryDefinitionsPath() != null) {
			File libDefFile = new File(sdk.getLibraryDefinitionsPath());
			if (libDefFile.isDirectory()) {
				return libDefFile;
			}
		}
		return null;
	}
		
	private static final IStatus linkDirectory(IProject p, final IFolder externalsFolder, File directory, IProgressMonitor monitor, boolean filterOutSubfolders) throws CoreException {
		IWorkspace workspace = ResourceUtils.getWorkspace();
		if (workspace == null){
			return Status.OK_STATUS;
		}
		List<File> parts = ResourceUtils.getParts(directory);
		IFolder folder = externalsFolder;
		for (int i = 0; i < parts.size() - 1; i++) {
			File part = parts.get(i);
			folder = folder.getFolder(getName(part));
			if (!folder.exists()) {
				folder.create(IResource.FORCE | IResource.VIRTUAL, true, monitor);
				ResourceUtils.setAbsolutePathOfLinkedResourceAttribute(folder, part.getAbsolutePath());
				ResourceUtils.setSyntheticResourceAttribute(folder);
			}
		}
		
		IFolder linkedFolder = folder.getFolder(directory.getName());
		
		if (linkedFolder.exists() && (linkedFolder.isVirtual() || linkedFolder.getLocation() == null)) { 
			// linkedFolder can be only linked or virtual folder (by the precondition of this method).
			// It can be virtual folder (linkedFolder.getLocation() === null) if lookup directory was added and it was not linked before. 
			
			linkedFolder.delete(true, monitor);
		}
		if (!linkedFolder.exists()){
			Path path = new Path(directory.getAbsolutePath());
			IStatus validateLinkLocationStatus = workspace.validateLinkLocation(linkedFolder, path);
			if (!JavaUtils.areFlagsSet(validateLinkLocationStatus.getSeverity(), IStatus.ERROR | IStatus.CANCEL)){
				linkedFolder.createLink(path, IResource.NONE, monitor);
				if (filterOutSubfolders) {
					ResourceUtils.applyRegexFilter(linkedFolder, IResourceFilterDescription.EXCLUDE_ALL | IResourceFilterDescription.FOLDERS, ".*", monitor); //$NON-NLS-1$
				}
				ResourceUtils.setAbsolutePathOfLinkedResourceAttribute(linkedFolder, directory.getAbsolutePath());
				ResourceUtils.refreshLocalSync(folder);
			}
			
			return validateLinkLocationStatus;
		}
		
		return Status.OK_STATUS;
	}
	
	/**
	 * Deletes unused linked folders.<br>
	 * TODO : 2016: this method should devise list of directories from the passed IProject.
	 * TODO : 2016: use progressMonitor
	 * @param p
	 * @param lookupDirectories
	 * @throws CoreException 
	 */
	public static void cleanUnusedLinkedDirectories(IProject p, Collection<File> lookupDirectories ) throws CoreException {
		File libDefFile = getLibraryDefinitionsPath(p);
		Iterable<File> libDefFileIter = libDefFile != null? Arrays.asList(libDefFile) : Collections.emptyList();
		
		Set<File> expectedLinkedFolders = Sets.newHashSet(Iterables.concat(lookupDirectories, libDefFileIter));
		
		IFolder externalsFolder = p.getFolder(SpecialFolderNames.VIRTUAL_MOUNT_ROOT_DIR_NAME);
		doCheckLinks(externalsFolder, expectedLinkedFolders);
	}
	
	private static boolean doCheckLinks(IFolder f, Collection<File> expectedFolders) throws CoreException {
		boolean checkResult = false;
		if (f.isLinked() && !f.isVirtual()) {
			checkResult = expectedFolders.contains(ResourceUtils.getAbsoluteFile(f));
		}
		else {
			for (IResource child : f.members()) {
	            if (child instanceof IFolder) {
	                if (doCheckLinks((IFolder)child, expectedFolders)) {
	                	checkResult = true;
	                }
	            }
	        }
		}
		
		if (!checkResult) {
			f.delete(true, new NullProgressMonitor());
		}
		
		return checkResult;
    }

	private static String getName(File f){
		String name = f.getName();
		if (name.isEmpty()) { // file system root 
			name = f.getAbsolutePath().replaceAll("[:\\\\/]", StringUtils.EMPTY);  //$NON-NLS-1$
			if (name.isEmpty()) {
				return "ROOT"; //$NON-NLS-1$
			}
			else {
				return name;
			}
		}
		else {
			return name;
		}
	}

	/**
	 * This method changes resources; these changes will be reported
     * in a subsequent resource change event.
	 * @param progressMonitor 
     * 
	 */
	private static void recreateExternalsFolder(String projectName, IProgressMonitor monitor) {
		recreateExternalsFolder(getProject(projectName), monitor);
	}
	
	/**
     * This method changes resources; these changes will be reported
     * in a subsequent resource change event.
     * 
     */
	public static void recreateExternalsFolder(IProject project, IProgressMonitor monitor) {
        final IFolder externalsFolder = project.getFolder(SpecialFolderNames.VIRTUAL_MOUNT_ROOT_DIR_NAME);
        try {
            if (!externalsFolder.exists()) {
            	externalsFolder.create(IResource.FORCE | IResource.VIRTUAL, true, monitor);
            }
        } catch (CoreException e) {
            LogHelper.logError(e);
        }
    }
	
	private static IProject getProject(String projectName) {
		return ResourceUtils.getProject(projectName);
	}
}