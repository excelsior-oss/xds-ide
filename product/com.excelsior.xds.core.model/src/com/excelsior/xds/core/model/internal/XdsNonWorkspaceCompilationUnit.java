package com.excelsior.xds.core.model.internal;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.model.CompilationUnitType;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsNonWorkspaceCompilationUnit;
import com.excelsior.xds.core.model.IXdsProject;

public class XdsNonWorkspaceCompilationUnit extends XdsCompilationUnit
		implements IXdsNonWorkspaceCompilationUnit {

	private final IFileStore absoluteFile;
	private final String name;

	public XdsNonWorkspaceCompilationUnit(IFileStore absoluteFile, IXdsProject xdsProject, IXdsContainer parent) {
		super(xdsProject, parent);
		this.absoluteFile = absoluteFile;
		this.name = absoluteFile.getName();
	}

	@Override
	public CompilationUnitType getCompilationUnitType() {
		return determineCompilationUnitType(getAbsoluteFile().fetchInfo().getName());
	}

	@Override
	public boolean isInCompilationSet() {
		return false;
	}

	@Override
	public String getElementName() {
		return name;
	}

	@Override
	public IFileStore getAbsoluteFile() {
		return absoluteFile;
	}

}
