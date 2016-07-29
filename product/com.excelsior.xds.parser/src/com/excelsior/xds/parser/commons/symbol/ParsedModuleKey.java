package com.excelsior.xds.parser.commons.symbol;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.builders.BuildSettingsKey;
import com.excelsior.xds.core.builders.BuildSettingsKeyFactory;
import com.excelsior.xds.core.builders.DefaultBuildSettingsHolder;
import com.excelsior.xds.core.sdk.SdkUtils;

/**
 * Context under which {@link #moduleFile} was parsed.
 * Also used as the utility class to store AST or ModuleSymbol in the hash table.<br><br> 
 * 
 * ModuleSymbols with: <br><br> 
 * 
 *  same path and created with the same {@link buildSettingsKey} are considered <b>equal.</b><br>
 * <br>
 * if {@link project} is null, this means non-workspace module. <br><br>
 * 
 * @author lsa80
 */
public final class ParsedModuleKey {
	private final BuildSettingsKey buildSettingsKey;
	public final IFileStore moduleFile;
	
	public ParsedModuleKey() {
		this(null);
	}
	
	public ParsedModuleKey(IFileStore moduleFile) {
		this(DefaultBuildSettingsHolder.DefaultBuildSettingsKey, moduleFile);
	}
	
	public ParsedModuleKey(BuildSettings buildSettings, IFileStore moduleFile) {
		this(createBuildSettingsKey(buildSettings, moduleFile), moduleFile);
	}
	
	public ParsedModuleKey(IProject project, IFileStore moduleFile) {
		this(BuildSettingsKeyFactory.createBuildSettingsKey(project, moduleFile), moduleFile);
	}
	
	private static BuildSettingsKey createBuildSettingsKey(BuildSettings buildSettings, IFileStore moduleFile) {
		if (SdkUtils.isInsideSdkLibraryDefinitions(buildSettings.getSdk(), moduleFile)) {
			return DefaultBuildSettingsHolder.DefaultBuildSettingsKey;
		}
		else {
			return buildSettings.createKey();
		}
	}
	
	private ParsedModuleKey(BuildSettingsKey buildSettingsKey, IFileStore moduleFile) {
		this.buildSettingsKey = buildSettingsKey != null? buildSettingsKey : DefaultBuildSettingsHolder.DefaultBuildSettingsKey;
		this.moduleFile = moduleFile;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((buildSettingsKey == null) ? 0 : buildSettingsKey.hashCode());
		result = prime * result
				+ ((moduleFile == null) ? 0 : moduleFile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParsedModuleKey other = (ParsedModuleKey) obj;
		if (buildSettingsKey == null) {
			if (other.buildSettingsKey != null)
				return false;
		} else if (!buildSettingsKey.equals(other.buildSettingsKey))
			return false;
		if (moduleFile == null) {
			if (other.moduleFile != null)
				return false;
		} else if (!moduleFile.equals(other.moduleFile))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String
				.format("ParsedModuleKey [buildSettingsKey=%s, moduleFile=%s]", buildSettingsKey, moduleFile);
	}
}
