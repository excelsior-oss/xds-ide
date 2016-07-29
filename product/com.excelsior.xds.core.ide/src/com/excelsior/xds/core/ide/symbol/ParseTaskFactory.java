package com.excelsior.xds.core.ide.symbol;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorInput;

import com.excelsior.xds.builder.buildsettings.BuildSettingsCache;
import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.builders.DefaultBuildSettingsHolder;
import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.resource.ResourceUtils;

public final class ParseTaskFactory {
	public ParseTaskFactory(){
	}
	
	public static ParseTask create(IEditorInput editorInput) {
		IFile ifile = CoreEditorUtils.editorInputToIFile(editorInput);
		if (ifile != null) {
			return create(ifile);
		}
		else {
			return create(CoreEditorUtils.editorInputToFileStore(editorInput));
		}
	}
	
	/**
	 * Create task to parse single workspace compilation unit with typical settings.
	 * @param ifile
	 * @return
	 */
	public static  ParseTask create(IFile ifile) {
		return createWorkspaceTask(Arrays.asList(ifile));
	}
	
	/**
	 * Create task to parse single NON-workspace compilation unit with typical settings.
	 * 
	 * @param file
	 * @return
	 */
	public static  ParseTask create(File file) {
		return createNonWorkspaceTask(Collections.singletonList(ResourceUtils.toFileStore(file)));
	}
	
	public static  ParseTask create(IFileStore fileStore) {
		return createNonWorkspaceTask(Collections.singletonList(fileStore));
	}
	
	/**
	 * Create task to parse workspace compilation units with typical settings.<br>
	 * Assumes that all IFile`s are from the same project.
	 * @param files - compilation modules to parse
	 * @return
	 */
	public static  ParseTask createWorkspaceTask(Collection<IFile> files) {
		return createWorkspaceTask(files, false, true, true, true, true);
	}
	
	/**
	 * Create task to parse NON-workspace compilation units with typical settings.
	 * @param files - compilation modules to parse
	 * @return
	 */
	public static  ParseTask createNonWorkspaceTask(Collection<IFileStore> files) {
		return createNonWorkspaceTask(files, false, true, true, true);
	}
	
	public static ParseTask createTask(IProject project, Collection<File> files) {
		if (project == null) {
			return createNonWorkspaceTask(ResourceUtils.convertFilesToFileStores(files));
		}
		else {
			BuildSettings buildSettings = BuildSettingsCache.createBuildSettings(project, files.iterator().next());
			return new ParseTask(project, ResourceUtils.convertFilesToFileStores(files), buildSettings, true, false, true, true, true);
		}
	}
	
	private static ParseTask createNonWorkspaceTask(Collection<IFileStore> files, boolean  isNeedModulaAst, boolean isParseDualModule, boolean isForce, boolean isParseImportSection) {
		if (CollectionUtils.isEmpty(files)) {
			return null;
		}
		return new ParseTask(null, files, DefaultBuildSettingsHolder.DefaultBuildSettings, false, isNeedModulaAst, isParseDualModule, isForce, isParseImportSection);
	}
	
	private static  ParseTask createWorkspaceTask(Collection<IFile> files, boolean  isNeedModulaAst, boolean isReportParseErrors, boolean isParseDualModule, boolean isForce, boolean isParseImportSection) {
		if (CollectionUtils.isEmpty(files)) {
			return null;
		}
		return new ParseTask(files, isReportParseErrors, isNeedModulaAst, isParseDualModule, isForce, isParseImportSection);
	}
}
