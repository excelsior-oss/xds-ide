package com.excelsior.xds.launching.commons.delegate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import com.excelsior.xds.builder.listener.IBuilderListener;
import com.excelsior.xds.core.builders.XdsBuildResult;
import com.excelsior.xds.core.console.ColorStreamType;
import com.excelsior.xds.core.console.IXdsConsole;
import com.excelsior.xds.core.console.IXdsConsoleFactory;
import com.excelsior.xds.core.console.IXdsConsoleTerminateCallback;
import com.excelsior.xds.core.exceptions.ExceptionHelper;
import com.excelsior.xds.core.ide.ui.consts.IUiConstants;
import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.project.launcher.ILaunchConfigConst;
import com.excelsior.xds.core.resource.EncodingUtils;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.text.TextUtils;
import com.excelsior.xds.core.utils.BuilderUtils;
import com.excelsior.xds.core.variables.VariableUtils;
import com.excelsior.xds.launching.commons.internal.delegate.BuildListener;
import com.excelsior.xds.launching.commons.internal.nls.Messages;
import com.excelsior.xds.launching.commons.internal.plugin.LaunchingCommonsPlugin;
import com.excelsior.xds.launching.commons.services.ServiceHolder;
import com.excelsior.xds.ui.commons.perspectives.PerspectiveUtils;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.commons.utils.SwtUtils;

@SuppressWarnings("restriction")
public abstract class AbstractLaunchDelegate implements ILaunchConfigurationDelegate2 {
    private AtomicBoolean isLaunchOkToProceed = new AtomicBoolean(false);
    
