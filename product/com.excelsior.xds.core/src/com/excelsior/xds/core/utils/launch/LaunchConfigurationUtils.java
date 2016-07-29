package com.excelsior.xds.core.utils.launch;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.project.launcher.ILaunchConfigConst;
import com.excelsior.xds.core.utils.IClosure;
import com.google.common.base.Objects;

/**
 * Helper methods to work with launch configurations.
 * @author lsa80
 */
public final class LaunchConfigurationUtils {
	private LaunchConfigurationUtils() {
	}

	public static void modifyLaunchConfiguration(ILaunchConfiguration config,
			IClosure<ILaunchConfigurationWorkingCopy> configWorkingCopyAccessor) throws CoreException {
		ILaunchConfigurationWorkingCopy workingCopy = config.getWorkingCopy();
		configWorkingCopyAccessor.execute(workingCopy);
		workingCopy.doSave();
	}
	
	/**
	 * Project referred by this launch
	 * @param config
	 * @return
	 * @throws CoreException
	 */
	public static IProject getProject(ILaunchConfiguration config) throws CoreException {
		if (config == null) {
			return null;
		}
		String projectName = getProjectName(config);
		if (projectName == null){
			return null;
		}
		return ProjectUtils.getProject(projectName);
	}

	/**
	 * Gets the name of the corresponding (XDS IDE) {@link IProject} from the {@link ILaunchConfiguration}
	 * @param config
	 * @return
	 * @throws CoreException
	 */
	public static String getProjectName(ILaunchConfiguration config)
			throws CoreException {
		return config.getAttribute(ILaunchConfigConst.ATTR_PROJECT_NAME, (String)null);
	}
	
	/**
	 * Get all XDS launches operating on the current project.<br>
	 * {@link #getProject(ILaunchConfiguration)} method is used to determine whether launch it is the XDS launch.<br>
	 * @param project current project
	 * @throws CoreException
	 */
	public static Stream<ILaunch> getLaunches(IProject project) throws CoreException {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		return Arrays.stream(launches).filter(l -> projectPredicate(project).test(l.getLaunchConfiguration()));
	}
	
	public static long countLaunches(IProject project, Predicate<ILaunch> predicate) throws CoreException {
		return getLaunches(project).filter(predicate).count();
	}
	
	public static Predicate<ILaunchConfiguration> projectPredicate(IProject p) {
		return lc -> {
			try {
				return ObjectUtils.equals(p, getProject(lc));
			} catch (CoreException e) {
				LogHelper.logError(e);
				return false;
			}
		};
	}
	
	public static Predicate<ILaunch> debugOrProfileLaunchPredicate() {
		return debugLaunchPredicate().or(profileLaunchPredicate());
	}
	
	public static Predicate<ILaunch> debugLaunchPredicate() {
		return launch -> ILaunchManager.DEBUG_MODE.equals(launch.getLaunchMode());
	}
	
	public static Predicate<ILaunch> profileLaunchPredicate() {
		return launch -> ILaunchManager.PROFILE_MODE.equals(launch.getLaunchMode());
	}
	
	/**
	 * <br> Finds Modula launch associated with the given project, if any (there should be only one)
	 * @param project project to query
	 * @return
	 */
	public static ILaunch getLaunch(IProject project) {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches = launchManager.getLaunches();
		for (ILaunch launch : launches) {
			try {
				IProject launchProject = getProject(launch.getLaunchConfiguration());
				if (launchProject == null){
					continue;
				}
				if (Objects.equal(launchProject, project)) {
					return launch;
				}
			} catch (CoreException e) {
			}
		}
		return null;
	}
}