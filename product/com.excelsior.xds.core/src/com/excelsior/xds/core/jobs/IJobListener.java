package com.excelsior.xds.core.jobs;

/**
 * Listener of the {@link org.eclipse.core.runtime.jobs.Job}.<br>
 * <br>
 * @author lsa
 */
public interface IJobListener {
	/**
	 * Job was canceled event.
	 */
	void canceled();
}