    @Override
    public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
    	isLaunchOkToProceed.set(saveEditors());
        return isLaunchOkToProceed.get();
    }
    
	private boolean saveEditors() {
    	final String saveBeforeLaunch = DebugUITools
				.getPreferenceStore()
				.getString(
						org.eclipse.debug.internal.ui.IInternalDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH);
        if (saveBeforeLaunch
				.equalsIgnoreCase(org.eclipse.jface.dialogs.MessageDialogWithToggle.NEVER)) {
        	return true;
		}
        
        return CoreEditorUtils.saveEditors(saveBeforeLaunch
				.equalsIgnoreCase(org.eclipse.jface.dialogs.MessageDialogWithToggle.PROMPT));
    }
    
    /*
     * Build before run - called inly when configured in preferencies
     */
    @Override
    public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
        if (!build(getProject(configuration))) {
            isLaunchOkToProceed.set(false);
        }
        return false;
    }

    /*
     * Returns false when need to cancel launching
     */
    @Override
    public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
        return isLaunchOkToProceed.get();
    }
    
    private boolean build(final IProject iProject) {
        final XdsBuildResult buildResult[] = {null}; 
        
        IBuilderListener bl = new IBuilderListener() {
            boolean wasStarted = false; // to don't listen for finish of possible build was runned before 
            
            @Override
            public void onBuildStarted(IProject p) {
                wasStarted = true;
            }

            @Override
            public void onBuildFinished(IProject p, XdsBuildResult buildRes) {
                if (iProject == p && wasStarted) {
                    synchronized(buildResult) {
                        buildResult[0] = buildRes;
                        buildResult.notifyAll();
                    }
                }
            }
        };
        
        // Do build:

        try {
            BuildListener.addListener(bl);
            BuilderUtils.invokeBuild(iProject, new NullProgressMonitor());
            
            synchronized(buildResult) {
                if (buildResult[0] == null) { // else - it is already finished
                    buildResult.wait();
                }
            }
        } catch (Exception e) {
            LogHelper.logError(e);
            return false;
        } finally {
            BuildListener.removeListener(bl);
        }

        return buildResult[0] == XdsBuildResult.SUCCESS;
    }
    
    /**
     * Prepare command line list. Gets command line in usual form like:
     * c:\dir\some.exe -arg1 "quo ted" $(varname) Before processing this string:
     * - All possible '\r' and '\n' characters will be replaced with spaces ' '
     * - All eclipse variables will be opened - All quotes will be opened
     * @param sdk 
     * 
     * @param cmdline
     * @return List with commandline items
     * @throws CoreException
     */
    protected List<String> prepareCommandline(Sdk sdk, String cmdline) throws CoreException {
        cmdline = cmdline.replace('\n', ' ').replace('\r', ' ');
        cmdline = VariableUtils.performStringSubstitution(sdk, cmdline);
        String arr[] = new StrTokenizer(cmdline, ' ', '"').getTokenArray();
        return new ArrayList<>(Arrays.asList(arr));
    }
    
    protected String[] prepareCommandlineAsArray(Sdk sdk, String cmdline) throws CoreException {
    	return prepareCommandline(sdk, cmdline).toArray(new String[0]);
    }
    
    /**
	 * Returns commandline with opened variables etc. 
	 * as it will be processed at launch time 
	 * @return
	 */
	public static String getReadableCommandline(Iterable<String> cmdlist) {
		StringBuilder sb = new StringBuilder();
		if (cmdlist != null) {
			for (String s : cmdlist) {
				if (sb.length() > 0) {
					sb.append(' ');
				}
				s.replace("\"", "\"\""); //$NON-NLS-1$ //$NON-NLS-2$
				if (s.indexOf(' ') >= 0) {
					sb.append('"').append(s).append('"');
				} else {
					sb.append(s);
				}
			}
		}
		return sb.toString();
	}
    
    protected static void appendArg(StringBuilder sb, String txt, String err) throws CoreException {
    	txt = StringUtils.trim(txt);
        if (txt == null || txt.isEmpty() || txt.equals(Sdk.NOT_SUPPORTED)) {
            if (err != null) {
                abortLaunch(Messages.AbstractLaunchDelegate_NotSpecified + ": " + err); //$NON-NLS-1$
            }
        } else {
            if (txt.indexOf(' ') > -1) {
                sb.append('"').append(txt).append("\" "); //$NON-NLS-1$
            } else {
                sb.append(txt);
            }
        }
    }
    
    protected static File getWorkingDirectory(ILaunchConfiguration configuration, IProject iProject) throws CoreException {
        File workDir = null;
        String path = configuration.getAttribute(ILaunchConfigConst.ATTR_WORKING_DIRECTORY, (String) null);
        if (path == null) {
            path = "${workspace_loc:" + iProject.getFullPath().makeRelative().toOSString() + "}";  //$NON-NLS-1$//$NON-NLS-2$
        }

        path = VariableUtils.performStringSubstitution(iProject, path);
        IPath iPath = new Path(path);
        
        if (iPath.isAbsolute()) {
            File dir = new File(iPath.toOSString());
            if (dir.isDirectory()) {
                workDir = dir;
            } else {
                // This may be a workspace relative path returned by a variable.
                // However variable paths start with a slash and thus are thought to
                // be absolute
                IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(iPath);
                if (res instanceof IContainer && res.exists()) {
                    IPath loc = res.getLocation();
                    if (loc == null){
                    	return null;
                    }
					workDir = loc.toFile();
                } else {
                    workDir = dir; // unexistent, will cause abort()
                }
            }
        } else {
            IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(iPath);
            if (res instanceof IContainer && res.exists()) {
                workDir = res.getLocation().toFile();
            } else {
                workDir = new File(path); // unexistent, will cause abort()
            }
        }
    
        if (workDir == null) {
            abortLaunch(Messages.AbstractLaunchDelegate_CantDetermineWorkDir);
            return null;
        } else if (!workDir.isDirectory()) {
            abortLaunch(Messages.AbstractLaunchDelegate_InvaludWorkDir + ": " + workDir.getAbsolutePath()); //$NON-NLS-1$
            return null;
        }
        return workDir;
    }


    protected static void abortLaunch(final String message) throws CoreException {
        throw new LaunchException(message);
    }
    
    protected static void openEditConfigurationPrompt(final ILaunchConfiguration configuration, final String message) {
    	 Display.getDefault().asyncExec(new Runnable() {
             @Override
             public void run() {
                 if (SWT.YES == SWTFactory.ShowMessageBox(
                         null,
                         Messages.AbstractLaunchDelegate_ProblemOccured,
                         String.format(Messages.AbstractLaunchDelegate_ProblemDescriptionAndAskToEdit,
                                       configuration.getName(), message),
                         SWT.YES | SWT.NO | SWT.ICON_QUESTION)) 
                 {
                     IStructuredSelection selection = new StructuredSelection(configuration);
                     DebugUITools.openLaunchConfigurationDialogOnGroup(SwtUtils.getDefaultShell(), selection, IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
                 }
            }
         });
    }
    
    @SuppressWarnings("serial")
	protected static class LaunchException extends CoreException {
    	private final boolean isEditConfiguration; // whether ask the user to edit the configuration to fix the problem
    	
    	public LaunchException(String message) {
    		this(message, true);
    	}

		public LaunchException(String message, boolean isEditConfiguration) {
			super(LogHelper.createErrorStatus(message));
			
			this.isEditConfiguration = isEditConfiguration;
		}

		public boolean isEditConfiguration() {
			return isEditConfiguration;
		}
    }

    protected static void abort(String message) throws CoreException {
    	ExceptionHelper.throwCoreException(LaunchingCommonsPlugin.PLUGIN_ID, message);
    }

    @Override
    public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
            throws CoreException {
        return null;
    }
    
    protected static File getAndCheckFileAttribute(Sdk sdk, ILaunchConfiguration configuration, String attrName, String messageIfEmpty, String messageIfIncorrect) throws CoreException {
    	String attrVal = performStringSubstitution(sdk, configuration, attrName);
	    if (attrVal.isEmpty()) {
			abortLaunch(messageIfEmpty);
		}
	    File file = new File(attrVal);
	    if (!file.isFile()) {
			abortLaunch(messageIfIncorrect + ": " + file.getAbsolutePath()); //$NON-NLS-1$
		}
	    return file;
    }

    protected static IProject getProject(ILaunchConfiguration config) throws CoreException {
    	  String projectName = config.getAttribute(ILaunchConfigConst.ATTR_PROJECT_NAME, StringUtils.EMPTY);
    	  return ResourceUtils.getProject(projectName);
    }
    
    protected static String performStringSubstitution(Sdk sdk, ILaunchConfiguration configuration, String attrName) throws CoreException {
    	return VariableUtils.performStringSubstitution(sdk, configuration.getAttribute(attrName, StringUtils.EMPTY));
    }
    
    protected static Sdk getProjectSdk(IProject iProject) throws CoreException {
		XdsProjectSettings xdsProjectSettings  =  XdsProjectSettingsManager.getXdsProjectSettings(iProject);
		return xdsProjectSettings.getProjectSdk();
	}
    
    protected static String enquoteIfHasSpace(String text) {
    	return TextUtils.enquoteIfHasSpace(text);
    }
    
    protected void runModulaApplication(String mode, ILaunchConfiguration configuration,
			ILaunch launch, final IProject iProject, Sdk sdk, IProgressMonitor monitor)
			throws CoreException {
    	try{
    		boolean isDebug = ILaunchManager.DEBUG_MODE.equals(mode);
    		boolean isProfile = ILaunchManager.PROFILE_MODE.equals(mode);
    		try{
    			// Executable
    			final String exeName = performStringSubstitution(sdk, configuration, ILaunchConfigConst.ATTR_EXECUTABLE_PATH); //$NON-NLS-1$
    			if (exeName.isEmpty()) {
    				abortLaunch(Messages.LaunchDelegate_ExeNameNotSet);
    			}

    			if (!new File(exeName).isFile()) {
    				abortLaunch(Messages.LaunchDelegate_InvalidExe + ": " + exeName); //$NON-NLS-1$
    			}

    			String cmdline = buildCommandLine(configuration, sdk, isDebug, isProfile, exeName);
    			
    			// Working dir
    			File workingDir = getWorkingDirectory(configuration, iProject);

    			// Environment variables (may be null to use default)
    			final String[] envp = DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);
    			// Run console mode:
    			boolean standalone = isDebug || isProfile || !configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
    			
    			String consoleEncoding = configuration.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, EncodingUtils.DOS_ENCODING);
    			if (Sdk.isSet(sdk.getSimulatorExecutablePath())) {
    				consoleEncoding = EncodingUtils.DOS_ENCODING;
    			}
    			
    			launch.setAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, consoleEncoding);
    			launch.setAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY, workingDir.getAbsolutePath());
    			launch.setAttribute(DebugPlugin.ATTR_PATH, exeName);
    			launch.setAttribute(DebugPlugin.ATTR_ENVIRONMENT, StringUtils.join(envp, ','));
    			
    			if (monitor.isCanceled()) {
    				return;
    			}
    			monitor.worked(1);
    			monitor.subTask(Messages.LaunchDelegate_Launching);
    			
    			IXdsConsoleFactory consoleFactory = ServiceHolder.getInstance().getConsoleFactory();
    			String consoleTitle = iProject.getName() + " [" + Messages.ConsoleType_XDS_App + "] " + exeName; //$NON-NLS-1$ //$NON-NLS-2$
    			final IXdsConsole console = consoleFactory.getXdsConsole( consoleTitle );
    			console.setEncoding(consoleEncoding);
    			if (!standalone) {
    				console.clearConsole();
    				console.show();
    			}
    			
    			final List<String> cmdLineList = prepareCommandline(sdk, cmdline);
    			if (standalone) {
    				launch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, "false");
    				cmdLineList.add(0,"start"); //$NON-NLS-1$
    				cmdLineList.add(0,"/c"); //$NON-NLS-1$
    				cmdLineList.add(0,"cmd.exe"); //$NON-NLS-1$
    			}
    			ProcessManager processManager = new ProcessManager(launch, cmdLineList.toArray(new String[0]), workingDir, envp, exeName, new HashMap<String, String>());
    			processManager.addListener(new AbstractProcessListener(console, consoleEncoding) {
    				@Override
    				public void beforeProcessStart() {
    					println(ColorStreamType.SYSTEM, getReadableCommandline(cmdLineList));
    				}
    				
    				@Override
    				public void processStarted(final IProcess iProcess) {
    					console.enableTerminateButton(true);
    					console.setTerminateCallback(new IXdsConsoleTerminateCallback() {
    						@Override
    						public void terminate() {
    							try {
    								console.enableTerminateButton(false);
    								iProcess.terminate();
    							} catch (DebugException e) {
    								// process can not be terminated - ignore
    							}
    						}
    					});
    				}
    				
    				@Override
    				public void processStdoutRead(String chunk) {
    					print(ColorStreamType.NORMAL, chunk);
    				}

    				@Override
    				public void processStderrRead(String chunk) {
    					print(ColorStreamType.ERROR, chunk);
    				}
    				
    				@Override
    				public void afterProcessEnd(int exitValue) {
    					ColorStreamType cst = exitValue == 0 ? ColorStreamType.SYSTEM : ColorStreamType.ERROR;
    					println(cst, Messages.LaunchDelegate_ProcessFinishedWithCode + exitValue);
    					console.enableTerminateButton(false);
    				}
    			});
    			processManager.start();
    		}
    		catch(LaunchException e) {
    			if (e.isEditConfiguration()) {
    				openEditConfigurationPrompt(configuration, e.getMessage());
    			}
    			else {
    				throw e;
    			}
    		}
    	}
    	finally{
    		if (!ILaunchManager.RUN_MODE.equals(mode)) {
    			PerspectiveUtils.showPerspective(IUiConstants.DEVELOPMENT_PERSPECTIVE_ID);
    		}
    	}
	}

	private static String buildCommandLine(ILaunchConfiguration configuration,
			Sdk sdk, boolean isDebug, boolean isProfile, String exeName)
			throws CoreException {
		// Arguments (as is from 'Run configurations' dialog - with \n, quotes etc.) 
		String pgmArgs = configuration.getAttribute(ILaunchConfigConst.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
		String dbgArgs = configuration.getAttribute(ILaunchConfigConst.ATTR_DEBUGGER_ARGUMENTS, ""); //$NON-NLS-1$
		String simArgs = configuration.getAttribute(ILaunchConfigConst.ATTR_SIMULATOR_ARGUMENTS, ""); //$NON-NLS-1$

		StringBuilder sb = new StringBuilder();
		if (isDebug || isProfile) {
			if (isDebug) {
				// xd.exe <dbg args> zz.exe <program args>
				appendArg(sb, enquoteIfHasSpace(sdk.getDebuggerExecutablePath()), Messages.LaunchDelegate_DebuggerLocation);
				sb.append(' ').append(dbgArgs);
			} else {
				// xdp.exe zz.exe <program args>
				appendArg(sb, enquoteIfHasSpace(sdk.getProfilerExecutablePath()), Messages.LaunchDelegate_ProfilerLocation);
			}
			sb.append(' ');
			appendArg(sb, exeName, Messages.LaunchDelegate_ExeOrPktName);
			sb.append(' ').append(pgmArgs); 
		} 
		else {
			String simulatorPath = sdk.getSimulatorExecutablePath();
			if (Sdk.isSet(simulatorPath)) {
				// simulator.exe <simulator args> application_exe <program args>
				if (!(new File(simulatorPath)).canExecute()) {
					abort(Messages.LaunchDelegate_InvalidSimulatorFileName + ": " + simulatorPath); //$NON-NLS-1$
				}
				appendArg(sb, enquoteIfHasSpace(simulatorPath), Messages.LaunchDelegate_SimulatorExe);
				sb.append(' ').append(simArgs).append(' ');
				appendArg(sb, enquoteIfHasSpace(exeName), Messages.LaunchDelegate_ExeName);
				sb.append(' ').append(pgmArgs);
			} else {
				// application_exe <program args>
				if (!(new File(exeName)).canExecute()) {
					abortLaunch(Messages.LaunchDelegate_FileIsNotExecutable + ": " + exeName); //$NON-NLS-1$
				}
				appendArg(sb, enquoteIfHasSpace(exeName), Messages.LaunchDelegate_ExeName);
				sb.append(' ').append(pgmArgs);
			}
		}
		return sb.toString();
	}
}
