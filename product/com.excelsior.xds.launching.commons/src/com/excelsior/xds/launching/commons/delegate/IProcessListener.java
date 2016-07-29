package com.excelsior.xds.launching.commons.delegate;

import org.eclipse.debug.core.model.IProcess;

/**
 * Listens for the events of the process managed by the {@link ProcessManager} 
 * @author lsa
 */
public interface IProcessListener {

	void beforeProcessStart();
	
	void processStarted(IProcess process);
	
	void processStdoutRead(String chunk);
	
	void processStderrRead(String chunk);

	void afterProcessEnd(int exitValue);
}