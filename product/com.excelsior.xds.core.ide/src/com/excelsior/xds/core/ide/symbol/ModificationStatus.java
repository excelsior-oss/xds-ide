package com.excelsior.xds.core.ide.symbol;

import org.eclipse.core.resources.IProject;

import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;

public final class ModificationStatus {
	private final ModificationType modificationType;
	private final IProject project;
	private final ParsedModuleKey key;

	public ModificationStatus(ModificationType modificationType,
			IProject project, ParsedModuleKey key) {
		this.modificationType = modificationType;
		this.project = project;
		this.key = key;
	}
	
	public ParsedModuleKey getKey() {
		return key;
	}

	public ModificationType getModificationType() {
		return modificationType;
	}
	
	public IProject getProject() {
		return project;
	}
}
