package com.excelsior.xds.core.marker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.resource.ResourceUtils;

public final class MarkerManager {
	private MarkerManager(){
		super();
	}
	/**
	 * Creates a bunch of markers in the workspace as the single atomic operation - thus resulting in a single delta
	 * @param fileToMarkerInfo
	 * @param progressMonitor
	 */
	public static void commitParserMarkers(final Map<IFileStore, FileMarkerInfo> fileToMarkerInfo, IProgressMonitor progressMonitor) {
		int i = 0;
		
		final IFileStore[] files = new IFileStore[fileToMarkerInfo.size()];
		IFile[] ifiles = new IFile[fileToMarkerInfo.size()];
		
		for (Entry<IFileStore, FileMarkerInfo> keyValue : fileToMarkerInfo.entrySet()) {
			files[i] = keyValue.getKey();
			ifiles[i] = keyValue.getValue().getIFile();
			i++;
		}
		
		ISchedulingRule rule = ResourceUtils.createRule(Arrays.asList(ifiles));
		
		WorkspaceJob job = new WorkspaceJob("Update markers job") { //$NON-NLS-1$
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor){
				for (int j = 0; j < files.length; j++) {
					FileMarkerInfo fileMarkerInfo = fileToMarkerInfo.get(files[j]);
					if (fileMarkerInfo != null) {
						IFile iFile = fileMarkerInfo.getIFile();
						
						try {
							if (iFile.exists()) {
								Map<Integer, IMarker> offset2BuildMarker = createOffset2BuildMarkerMap(iFile);
								
								MarkerUtils.deleteParserProblemMarkers(iFile, IResource.DEPTH_INFINITE);
								Set<MarkerInfo> parserMarkers = fileMarkerInfo.getParserMarkers();
								for (MarkerInfo parserMarker : parserMarkers) {
									if (offset2BuildMarker.containsKey(parserMarker.getCharStart())) {
										continue;
									}
									IMarker marker = iFile.createMarker(parserMarker.getType());
									marker.setAttributes(parserMarker.getAttributes());
								}
							}
						} catch (CoreException e) {
							LogHelper.logError(e);
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(rule);
		job.schedule();
	}
	
	/**
	 * @param iFile - iFile to be processed
	 * @return hashmap (Marker Start Offset --> IMarker) of the build markers from the input {@link IFile}
	 */
	private static Map<Integer, IMarker> createOffset2BuildMarkerMap(IFile iFile) {
		IMarker[] buildProblemMarkers = MarkerUtils.findBuildProblemMarkers(iFile);
		Map<Integer, IMarker> offset2BuildMarker = new HashMap<Integer, IMarker>(buildProblemMarkers.length);
		for (IMarker m : buildProblemMarkers) {
			Integer charStart = MarkerUtils.getCharStart(m);
			if (charStart != null) {
				offset2BuildMarker.put(charStart, m);
			}
		}
		return offset2BuildMarker;
	}
}
