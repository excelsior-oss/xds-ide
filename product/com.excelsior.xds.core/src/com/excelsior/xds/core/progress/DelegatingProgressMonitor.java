package com.excelsior.xds.core.progress;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Observable wrapper of the usual {@link org.eclipse.core.runtime.IProgressMonitor}.<br><br>
 * Know issues: does not works well in the case when monitor is canceled via the {@link org.eclipse.core.runtime.SubMonitor}.
 * @author lsa80
 */
public class DelegatingProgressMonitor implements IListenableProgressMonitor {
	private final IProgressMonitor delegateMonitor;
	private final List<IProgressMonitorListener> listeners = new CopyOnWriteArrayList<IProgressMonitorListener>(); 
	
	public DelegatingProgressMonitor(IProgressMonitor delegateMonitor) {
		this.delegateMonitor = delegateMonitor;
	}
	
	public static DelegatingProgressMonitor wrap(IProgressMonitor monitor) {
		return new DelegatingProgressMonitor(monitor);
	}
	
	public static DelegatingProgressMonitor nullProgressMonitor() {
		return wrap(new NullProgressMonitor());
	}
	
	public void setListener(IProgressMonitorListener l) {
		listeners.add(l);
	}
	
	public void beginTask(String name, int totalWork) {
		delegateMonitor.beginTask(name, totalWork);
	}

	public void done() {
		delegateMonitor.done();
	}

	public void internalWorked(double work) {
		delegateMonitor.internalWorked(work);
	}

	public boolean isCanceled() {
		return delegateMonitor.isCanceled();
	}

	public void setCanceled(boolean value) {
		delegateMonitor.setCanceled(value);
		
		for (IProgressMonitorListener l : listeners) {
			l.onSetCanceled(delegateMonitor, value);
		}
	}

	public void setTaskName(String name) {
		delegateMonitor.setTaskName(name);
	}

	public void subTask(String name) {
		delegateMonitor.subTask(name);
	}

	public void worked(int work) {
		delegateMonitor.worked(work);
	}
}