package com.excelsior.xds.launching.commons.delegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IFlushableStreamMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.RuntimeProcess;

import com.excelsior.xds.core.exceptions.ExceptionHelper;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.utils.IClosure;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;
import com.excelsior.xds.core.utils.io.StreamUtils;
import com.excelsior.xds.launching.commons.internal.delegate.StreamsProxy;

/**
 * Helper class to integrate launched process to the Eclipse Run/Debug infrastructure.
 * @author lsa
 */
public final class ProcessManager {
	private ILaunch launch;
	private String[] cmdLine;
	private File workingDirectory;
	private String[] environmentVars;
	private String processLabel;
	private Map<String, String> iProcessAttributes;
	private final Set<IProcessListener> listeners = CollectionsUtils.newConcurentHashSet();
	
	private final AtomicBoolean isStdOutRead = new AtomicBoolean(false);
	private final AtomicBoolean isStdErrRead = new AtomicBoolean(false);
	
	private StreamsProxy streamsProxy;
	private String stdoutPipeName;
	private String stderrPipeName;
	private String stdinPipeName;
	
	private Process process;
	
	private String exitCodePipeName;
	/**
	 * Dedicated thread to read exit code from the exit code pipe
	 */
	private Thread exitCodeThread;
	
	private final AtomicInteger exitCode = new AtomicInteger();
	
	public ProcessManager(ILaunch launch, String[] cmdLine,  
			File workingDirectory, String[] environmentVars, String processLabel, Map<String, String> iProcessAttributes) {
		this(launch, cmdLine, null, null, null, null, workingDirectory, environmentVars, processLabel, iProcessAttributes);
	}
	
	public ProcessManager(ILaunch launch, String[] cmdLine, String stdoutPipeName, String stderrPipeName, String stdinPipeName, 
			String exitCodePipeName, File workingDirectory, String[] environmentVars, String processLabel, Map<String, String> iProcessAttributes) {
		this.launch = launch;
		this.cmdLine = cmdLine;
		this.workingDirectory = workingDirectory;
		
		this.stdoutPipeName = stdoutPipeName;
		this.stderrPipeName = stderrPipeName;
		this.stdinPipeName = stdinPipeName;
		this.exitCodePipeName = exitCodePipeName;
		
		this.environmentVars = environmentVars;
		this.processLabel = processLabel;
		this.iProcessAttributes = iProcessAttributes;
	}

	public void start() throws CoreException {
		visitListeners(new IClosure<IProcessListener>() {
			@Override
			public void execute(IProcessListener l)
					throws RuntimeException {
				l.beforeProcessStart();
			}
		});
		
		process = DebugPlugin.exec(cmdLine, workingDirectory, environmentVars);
		if (exitCodePipeName != null) {
			createAndStartExitCodeThread();
		}
		final IProcess iProcess = newProcess(launch, process, processLabel, iProcessAttributes);
		visitListeners(new IClosure<IProcessListener>() {
			@Override
			public void execute(IProcessListener l)
					throws RuntimeException {
				l.processStarted(iProcess);
			}
		});
		
		try {
			streamsProxy = new StreamsProxy(process, stdoutPipeName, stderrPipeName, stdinPipeName, launch.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING));
		} catch (FileNotFoundException e) {
			ExceptionHelper.rethrowAsCoreException(e);
		}
		streamsProxy.getOutputStreamMonitor().addListener(new IStreamListener() {
			StreamAppendedListenerVisitor visitor = new StreamAppendedListenerVisitor(isStdOutRead, streamsProxy.getOutputStreamMonitor()) {
				@Override
				protected void onStreamRead(IProcessListener l, String chunk) {
					l.processStdoutRead(chunk);
				}
			};
			
			@Override
			public void streamAppended(final String chunk, IStreamMonitor monitor) {
				visitor.chunk = chunk;
				visitListeners(visitor);
			}
		});
		streamsProxy.getErrorStreamMonitor().addListener(new IStreamListener() {
			StreamAppendedListenerVisitor visitor = new StreamAppendedListenerVisitor(isStdErrRead, streamsProxy.getErrorStreamMonitor()) {
				@Override
				protected void onStreamRead(IProcessListener l, String chunk) {
					l.processStderrRead(chunk);
				}
			};
			@Override
			public void streamAppended(final String chunk, IStreamMonitor monitor) {
				visitor.chunk = chunk;
				visitListeners(visitor);
			}
		});
		
