package com.excelsior.xds.ui.launcher;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.model.XdsProjectConfiguration;
import com.excelsior.xds.core.project.launcher.ILaunchConfigConst;
import com.excelsior.xds.core.resource.EncodingUtils;
import com.excelsior.xds.core.text.TextEncoding;
import com.excelsior.xds.core.utils.launch.LaunchConfigurationUtils;
import com.excelsior.xds.ui.internal.nls.Messages;

/**
 * Common behavior for Modula-2 launch shortcuts
 */
public class LaunchShortcut extends AbstractLaunchShortcut {
	private static final String LAUNCH_CONFIG_TYPE_ID = ILaunchConfigConst.ID_MODULA_APPLICATION;

	/**
	 * Resolves a type that can be launched from the given scope and launches in the
	 * specified mode.
	 * 
	 * @param scope the XDS elements to consider for a type that can be launched
	 * @param mode launch mode
	 * @param emptyMessage error message when no types are resolved for launching
	 */
	private void searchAndLaunch(Object[] scope, String mode, String emptyMessage) {
		IProject xdsProject = selectXdsProject(scope, emptyMessage);
		if (xdsProject != null) {
			ILaunchConfigurationType launchConfigType = getConfigurationType(LAUNCH_CONFIG_TYPE_ID);
            List<ILaunchConfiguration> configs = getLaunchConfigurations(xdsProject, launchConfigType).collect(Collectors.toList());
            ILaunchConfiguration config = null;
            if (!configs.isEmpty()) {
            	config = selectConfiguration(configs);
            	if (config == null) {
            		return;
            	}
            }
            if (config == null) {
            	config = createConfiguration(xdsProject);
            }
            if (config != null) {
                DebugUITools.launch(config, mode);
            }
		}
	}
	
	private ILaunchConfiguration createConfiguration(IProject iProject) {
		ILaunchConfiguration config = null;
		try {
		    XdsProjectConfiguration xdsProjectSettings = new XdsProjectConfiguration(iProject);
			
			ILaunchConfigurationType configType = getConfigurationType(LAUNCH_CONFIG_TYPE_ID);		
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, 
					DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(iProject.getName())); 
			wc.setAttribute(ILaunchConfigConst.ATTR_PROJECT_NAME, iProject.getName());
			wc.setAttribute(ILaunchConfigConst.ATTR_EXECUTABLE_PATH, xdsProjectSettings.getExePath());
            wc.setAttribute(ILaunchConfigConst.ATTR_PROGRAM_ARGUMENTS, StringUtils.EMPTY);
            wc.setAttribute(ILaunchConfigConst.ATTR_DEBUGGER_ARGUMENTS, StringUtils.EMPTY);
            wc.setAttribute(ILaunchConfigConst.ATTR_SIMULATOR_ARGUMENTS, StringUtils.EMPTY);
			if (TextEncoding.isCodepageSupported(EncodingUtils.DOS_ENCODING)) {
    			wc.setAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, EncodingUtils.DOS_ENCODING);
			} 
	        
			wc.setMappedResources(new IResource[] {iProject});
			config = wc.doSave();		
		} catch (CoreException e) {
			MessageDialog.openError(getShell(), Messages.Common_Error, Messages.LaunchShortcut_CantCreateLaunchCfg + ": " + e); //$NON-NLS-1$
		}
		return config;
	}	

    private Stream<ILaunchConfiguration> getLaunchConfigurations(IProject xdsProject, ILaunchConfigurationType configType) {
    	try {
    		ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(configType);
    		return Arrays.stream(configs).filter(LaunchConfigurationUtils.projectPredicate(xdsProject));
    	} catch (CoreException e) {
    		MessageDialog.openError(getShell(), Messages.Common_Error, e.toString());
    	}
        return Stream.empty();
    }


	@Override
	public void launch(ISelection selection, String mode) {
		// Called from Eclipse when there is no last launch configuration for current XDS selection.
		if (selection instanceof IStructuredSelection) {
			searchAndLaunch(((IStructuredSelection)selection).toArray(), mode, Messages.LaunchShortcut_NoLaunchableInSelection);
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		// Called from Eclipse when there is no last launch configuration for *.mod, *.ob2 or *.def editor file.
		IFile ifile= CoreEditorUtils.editorInputToIFile(editor.getEditorInput());
		if (ifile != null) {
			searchAndLaunch(new Object[] {ifile}, mode, Messages.LaunchShortcut_NoLaunchableContentInEditor);
		}
	}
}
