package com.excelsior.xds.core.ide.utils;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.google.common.collect.Iterables;

public final class ParsedModuleKeyUtils {
	public static ParsedModuleKey create(IFile ifile) {
		IProject project = ifile != null? ifile.getProject() : null;
		return new ParsedModuleKey(project, ResourceUtils.toFileStore(ifile));
	}
	
	public static ParsedModuleKey create(final IProject project, IFileStore moduleFile) {
		return new ParsedModuleKey(project, moduleFile);
	}
	
	public static Iterable<ParsedModuleKey> transform(final IProject project, Iterable<IFileStore> files) {
		return Iterables.transform(files, f -> new ParsedModuleKey(project, f));
	}
	
	public static Iterable<ParsedModuleKey> transform(final BuildSettings buildSettings, Iterable<IFileStore> files) {
		return Iterables.transform(files, f -> new ParsedModuleKey(buildSettings, f));
	}
	
	public static Iterable<ParsedModuleKey> create(Iterable<IFile> ifiles) {
		return Iterables.transform(ifiles, iFile -> create(iFile));
	}
}
