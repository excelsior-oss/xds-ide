package com.excelsior.xds.core.tool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;

import com.excelsior.xds.core.console.ColorStreamType;
import com.excelsior.xds.core.console.IXdsConsole;
import com.excelsior.xds.core.console.IXdsConsoleTerminateCallback;
import com.excelsior.xds.core.internal.nls.Messages;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.process.InputStreamListener;
import com.excelsior.xds.core.process.InputStreamObserver;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.project.XdsProjectType;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkTool;
import com.excelsior.xds.core.variables.VariableUtils;

/**
 * Tool which invokes the specified SDK tool file on given resource elements
 * @author lsa80
 */
public class XTool implements ITool {
    private SdkTool toolDesc;
    private XdsProjectType xdsProjectType;
    private boolean terminated;
    
	private List<IToolListener> listeners = new CopyOnWriteArrayList<IToolListener>(); 
	private boolean isConsoleButtonEnabled;
	
	XTool(SdkTool toolDesc) 
	{
	    Assert.isTrue(!toolDesc.isSeparator(), "XTool() is called for separator"); //$NON-NLS-1$
	    this. toolDesc = toolDesc;

	    switch (toolDesc.getSourceRoot()) {
		case PRJ_FILE:
		    this.xdsProjectType = XdsProjectType.PROJECT_FILE;
		    break;
		case MAIN_MODULE:
		    this.xdsProjectType = XdsProjectType.MAIN_MODULE;
		    break;
		default:
		    this.xdsProjectType = null;
		}
	}
	
    @Override
    public String getName() {
        return toolDesc.getToolName();
    }
    
    @Override
    public String getLocation() {
        return toolDesc.getLocation();
    }
	
	@Override
	public boolean isEnabled(List<IResource> resources) {
		try {
	        resources = filterResList(resources);
	        
	        if (resources.isEmpty()) {
	            return false; // no resource(s) to process
	        }
	        
	        // check tools working dir(s):
	        HashSet<IProject> prjs = new HashSet<IProject>();
	        for (IResource r : resources) {
	            prjs.add(r.getProject());
	        }
	        for (IProject p : prjs) {
	            XdsProjectSettings settings = XdsProjectSettingsManager.getXdsProjectSettings(p);
	            String dir = toolDesc.getWorkingDirectory(settings.getProjectType());
	            if (!StringUtils.isBlank(dir)) {
	                if (dir.contains("$")) { //$NON-NLS-1$
//	                    IValueVariable context = VariableUtils.setResolveContext(p);
	                    dir = VariableUtils.performStringSubstitution(p, dir);
//	                     VariableUtils.removeResolveContext(context);
	                }
	                if (!new File(dir).isDirectory()) {
	                    return false; // bad work dir
	                }
	            }
	        }
        } catch (CoreException e) {
            return false;
        }
		return true;
	}
	
	private List<IResource> filterResList(List<IResource> resources) {
	    
        // drop resources w/o MODULA2_SOURCE_PROJECT_NATURE_ID project
        resources = ResourceUtils.applyXdsResourcesFilter(resources);

        // drop resources w/o required extensions (if any)
        if (!CollectionUtils.isEmpty(toolDesc.getFileExtensionsList())) {
            ArrayList<IResource> resWithExt = new ArrayList<IResource>(); 
            HashSet<String>extensionsToRunOn = new HashSet<String>(toolDesc.getFileExtensionsList());
            for (IResource r : resources) {
                String ext = FilenameUtils.getExtension(ResourceUtils.getAbsolutePath(r)).toLowerCase();
                if (extensionsToRunOn.contains(ext)) {
                    resWithExt.add(r);
                }
            }
            resources = resWithExt;
        }

        // drop resources w/o required project type (if any)
        if (xdsProjectType != null) {
            ArrayList<IResource> resWithPT = new ArrayList<IResource>(); 
            for (IResource r : resources) {
                XdsProjectSettings settings = XdsProjectSettingsManager.getXdsProjectSettings(r.getProject());
                if (settings.getProjectType() == xdsProjectType) {
                    resWithPT.add(r);
                }
            }
            resources = resWithPT;
        }

        return resources;
	}
	
