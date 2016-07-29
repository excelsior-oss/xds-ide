package com.excelsior.xds.core.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.osgi.service.prefs.BackingStoreException;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.preferences.PreferenceKey;

/**
 * Base class for all IProject-based settings
 * 
 * @author lsa80
 */
public abstract class AbstractProjectSettings {
	protected final IProject project;
	private final String quailifier;
	protected final IScopeContext projectScope;
    
    protected AbstractProjectSettings(IProject project, String quailifier) {
        this.project = project;
        this.quailifier = quailifier;
        this.projectScope = new ProjectScope(project);
    }
    

	public void addChangeListener(IPreferenceChangeListener listener) {
		PreferenceKey.addChangeListener(projectScope, null, quailifier, listener);
	}
	
	public void removeChangeListener(IPreferenceChangeListener listener) {
		PreferenceKey.removeChangeListener(projectScope, null, quailifier, listener);
	}
	
	public void flush() {
        try {
            IEclipsePreferences node = projectScope.getNode(quailifier);
            node.flush();
            ProjectUtils.refreshLocalSync(project);
        } catch (BackingStoreException e) {
            LogHelper.logError(e);
        }
    }
}