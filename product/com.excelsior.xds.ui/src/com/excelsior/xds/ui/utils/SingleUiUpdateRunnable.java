package com.excelsior.xds.ui.utils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runnable for the UI update with the special property - no more than two instances (one executing, another is enqueued) of a runnable of the same group can 
 * simultaneously be on the SWT message queue. Belonging to the specific group is determined via {@code isUpdateRequestPending} variable.
 * For usage example see {@link com.excelsior.xds.ui.navigator.project.ProjectExplorerContentProvider}
 * @author lsa80
 */
public abstract class SingleUiUpdateRunnable implements Runnable {
	
	private final AtomicBoolean isUpdateRequestPending;
	
	public SingleUiUpdateRunnable(AtomicBoolean isUpdateRequestPending) {
		this.isUpdateRequestPending = isUpdateRequestPending;
	}

	@Override
	public void run() {
		isUpdateRequestPending.compareAndSet(true, false);
		doRun();
	}
	
	/**
	 * Does the actual UI job of the Runnable
	 */
	protected abstract void doRun();
}
