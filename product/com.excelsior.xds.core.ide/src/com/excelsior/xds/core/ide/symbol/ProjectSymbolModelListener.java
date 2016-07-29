package com.excelsior.xds.core.ide.symbol;

import org.eclipse.core.resources.IProject;

import com.excelsior.xds.core.project.IXdsProjectSettingsListener;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;

/**
 * {@link ISymbolModelListener} which listens to the project settings changes as {@link IXdsProjectSettingsListener}
 * 
 * @author lsa80
 */
public abstract class ProjectSymbolModelListener extends SymbolModelListenerAdapter implements IXdsProjectSettingsListener {
	private final IProject project;
	private boolean isReportOnExistingModule;
	private boolean isNeedModulaAst;

	public ProjectSymbolModelListener(IProject project, ParsedModuleKey targetModuleKey, boolean isReportOnExistingModule, boolean isNeedModulaAst) {
		super(targetModuleKey);
		this.project = project;
		this.isReportOnExistingModule = isReportOnExistingModule;
		this.isNeedModulaAst = isNeedModulaAst;
	}
	
	public void install() {
		if (project != null) {
			XdsProjectSettingsManager.addListener(project, this);
		}
		SymbolModelManager.instance().addListener(this, isReportOnExistingModule, isNeedModulaAst);
	}
	
	public void uninstall() {
		SymbolModelManager.instance().removeListener(this);
		if (project != null) {
			XdsProjectSettingsManager.removeListener(project, this);
		}
	}
}
