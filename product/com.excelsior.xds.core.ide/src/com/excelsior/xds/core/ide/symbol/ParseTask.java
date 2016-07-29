package com.excelsior.xds.core.ide.symbol;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import com.excelsior.xds.builder.buildsettings.BuildSettingsCache;
import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.resource.ResourceUtils;

/**
 * @author lsa80
 */
public class ParseTask implements IParseTask {
	private IProject project;
	private Collection<IFileStore> files;
	private BuildSettings buildSettings;
	private boolean isReportParseErrors = true;
	private boolean isNeedModulaAst = true;
	private boolean isParseDualModule = true;
	private boolean isForce = true;
	private boolean isParseImportSection = true;
	
	public ParseTask(File file) {
		files = Collections.singletonList(ResourceUtils.toFileStore(file));
	}
	
	public ParseTask(Collection<IFile> files,
			boolean isReportParseErrors,
			boolean isNeedModulaAst, boolean isParseDualModule,
			boolean isForce, boolean isParseImportSection) {
		IFile firstIFile = files.iterator().next();
		this.buildSettings = BuildSettingsCache.createBuildSettings(firstIFile);
		this.project = firstIFile.getProject();
		this.files = ResourceUtils.convertIFilesToFileStores(files);
		this.isReportParseErrors = isReportParseErrors;
		this.isNeedModulaAst = isNeedModulaAst;
		this.isParseDualModule = isParseDualModule;
		this.isForce = isForce;
		this.isParseImportSection = isParseImportSection;
	}
	
	public ParseTask(IProject project, Collection<IFileStore> files,
			BuildSettings buildSettings, boolean isReportParseErrors,
			boolean isNeedModulaAst, boolean isParseDualModule,
			boolean isForce, boolean isParseImportSection) {
		this.project = project;
		this.files = files;
		this.buildSettings = buildSettings;
		this.isReportParseErrors = isReportParseErrors;
		this.isNeedModulaAst = isNeedModulaAst;
		this.isParseDualModule = isParseDualModule;
		this.isForce = isForce;
		this.isParseImportSection = isParseImportSection;
	}

	public ParseTask(IParseTask parseTask) {
		this.project = parseTask.project();
		this.files = parseTask.files();
		this.buildSettings = parseTask.buildSettings();
		this.isReportParseErrors = parseTask.isReportParseErrors();
		this.isNeedModulaAst = parseTask.isNeedModulaAst();
		this.isParseDualModule = parseTask.isParseDualModule();
		this.isForce = parseTask.isForce();
		this.isParseImportSection = parseTask.isForce();
	}

	@Override
	public IProject project() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	@Override
	public Collection<IFileStore> files() {
		return files;
	}

	public void setFiles(Collection<IFileStore> files) {
		this.files = files;
	}

	@Override
	public BuildSettings buildSettings() {
		return buildSettings;
	}

	public void setBuildSettings(BuildSettings buildSettings) {
		this.buildSettings = buildSettings;
	}

	@Override
	public boolean isReportParseErrors() {
		return isReportParseErrors;
	}

	/**
	 * @param isReportParseErrors
	 */
	public void setReportParseErrors(boolean isReportParseErrors) {
		this.isReportParseErrors = isReportParseErrors;
	}

	/* (non-Javadoc)
	 * @see com.excelsior.xds.builder.symbol.IParseTask#isNeedModulaAst()
	 */
	@Override
	public boolean isNeedModulaAst() {
		return isNeedModulaAst;
	}

	public void setNeedModulaAst(boolean isNeedModulaAst) {
		this.isNeedModulaAst = isNeedModulaAst;
	}

	@Override
	public boolean isParseDualModule() {
		return isParseDualModule;
	}

	public void setParseDualModule(boolean isParseDualModule) {
		this.isParseDualModule = isParseDualModule;
	}

	@Override
	public boolean isForce() {
		return isForce;
	}

	public void setForce(boolean isForce) {
		this.isForce = isForce;
	}

	@Override
	public boolean isParseImportSection() {
		return isParseImportSection;
	}

	public void setParseImportSection(boolean isParseImportSection) {
		this.isParseImportSection = isParseImportSection;
	}

	@Override
	public String toString() {
		return String
				.format("ParseTask [project=%s, files=%s, buildSettings=%s, isReportParseErrors=%s, isNeedModulaAst=%s, isParseDualModule=%s, isForce=%s, isParseImportSection=%s]",
						project, files, buildSettings, isReportParseErrors,
						isNeedModulaAst, isParseDualModule, isForce,
						isParseImportSection);
	}
}
