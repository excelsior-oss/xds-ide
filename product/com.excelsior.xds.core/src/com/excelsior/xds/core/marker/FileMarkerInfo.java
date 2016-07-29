package com.excelsior.xds.core.marker;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.resources.IFile;

import com.excelsior.xds.core.utils.collections.CollectionsUtils;

/**
 * Class contains descriptors of the markers related to the specific IFile.
 * Use by {@link com.excelsior.xds.core.marker.MarkerManager} to bulk-create the markers.
 * @author lsa80
 */
public class FileMarkerInfo {
	private final Set<MarkerInfo> parserMarkers = CollectionsUtils.newConcurentHashSet();
	
	private final IFile ifile;

	public FileMarkerInfo(IFile ifile) {
		this.ifile = ifile;
	}
	
	public IFile getIFile() {
		return ifile;
	}
	
	public Set<MarkerInfo> getParserMarkers() {
		return Collections.unmodifiableSet(parserMarkers);
	}
	
	/**
	 * Adds parser marker if its position is not occupied by the build marker
	 * @param markerInfo
	 */
	public void addParserMarker(MarkerInfo markerInfo) {
		// if not position of the parser marker starts at the same position where build marker is to be placed: 
		parserMarkers.add(markerInfo);
	}
}