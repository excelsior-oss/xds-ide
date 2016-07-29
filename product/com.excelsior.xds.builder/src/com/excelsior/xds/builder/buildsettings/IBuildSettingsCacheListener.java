package com.excelsior.xds.builder.buildsettings;

import org.eclipse.core.resources.IProject;

public interface IBuildSettingsCacheListener {
	void buildSettingsReload(IProject p);
}
