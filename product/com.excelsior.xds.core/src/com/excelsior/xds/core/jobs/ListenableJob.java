package com.excelsior.xds.core.jobs;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.jobs.Job;

public abstract class ListenableJob extends Job {
	private final List<IJobListener> listeners = new CopyOnWriteArrayList<>();
	
	public ListenableJob(String name) {
		super(name);
	}
	
	public void addListener(IJobListener l) {
		listeners.add(l);
	}
	
	public void removeListener(IJobListener l) {
		listeners.remove(l);
	}
	
	@Override
	protected void canceling() {
		super.canceling();
		for (IJobListener l : listeners) {
			l.canceled();
		}
	}
}
