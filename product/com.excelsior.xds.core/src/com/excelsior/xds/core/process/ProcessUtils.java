package com.excelsior.xds.core.process;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;

/**
 * Utilities to work with OS processes.
 * @author lsa80
 */
public final class ProcessUtils {
	/**
	 * @param cmdLineArgs - command line, [0] should be executable path
	 * @param workingDirectory
	 * @param environmentVariables
	 * @return stdout contents
	 * @throws CoreException if something gone wrong
	 */
	public static String launchProcessAndCaptureStdout(String[] cmdLineArgs, File workingDirectory,
			String[] environmentVariables) throws CoreException {
		ProcessLauncher launcher = new ProcessLauncher();
		launcher.setCommandline(Arrays.asList(cmdLineArgs));
		launcher.setEnvironment(environmentVariables);
		launcher.setWorkingDirectory(workingDirectory);
		final StringBuilder sb = new StringBuilder();
		launcher.addProcessStdoutListener(new InputStreamListener() {
			@Override
			public void onHasData(byte[] buffer, int length) {
				sb.append(new String(buffer, 0, length));
			}
			
			@Override
			public void onEndOfStreamReached() {
			}
		});
		launcher.launch();
		return sb.toString();
	}
	
	private ProcessUtils(){
	}
}