	/* XTool expects that all resources are from the same project  
	 */
	@Override
	public void invoke(List<IResource> resources, final IXdsConsole console) {
		final List<IResource> resToProcess = filterResList(resources);
        isConsoleButtonEnabled = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String str = String.format("%s [%s: %s]", toolDesc.getSdk().getName(), Messages.ConsoleType_XDS_Tool, getName()); //$NON-NLS-1$
                console.println(str, ColorStreamType.SYSTEM);
//                IValueVariable context = null;
                try {
                    terminated = false;
                    boolean first = true;
                    for (IResource r : resToProcess) {
                        if (!first) {
                            console.println("--------------------------------------", ColorStreamType.SYSTEM); //$NON-NLS-1$
                        }
                        first = false;
//                        context = VariableUtils.setResolveContext(r.getProject());
                        invokeOne(r, console);
//                        VariableUtils.removeResolveContext(context);
//                        context = null;
                        if (terminated) {
                            console.println("--------------------------------------", ColorStreamType.SYSTEM); //$NON-NLS-1$
                            console.println(Messages.XTool_ToolTerminated, ColorStreamType.SYSTEM);
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                } catch (CoreException e) {
                    console.println(e.getStatus().getMessage(), ColorStreamType.ERROR);
                    return;
                } catch (IOException e) {
                    LogHelper.logError(e);
                }
                finally{
//                    if (context != null) {
//                        VariableUtils.removeResolveContext(context);
//                    }
                    notifyListenersFinished();
                    console.enableTerminateButton(false);
                    console.setTerminateCallback(null);
                }
            }
        }, "XTool.waitForProcessToTerminateAndDisableButton Observer").start(); //$NON-NLS-1$
        
	    
	}
	    
	public void invokeOne(IResource resource, final IXdsConsole console) throws InterruptedException, CoreException, IOException {
	    Sdk sdk = toolDesc.getSdk();
	    XdsProjectSettings settings = XdsProjectSettingsManager.getXdsProjectSettings(resource.getProject());
	    String toolexe = getLocation();

	    if (!new File(toolexe).isFile()) {
	        throw new CoreException(LogHelper.createStatus(Status.ERROR, Status.ERROR, 
	                Messages.XTool_ToolFileNotFound + toolexe + "'", null)); //$NON-NLS-1$
	    }

	    List<String> cmdlst = new ArrayList<String>();
	    cmdlst.add('"' + toolexe + '"');     //$NON-NLS-1$  //$NON-NLS-2$
	    StringBuilder sb = new StringBuilder();
	    if (toolexe.contains(" ")) { //$NON-NLS-1$
	        sb.append('"').append(toolexe).append('"');
	    } else {
            sb.append(toolexe);
	    }

	    String arr[] = new StrTokenizer(VariableUtils.performStringSubstitution(resource.getProject(), toolDesc.getArguments(settings.getProjectType())), ' ', '"').getTokenArray();
	    for (String s : arr) {
	        cmdlst.add(s);
	        sb.append(' ');
	        if (s.contains(" ")) { //$NON-NLS-1$
	            sb.append('"').append(s).append('"');
	        } else {
	            sb.append(s);
	        }
	    }
	    console.println(sb.toString() + "\n", ColorStreamType.SYSTEM); //$NON-NLS-1$

	    final ProcessBuilder builder = new ProcessBuilder(cmdlst);

	    Map<String, String> envs = sdk.getEnvironmentVariables();
	    if (!MapUtils.isEmpty(envs)) {
	        // case-insensitive remove from builder.environment() variables to be redefined:
	        HashSet<String> hsNewNames = new HashSet<String>();
	        for (String s : envs.keySet()) {
	            hsNewNames.add(s.toLowerCase());
	        }
	        Set<String> oldset = new HashSet<String>();
	        oldset.addAll(builder.environment().keySet());
	        for (String s : oldset) {
	            if (hsNewNames.contains(s.toLowerCase())) {
	                builder.environment().remove(s);
	            }
	        }
	        // [re]define variables:
	        builder.environment().putAll(envs);
	    }

	    String actualWorkingDir = toolDesc.getWorkingDirectory(settings.getProjectType());
	    if (!StringUtils.isBlank(actualWorkingDir)) {
	        actualWorkingDir = VariableUtils.performStringSubstitution(actualWorkingDir);
	    } else {
	        actualWorkingDir = ResourceUtils.getAbsolutePath(resource.getProject());
	    }

	    if (!new File(actualWorkingDir).isDirectory()) {
	        throw new CoreException(LogHelper.createStatus(Status.ERROR, Status.ERROR, 
	                Messages.XTool_BadWorkDir + actualWorkingDir + "'", null)); //$NON-NLS-1$
	    }
	    builder.directory(new File(actualWorkingDir));

	    final Process process = builder.start();

	    console.setEncoding(toolDesc.getPropertyValue(SdkTool.Property.CONSOLE_CODEPAGE)); // invalid values treats as system default codepage

	    OutObserver oo = new OutObserver();
	    oo.observeStdoutAndStderr(console, process);

	    console.setTerminateCallback(new IXdsConsoleTerminateCallback() {
			@Override
			public void terminate() {
				 terminated = true;
		         process.destroy();
			}
		});
	    enableConsoleButton(console);

	    process.waitFor();
	    oo.waitFor();
        console.setEncoding(null);
	}
	
	private class OutObserver {
        InputStreamObserver stdinObserver;
        InputStreamObserver stErrObserver;

	    public void observeStdoutAndStderr(final IXdsConsole console,
	            final Process process) throws IOException {
	        stdinObserver = observeStream(console, ColorStreamType.NORMAL, process.getInputStream(), 1024);
	        stErrObserver = observeStream(console, ColorStreamType.ERROR, process.getErrorStream(), 1024);
	        stdinObserver.start();
	        stErrObserver.start();
	    }
	    
	    public void waitFor() {
	        try {
	            stdinObserver.join();
	            stErrObserver.join();
	        } catch (Exception e) {}
	    }
	    
	}

	@Override
	public void addListener(IToolListener listener) {
		listeners.add(listener);
	}
	
	private void enableConsoleButton(IXdsConsole console) {
		synchronized(console) {
			if (!isConsoleButtonEnabled) {
				console.enableTerminateButton(true);
				isConsoleButtonEnabled = true;
			}
		}
	}

	private InputStreamObserver observeStream(final IXdsConsole reportConsole, final ColorStreamType reportColorStreamType, InputStream inputStream, int bufferSize ) {
		InputStreamObserver streamObserver = new InputStreamObserver(inputStream, bufferSize);
		final OutputStream consoleStream = reportConsole.getConsoleStream(reportColorStreamType);
		streamObserver.addListener(new InputStreamListener() {
			@Override
			public void onHasData(byte[] buffer, int length) {
				try {
					consoleStream.write(buffer, 0, length);
					consoleStream.flush();
				} catch (IOException e) {
					LogHelper.logError(e);
				}
			}

			@Override
			public void onEndOfStreamReached() {
			}
		});
		return streamObserver;
	}

	private void notifyListenersFinished() {
		for (IToolListener listener : listeners) {
			listener.finished();
		}
	}
}