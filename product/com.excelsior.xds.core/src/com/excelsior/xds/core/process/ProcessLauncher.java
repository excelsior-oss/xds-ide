package com.excelsior.xds.core.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import com.excelsior.xds.core.XdsCorePlugin;
import com.excelsior.xds.core.console.ColorStreamType;
import com.excelsior.xds.core.console.IXdsConsole;
import com.excelsior.xds.core.console.IXdsConsoleTerminateCallback;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.progress.IListenableProgressMonitor;
import com.excelsior.xds.core.progress.IProgressMonitorListener;

public final class ProcessLauncher {
	public static final int LAUNCH_ERROR_CODE = 100;
	
	private List<String> cmdlist;
	private File workDir;
	private Map<String, String> envmap;
	private Map<String, String> envadd;

	private IXdsConsole console;
	private OutputStream normConsoleStream;
	private OutputStream errConsoleStream;
	private InputStream  inputFromConsoleStream;

	private volatile Process proc;
    private volatile int exitValue = 2;
	
	private List<InputStreamListener> processStdoutListeners = new ArrayList<InputStreamListener>();
	private List<InputStreamListener> processStderrListeners = new ArrayList<InputStreamListener>();
	
	private final ProgressMonitorListener progressMonitorListener = new ProgressMonitorListener();

	private IListenableProgressMonitor monitor;
	
	public void addProcessStdoutListener(InputStreamListener inputStreamListener) {
		processStdoutListeners.add(inputStreamListener);
	}
	
	public void addProcessStderrListener(InputStreamListener inputStreamListener) {
		processStderrListeners.add(inputStreamListener);
	}

	/**
	 * Set monitor. When set - it will be periodicaly checked for 
	 * isCanceled() and process will be terminated if need.
	 * 
	 * @param monitor
	 */
	public void setMonitor(IListenableProgressMonitor monitor) {
		this.monitor = monitor;
		monitor.setListener(progressMonitorListener);
	}

	/**
	 * Set console for the process to be launched
	 * 
	 * @param console
	 * @param bindTerminateButtonOnly - when true - just bind console's terminate button
	 *        to the process, false - redirect process output to the console too
	 */
	public void setConsole(IXdsConsole console, boolean bindTerminateButtonOnly) {
		this.console = console;
		
		if (!bindTerminateButtonOnly) {
    		normConsoleStream = console.getConsoleStream(ColorStreamType.NORMAL);
    		errConsoleStream = console.getConsoleStream(ColorStreamType.ERROR);
    		inputFromConsoleStream = console.getInputStream();
		}
		
		console.setTerminateCallback(new IXdsConsoleTerminateCallback() {
			@Override
			public void terminate() {
				if (proc != null) {
					try {
						proc.destroy();
					} catch (Exception ex) {}
				}
			}
		});
	}
	
	/**
	 * Sets 'raw' commandline with program and arguments. 
	 *  
	 * @param cmdlist
	 */
	public void setCommandline(List<String> cmdlist) {
		this.cmdlist = cmdlist;
	}
	