		Thread monitorThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					waitFor();
				} catch (InterruptedException e) {
					LogHelper.logError(e);
				}
				
				visitListeners(new IClosure<IProcessListener>() {
					@Override
					public void execute(IProcessListener l)
							throws RuntimeException {
						if (isStdErrRead.compareAndSet(false, true)) {
							// some reads can be before we added the listener, so notify with the whole buffered content
							l.processStderrRead(streamsProxy.getErrorStreamMonitor().getContents());
						}
						if (isStdOutRead.compareAndSet(false, true)) {
							// some reads can be before we added the listener, so notify with the whole buffered content
							l.processStdoutRead(streamsProxy.getOutputStreamMonitor().getContents());
						}
						
						if (exitCodePipeName != null) {
							try {
								exitCodeThread.join();
								l.afterProcessEnd(exitCode.get());
							} catch (InterruptedException e) {
								LogHelper.logError(e);
							}
						}
						else {
							l.afterProcessEnd(process.exitValue());
						}
					}
				});
			}
		});
		
		monitorThread.start();
	}

	/**
	 * Start the dedicated thread to read the exit code
	 */
	private void createAndStartExitCodeThread() {
		exitCodeThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try (FileInputStream inputPipe = StreamUtils.tryOpenInputPipe(exitCodePipeName)){
					String exitCodeStr = IOUtils.toString(inputPipe);
					exitCode.set(Integer.parseInt(exitCodeStr));
				} catch (NumberFormatException | IOException e) {
					LogHelper.logError(e);
				}
			}
		});
		exitCodeThread.start();
	}
	
	public void waitFor() throws InterruptedException {
		process.waitFor(); // wait for the process to die
		streamsProxy.getOutputStreamMonitor().join(); // wait for the output stream monitor thread to die
		streamsProxy.getErrorStreamMonitor().join(); // wait for the error stream monitor thread to die
	}
	
	/**
	 * Visits every {@link IProcessListener} to invoke 'stream appended' notification.
	 * @author lsa
	 */
	private static abstract class StreamAppendedListenerVisitor implements IClosure<IProcessListener> {
		private final AtomicBoolean isFirstRead;
		private final IStreamMonitor streamMonitor;
		private String chunk;

		public StreamAppendedListenerVisitor(AtomicBoolean isFirstRead,
				IStreamMonitor streamMonitor) {
			this.isFirstRead = isFirstRead;
			this.streamMonitor = streamMonitor;
		}

		@Override
		public void execute(IProcessListener l) throws RuntimeException {
			if (isFirstRead.compareAndSet(false, true)) {
				// some reads can be before we added the listener, so notify with the whole buffered content
				onStreamRead(l, streamMonitor.getContents());
				// stop the buffering to reduce the memory footprint
				flushOutputStreamMonitor(streamMonitor);
			}
			else {
				onStreamRead(l, chunk);
			}
		}
		
		protected abstract void onStreamRead(IProcessListener l, String chunk);
		
		private void flushOutputStreamMonitor(IStreamMonitor streamMonitor) {
			if (streamMonitor instanceof IFlushableStreamMonitor) {
				IFlushableStreamMonitor flushableStreamMonitor = (IFlushableStreamMonitor) streamMonitor;
				flushableStreamMonitor.setBuffered(false);
				flushableStreamMonitor.flushContents(); // flush is necessary to reduce the memory footprint
			}
		}
	}
	
	public static IProcess newProcess(ILaunch launch, Process process, String label, Map<String, String> attributes) {
		return new RuntimeProcess(launch, process, label, attributes){
			protected IStreamsProxy createStreamsProxy() {
				return null;
			}
		};
	}
	
	public void setLaunch(ILaunch launch) {
		this.launch = launch;
	}

	public void setCmdLine(String[] cmdLine) {
		this.cmdLine = cmdLine;
	}

	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public void setEnvironmentVars(String[] environmentVars) {
		this.environmentVars = environmentVars;
	}

	public void setProcessLabel(String processLabel) {
		this.processLabel = processLabel;
	}

	public void addListener(IProcessListener l) {
		listeners.add(l);
	}
	
	public void removeListener(IProcessListener l) {
		listeners.remove(l);
	}
	
	private void visitListeners(IClosure<IProcessListener> operation) {
		for (IProcessListener l : listeners) {
			operation.execute(l);
		}
	}
}