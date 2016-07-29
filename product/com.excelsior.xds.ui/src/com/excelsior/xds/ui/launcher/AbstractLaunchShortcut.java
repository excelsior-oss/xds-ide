package com.excelsior.xds.ui.launcher;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.utils.AdapterUtilities;
import com.excelsior.xds.launching.commons.utils.LaunchUtils;
import com.excelsior.xds.ui.commons.utils.SelectionUtils;
import com.excelsior.xds.ui.commons.utils.SwtUtils;
import com.excelsior.xds.ui.dialogs.SimpleListSelectionDialog;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.internal.nls.Messages;

public abstract class AbstractLaunchShortcut implements ILaunchShortcut2{

	@Override
	public IResource getLaunchableResource(ISelection selection) {
		IAdaptable adaptable= SelectionUtils.getObjectFromStructuredSelection(selection, IAdaptable.class);
		return adaptAsXdsIProject(adaptable);
	}

	@Override
	public IResource getLaunchableResource(IEditorPart editorpart) {
		return adaptAsXdsIProject(editorpart.getEditorInput());
	}
	

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		// let the framework resolve configurations based on resource mapping
		return null;
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		// let the framework resolve configurations based on resource mapping
		return null;
	}
	
	/**
	 * This method is used to ensure that when Debug perspective should not appear (say when we are launching PKT as external process) - it will not appear.
	 * @param mode
	 * @param p
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String determineLaunchMode(String mode, IProject p, ILaunchConfiguration configuration) throws CoreException {
		if (ILaunchManager.RUN_MODE.equals(mode)) {
			return mode;
		}
    	return LaunchUtils.canLaunchInIdeDebugMode(p, configuration)? ILaunchManager.DEBUG_MODE : ILaunchManager.RUN_MODE;
    }

	protected ILaunchConfigurationType getConfigurationType(String launchConfigurationTypeId) {
		ILaunchManager lm= DebugPlugin.getDefault().getLaunchManager();
		return lm.getLaunchConfigurationType(launchConfigurationTypeId);		
	}
	
	/**
	 * Returns the XDS project containing the XDS element associated with the
	 * given adaptable, or <code>null</code>.
	 * 
	 * @param adaptable adaptable object
	 * @return IProject or <code>null</code>
	 */
	protected static IProject adaptAsXdsIProject(Object o) {
		IProject p = AdapterUtilities.getAdapter(o, IProject.class);
		if (p == null) {
			IFile ifile = AdapterUtilities.getAdapter(o, IFile.class);
			if (ifile != null){
				p = ifile.getProject();
			}
		}
		if (ProjectUtils.isXdsProject(p)) {
			return p;
		}
		return null;
	}
	
	protected static IProject selectXdsProject(Object[] scope, String emptyMessage) {
		IProject xdsProject = null;
		List<IProject> projects = new ArrayList<IProject>(); 
		for (Object o : scope) {
			IProject p = adaptAsXdsIProject(o);
			if (p != null){
				projects.add(p);
			}
		}
		if (projects.isEmpty()) {
			MessageDialog.openError(getShell(), Messages.LaunchShortcut_LaunchError, emptyMessage); 
		} 
		else if (projects.size() > 1) {
			// There was more than one XDS project selected and no XDS run configurations for them
			// Ask what project to run (and create configuration for it):
			int selIdx = SimpleListSelectionDialog.Selection(getShell(), 
					Messages.LaunchShortcut_SelectXdsProject,
                                        Messages.LaunchShortcut_SelectXdsProjectToLaunch+':',
                                        ImageUtils.getImage(ImageUtils.M2_PRJ_FOLDER_TRANSP),
					new SimpleListSelectionDialog.ITextProvider() {
						@Override
						public String getText(Object o) {
							if (o instanceof IProject) {
								IProject p = (IProject) o;
								return p.getName();
							}
							else {
								return StringUtils.EMPTY;
							}
						}
					},
					projects.toArray(), 
					null);
			if (selIdx >= 0) {
				xdsProject = projects.get(selIdx);
			}
		} 
		else {
			xdsProject = projects.get(0);
		}
		return xdsProject;
	}
	
	/**
	 * Show Select Configuration dialog if there are several possible launch configurations
	 * @param configs
	 * @return null if user canceled dialog
	 */
	protected ILaunchConfiguration selectConfiguration(List<ILaunchConfiguration> configs) {
		ILaunchConfiguration c = null;
		if (configs.size() == 1) {
            c = configs.get(0);
    	} else if (configs.size() > 1) {
    		// Ask configuration to launch:
    		int selIdx = SimpleListSelectionDialog.Selection(getShell(), 
    				Messages.LaunchShortcut_SelectLaunchCongiguration,
                                Messages.LaunchShortcut_SelectLaunchCongigurationToLaunch+':',
    				ImageUtils.getImage(ImageUtils.XDS_GREEN_IMAGE_16X16),
    				new SimpleListSelectionDialog.ITextProvider() {
    					@Override
    					public String getText(Object o) {
    						if (o instanceof ILaunchConfiguration) {
								ILaunchConfiguration lc = (ILaunchConfiguration) o;
								return lc.getName();
							}
    						else {
    							return StringUtils.EMPTY;
    						}
    					}
    				},
    				configs.toArray(), 
    				null);
            if (selIdx >= 0) {
                c = configs.get(selIdx);
            }
        }
		return c;
	}
	
	protected static Shell getShell() {
		return SwtUtils.getDefaultShell();
	}
}