	/**
	 * Returns commandline with opened variables etc. 
	 * as it will be processed at launch time 
	 * @return
	 */
	public String getReadableCommandline() {
		StringBuilder sb = new StringBuilder();
		if (cmdlist != null) {
			for (String s : cmdlist) {
				if (sb.length() > 0) {
					sb.append(' ');
				}
				s = s.replace("\"", "\"\""); //$NON-NLS-1$ //$NON-NLS-2$
				if (s.indexOf(' ') >= 0) {
					sb.append('"').append(s).append('"');
				} else {
					sb.append(s);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Set process working directory (or null to use default)
	 * @param workDir
	 */
	public void setWorkingDirectory(File workDir) {
		this.workDir = workDir;
	}

	/**
	 * Set environment variables (or null to use default)
	 * @param envmap - map with <name, value> pairs
	 */
	public void setEnvironment(Map<String, String> envmap) {
		this.envmap = envmap;
	}

	/**
	 * Set environment variables (or null to use default)
	 * @param envarray - strings in form "name=value"
	 */
	public void setEnvironment(String[] envarray) {
		if (envarray == null) {
			envmap = null;
		} else {
    	    envmap = new HashMap<String, String>(); 
    	    for (String env : envarray) {
    	    	int pos = env.indexOf('=');
    	    	if (pos > 0) {
    	    		String var = env.substring(0,pos).trim();
    	    		String val = env.substring(pos+1);
    	    		envmap.put(var,  val);
    	    	}
    	    }
		}
	}
	
	/**
	 * Add the given environment variables to other. 
	 * When setEnvironment is used it redefines all default environment
	 * variables. addEnvironment adds the given variables to the default
	 * (or to the variables set in setEnvironment if it was used) so
	 * it gives easy way to add numeral variables to the default set.
	 * @param envmap - map with <name, value> pairs
	 */
	public void addEnvironment(Map<String, String> envadd) {
		if (this.envadd == null) {
			this.envadd = new HashMap<String, String>();
		}
		this.envadd.putAll(envadd);
	}
	public void addEnvironment(String name, String value) {
		HashMap<String, String> hm = new HashMap<String, String>();
		hm.put(name, value);
		addEnvironment(hm);
	}

	/**
	 * Launches all.
	 * @return false if launching was aborted with monitor
	 * @throws CoreException
	 */
	public synchronized boolean launch() throws CoreException {
		if (!(new File(cmdlist.get(0)).canExecute())) {
            abort(String.format("XDS launching error: can't execute \"%s\"", cmdlist.get(0)), null, LAUNCH_ERROR_CODE); //$NON-NLS-1$
		}
		
		if (monitor != null && monitor.isCanceled()) {
			return false;
		}
		
    	ProcessBuilder builder = new ProcessBuilder(cmdlist.toArray(new String[]{}));
    	builder.directory(workDir);
        if (envmap != null) {
            builder.environment().clear();
            builder.environment().putAll(envmap);
    	}
    	addEnvCaseInsensitive(builder.environment());

        try {
        	proc = builder.start();
        } catch (IOException e) {
        	abort (e.getMessage(), e, LAUNCH_ERROR_CODE);
        }

	    enableTerminateButton(true);
	    
	    InputStreamCopyObserver stdoutObserver = new InputStreamCopyObserver(proc.getInputStream(), normConsoleStream, processStdoutListeners);
	    InputStreamCopyObserver stderrObserver = new InputStreamCopyObserver(proc.getErrorStream(), errConsoleStream, processStderrListeners);
	    stdoutObserver.start();
	    stderrObserver.start();
	    if (inputFromConsoleStream != null) {
	    	InputStreamCopyObserver consoleInputStreamObserver = new InputStreamCopyObserver(inputFromConsoleStream, proc.getOutputStream(), null);
	    	consoleInputStreamObserver.start();
	    }
	    
	    afterProcessTerminated(proc);
	    
    	try {
			stdoutObserver.join();
			stderrObserver.join();
		} 
    	catch (InterruptedException e) {
		}
    	
    	if (monitor != null && !(monitor instanceof NullProgressMonitor)) {
    		if (monitor.isCanceled()) {
    			System.out.println("monitor.isCanceled():"+monitor.isCanceled());
    			System.out.println(cmdlist);
    		}
    	}
	    
		return true;
	}
	
	private final class ProgressMonitorListener implements
			IProgressMonitorListener {
		@Override
		public void onSetCanceled(IProgressMonitor monitor, boolean value) {
			if (value) {
				proc.destroy();
			}
		}
	}

	private static class InputStreamCopyObserver extends InputStreamObserver{
		public InputStreamCopyObserver(InputStream fromStream, final OutputStream toStream,  List<InputStreamListener> streamListeners) {
			super(fromStream);
			addListeners(streamListeners);
			if (toStream != null) {
				addListener(new InputStreamListener() {
					@Override
					public void onHasData(byte[] buffer, int length) {
						try {
							toStream.write(buffer, 0, length);
						} catch (IOException e) {
							LogHelper.logError(e);
						}
					}

					@Override
					public void onEndOfStreamReached() {
					}
				});
			}
		}

		private void addListeners(List<InputStreamListener> streamListeners) {
			if (!CollectionUtils.isEmpty(streamListeners)) {
				for (InputStreamListener listener : streamListeners) {
					addListener(listener);
				}
			}
		}
	}
	
	private void addEnvCaseInsensitive(Map<String, String> env) {
		if (envadd == null || envadd.isEmpty()) {
			return;
		}
		
		HashSet<String> addvars = new HashSet<String>();
		for (String var : envadd.keySet()) {
			addvars.add(var.toLowerCase());
		}
		
		for (Iterator<String> i = env.keySet().iterator(); i.hasNext(); )  
		{    
		    String v = i.next();  
			if (addvars.contains(v.toLowerCase())) {
				i.remove();
			}
		}
		
		env.putAll(envadd);
	}
	
	/**
	 * 
	 * @return process exit code
	 */
	public int exitValue() {
		return exitValue;
	}

	protected void enableTerminateButton(boolean en) {
		if (console != null) {
			console.enableTerminateButton(en);
		}
	}
	
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, XdsCorePlugin.PLUGIN_ID,
				code, message, exception));
	}
	
	/**
	 * Waits for process to terminate and performs final actions
	 */
	private void afterProcessTerminated(final Process process) {
	    try {
            proc.waitFor();
        } catch (InterruptedException e) {
        }
	    exitValue = proc.exitValue();
        enableTerminateButton(false);
	}
}
