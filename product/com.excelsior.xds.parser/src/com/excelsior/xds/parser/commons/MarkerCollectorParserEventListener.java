package com.excelsior.xds.parser.commons;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.excelsior.xds.core.compiler.compset.CompilationSetManager;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.marker.FileMarkerInfo;
import com.excelsior.xds.core.marker.MarkerInfo;
import com.excelsior.xds.core.marker.XdsMarkerConstants;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.core.todotask.TodoTask;

/**
 * Accumulates information during the parsing phase related to markers that are to be created.
 * @author lsa80
 */
public class MarkerCollectorParserEventListener implements IParserEventListener {
	private final IProject iproject;
	private final Map<IFileStore, FileMarkerInfo> fileToMarkerInfo = new ConcurrentHashMap<IFileStore, FileMarkerInfo>();
	
	public MarkerCollectorParserEventListener(IProject iproject) {
		this.iproject = iproject;
	}
	
	public Map<IFileStore, FileMarkerInfo> getFileToMarkerInfo() {
		return fileToMarkerInfo;
	}

	@Override
	public void taskTag(IFileStore file, TextPosition position, int endOffset,
			TodoTask task, String message) {
	}

	@Override
	public void endFileParsing(IFileStore file) {
		getFileMarkerInfo(file); // make sure FileMarkerInfo structure is initialized,
		// it is nesecarry to represent situation for the file with no errors
	}

	@Override
	public void warning(IFileStore file, CharSequence chars, TextPosition position,
			int length, String message, Object... arguments) {
		addMarkerInfo(false, file, chars, position, length, MessageFormat.format(message, arguments));
	}

	private void addMarkerInfo( boolean isError, IFileStore file, CharSequence chars
			                  , TextPosition position, int length, String message )
	{
		FileMarkerInfo fileMarkerInfo = getFileMarkerInfo(file);
		if (fileMarkerInfo != null) {
			MarkerInfo markerInfo = createMarkerInfo(fileMarkerInfo.getIFile(), isError, chars, position, length, message);
			fileMarkerInfo.addParserMarker(markerInfo);
		}
	}

	@Override
	public void error(IFileStore file, CharSequence chars, TextPosition position,
			int length, String message, Object... arguments) {
		addMarkerInfo(true, file, chars, position, length, MessageFormat.format(message, arguments));
	}

	@Override
	public void logInternalError(IFileStore file, String message, Throwable exception) {
		LogHelper.logError(message);
	}

	@Override
	public void logInternalError(IFileStore file, String message) {
		LogHelper.logError(message);
	}

	@Override
	public void logInternalError(IFileStore file, Throwable exception) {
		LogHelper.logError(exception);
	}
	
	private FileMarkerInfo getFileMarkerInfo(IFileStore file) {
		FileMarkerInfo fileMarkerInfo = fileToMarkerInfo.get(file);
		if (fileMarkerInfo == null) {
			IResource res;
			res = ResourceUtils.getResource(iproject, file.toURI());
			if (!(res instanceof IFile)) {
				return null;
			}
			fileMarkerInfo = new FileMarkerInfo((IFile)res);
			fileToMarkerInfo.put(file, fileMarkerInfo);
		}
		return fileMarkerInfo;
	}
	
	private MarkerInfo createMarkerInfo(IFile ifile, boolean isError, CharSequence chars, TextPosition position, int length, String message) {
		
		MarkerInfo markerInfo = new MarkerInfo(XdsMarkerConstants.PARSER_PROBLEM);
		markerInfo.setAttribute(IMarker.MESSAGE, message);
		
		// support for source code problem markers not showed in the Problem View or Project Explorer
		int markerSeverity = isError ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING;
		boolean isInCompilationSet = CompilationSetManager.getInstance().isInCompilationSet(ifile);
		markerInfo.setAttribute(IMarker.SEVERITY, isInCompilationSet ? markerSeverity : IMarker.SEVERITY_INFO);
		markerInfo.setAttribute(XdsMarkerConstants.PARSER_PROBLEM_SEVERITY_ATTRIBUTE, markerSeverity);
		
		if (position != null) {
			markerInfo.setAttribute(IMarker.LINE_NUMBER, position.getLine());
			int offset = position.getOffset();
			markerInfo.setAttribute(IMarker.CHAR_START, offset);
			markerInfo.setAttribute(IMarker.CHAR_END,   offset + length);
		}
		
		return markerInfo;
	}
}
