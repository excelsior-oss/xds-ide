package com.excelsior.xds.launching.commons.delegate;

import org.eclipse.debug.core.model.IProcess;

public class ProcessListenerAdapter implements IProcessListener {

	@Override
	public void beforeProcessStart() {
	}

	@Override
	public void processStarted(IProcess process) {
	}

	@Override
	public void processStdoutRead(String chunk) {
	}

	@Override
	public void processStderrRead(String chunk) {
	}

	@Override
	public void afterProcessEnd(int exitValue) {
	}
}
