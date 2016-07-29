package com.excelsior.xds.core.marker;

import java.util.Arrays;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.excelsior.xds.core.internal.nls.Messages;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.resource.ResourceUtils;

/**
 * Utilities that work with markers in the XDS IDE.
 * @see {@link IMarker} 
 * @author lsa80
 */
public final class MarkerUtils {
	private MarkerUtils(){
		super();
	}
	/**
	 *  Deletes xds build markers on given resource, and, 
	 * optionally, deletes such markers from its children.  If <code>includeSubtypes</code>
	 * is <code>false</code>, only markers whose type exactly matches 
	 * the given type are deleted.  
	 * <p/>
	 * @param resource where to delete markers
	 * @param includeSubtypes whether or not to consider sub-types of the given type
	 * @param depth how far to recurse (see <code>IResource.DEPTH_* </code>)
	 * @throws CoreException
	 */
	public static void deleteBuildProblemMarkers(IResource resource, boolean isIncludeSubtypes, int depth) throws CoreException {
		resource.deleteMarkers(XdsMarkerConstants.BUILD_PROBLEM_MARKER_TYPE, isIncludeSubtypes, depth);
	}
	
	public static IMarker[] findBuildProblemMarkers(IResource r) {
		IMarker[] buildProblemMarkers = null;
		try {
			buildProblemMarkers = r.findMarkers(XdsMarkerConstants.BUILD_PROBLEM_MARKER_TYPE, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
		return buildProblemMarkers;
	}
	
	public static Integer getCharStart(IMarker marker) {
		Integer charStart = marker.getAttribute(IMarker.CHAR_START, -1);
		if (charStart == -1) {
			charStart = null;
		}
		return charStart;
	}
	
	public static void deleteParserProblemMarkers(IResource resource, int depth) throws CoreException {
		resource.deleteMarkers(XdsMarkerConstants.PARSER_PROBLEM, false, depth);
	}
	
	public static void scheduleDeleteMarkers(IProject p){
		WorkspaceJob deleteMarkersJob = new WorkspaceJob(Messages.BuilderUtils_DeleteMarkers) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				MarkerUtils.deleteBuildProblemMarkers(p, true, IResource.DEPTH_INFINITE);
				return Status.OK_STATUS;
			}
		};
		//TODO : is this a correct scheduling rule? Perhaps we should include every individual IFile from project...
		deleteMarkersJob.setRule(ResourceUtils.createRule(Arrays.asList(p)));
		deleteMarkersJob.schedule();
	}
}
