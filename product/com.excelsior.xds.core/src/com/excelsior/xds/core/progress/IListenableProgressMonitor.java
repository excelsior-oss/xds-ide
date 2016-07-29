package com.excelsior.xds.core.progress;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Observalble extension of the {@link org.eclipse.core.runtime.IProgressMonitor}. 
 * @author lsa
 */
public interface IListenableProgressMonitor extends IProgressMonitor {
	void setListener(IProgressMonitorListener l);
}
