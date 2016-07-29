package com.excelsior.xds.ui.internal.update;

import java.util.HashMap;

import com.excelsior.xds.core.log.LogHelper;

public class UpdateChecker implements IUpdateChecker {
	public static boolean DEBUG = false;
	public static boolean TRACE = false;
	
	public UpdateChecker() {
	}
	/**
	 * Map of IUpdateListener->UpdateCheckThread.
	 */
	private HashMap<IUpdateListener, UpdateCheckThread> checkers = new HashMap<IUpdateListener, UpdateCheckThread>();

	private class UpdateCheckThread extends Thread {
		boolean done = false;
		long poll, delay;
		IUpdateListener listener;

		UpdateCheckThread(long delay, long poll, IUpdateListener listener) {
			this.poll = poll;
			this.delay = delay;
			this.listener = listener;
		}

		public void run() {
			try {
				if (delay != ONE_TIME_CHECK && delay > 0) {
					Thread.sleep(delay);
				}
				while (!done) {
//					Collection<IInstallableUnit> iusWithUpdates = checkForUpdates(profileId, query);
//					boolean isHasUpdates = false;
//					if (isHasUpdates) {
//					}
					if (!done) {
						listener.updatesAvailable();
					}
					if (delay == ONE_TIME_CHECK || delay <= 0) {
						done = true;
					} else {
						Thread.sleep(poll);
					}
				}
			} catch (InterruptedException e) {
				// nothing
			} catch (Exception e) {
				LogHelper.logError(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.internal.provisional.p2.updatechecker.IUpdateChecker#addUpdateCheck(java.lang.String, long, long, org.eclipse.equinox.internal.provisional.p2.updatechecker.IUpdateListener)
	 */
	public void addUpdateCheck(long delay, long poll, IUpdateListener listener) {
		if (checkers.containsKey(listener))
			return;
		UpdateCheckThread thread = new UpdateCheckThread(delay, poll, listener);
		checkers.put(listener, thread);
		thread.start();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.internal.provisional.p2.updatechecker.IUpdateChecker#removeUpdateCheck(org.eclipse.equinox.internal.provisional.p2.updatechecker.IUpdateListener)
	 */
	public void removeUpdateCheck(IUpdateListener listener) {
		checkers.remove(listener);
	}
}
