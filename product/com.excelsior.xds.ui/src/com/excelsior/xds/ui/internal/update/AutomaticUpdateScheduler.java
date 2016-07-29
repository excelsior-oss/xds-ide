package com.excelsior.xds.ui.internal.update;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.internal.p2.ui.sdk.scheduler.AutomaticUpdatePlugin;
import org.eclipse.equinox.internal.p2.ui.sdk.scheduler.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IStartup;

import com.excelsior.xds.core.updates.resources.XdsResourcesUpdater;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;

@SuppressWarnings("restriction")
public class AutomaticUpdateScheduler implements IStartup {
	// values are to be picked up from the arrays DAYS and HOURS
	public static final String P_DAY = "day"; //$NON-NLS-1$

	public static final String P_HOUR = "hour"; //$NON-NLS-1$

	public static final String[] DAYS;

	public static final String[] HOURS = {AutomaticUpdateMessages.SchedulerStartup_1AM, AutomaticUpdateMessages.SchedulerStartup_2AM, AutomaticUpdateMessages.SchedulerStartup_3AM, AutomaticUpdateMessages.SchedulerStartup_4AM, AutomaticUpdateMessages.SchedulerStartup_5AM, AutomaticUpdateMessages.SchedulerStartup_6AM, AutomaticUpdateMessages.SchedulerStartup_7AM, AutomaticUpdateMessages.SchedulerStartup_8AM, AutomaticUpdateMessages.SchedulerStartup_9AM, AutomaticUpdateMessages.SchedulerStartup_10AM, AutomaticUpdateMessages.SchedulerStartup_11AM, AutomaticUpdateMessages.SchedulerStartup_12PM, AutomaticUpdateMessages.SchedulerStartup_1PM, AutomaticUpdateMessages.SchedulerStartup_2PM, AutomaticUpdateMessages.SchedulerStartup_3PM, AutomaticUpdateMessages.SchedulerStartup_4PM,
			AutomaticUpdateMessages.SchedulerStartup_5PM, AutomaticUpdateMessages.SchedulerStartup_6PM, AutomaticUpdateMessages.SchedulerStartup_7PM, AutomaticUpdateMessages.SchedulerStartup_8PM, AutomaticUpdateMessages.SchedulerStartup_9PM, AutomaticUpdateMessages.SchedulerStartup_10PM, AutomaticUpdateMessages.SchedulerStartup_11PM, AutomaticUpdateMessages.SchedulerStartup_12AM,};

	private IUpdateListener listener = null;
	private IUpdateChecker checker = null;

	static {
		Calendar calendar = Calendar.getInstance(new ULocale(Platform.getNL()));
		String[] daysAsStrings = {AutomaticUpdateMessages.SchedulerStartup_day, AutomaticUpdateMessages.SchedulerStartup_Sunday, AutomaticUpdateMessages.SchedulerStartup_Monday, AutomaticUpdateMessages.SchedulerStartup_Tuesday, AutomaticUpdateMessages.SchedulerStartup_Wednesday, AutomaticUpdateMessages.SchedulerStartup_Thursday, AutomaticUpdateMessages.SchedulerStartup_Friday, AutomaticUpdateMessages.SchedulerStartup_Saturday};
		int firstDay = calendar.getFirstDayOfWeek();
		DAYS = new String[8];
		DAYS[0] = daysAsStrings[0];
		int countDays = 0;
		for (int i = firstDay; i <= 7; i++) {
			DAYS[++countDays] = daysAsStrings[i];
		}
		for (int i = 1; i < firstDay; i++) {
			DAYS[++countDays] = daysAsStrings[i];
		}
	}

	/**
	 * The constructor.
	 */
	public AutomaticUpdateScheduler() {
		checker = new UpdateChecker();
		IPreferenceStore pref = AutomaticUpdatePlugin.getDefault().getPreferenceStore();
		pref.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				rescheduleUpdate();
			}
		});
	}

	public void earlyStartup() {
		scheduleUpdate();
	}

	public void shutdown() {
		removeUpdateListener();
	}

	public void rescheduleUpdate() {
		removeUpdateListener();
		IPreferenceStore pref = AutomaticUpdatePlugin.getDefault().getPreferenceStore();
		String schedule = pref.getString(PreferenceConstants.PREF_AUTO_UPDATE_SCHEDULE);
		// See if we have a scheduled check or startup only.  If it is
		// startup only, there is nothing more to do now, a listener will
		// be created on the next startup.
		if (schedule.equals(PreferenceConstants.PREF_UPDATE_ON_STARTUP)) {
			return;
		}
		scheduleUpdate();
	}

	private void scheduleUpdate() {
		IPreferenceStore pref = AutomaticUpdatePlugin.getDefault().getPreferenceStore();
		// See if automatic search is enabled at all
		if (!pref.getBoolean(PreferenceConstants.PREF_AUTO_UPDATE_ENABLED))
			return;
		String schedule = pref.getString(PreferenceConstants.PREF_AUTO_UPDATE_SCHEDULE);
		long delay = IUpdateChecker.ONE_TIME_CHECK;
		long poll = IUpdateChecker.ONE_TIME_CHECK;
		if (!schedule.equals(PreferenceConstants.PREF_UPDATE_ON_STARTUP)) {
			delay = computeDelay(pref);
			poll = computePoll(pref);
		}
		listener = new IUpdateListener() {
			public void updatesAvailable() {
				UpdateManager.runUpdateHelpPluginRoutines(false);
				XdsResourcesUpdater.getInstance().updateResources();
			}
		};
		checker.addUpdateCheck(delay, poll, listener);
	}

	private int getDay(IPreferenceStore pref) {
		String day = pref.getString(P_DAY);
		for (int d = 0; d < DAYS.length; d++)
			if (DAYS[d].equals(day))
				switch (d) {
					case 0 :
						return -1;
					case 1 :
						return Calendar.MONDAY;
					case 2 :
						return Calendar.TUESDAY;
					case 3 :
						return Calendar.WEDNESDAY;
					case 4 :
						return Calendar.THURSDAY;
					case 5 :
						return Calendar.FRIDAY;
					case 6 :
						return Calendar.SATURDAY;
					case 7 :
						return Calendar.SUNDAY;
				}
		return -1;
	}

	private int getHour(IPreferenceStore pref) {
		String hour = pref.getString(P_HOUR);
		for (int h = 0; h < HOURS.length; h++)
			if (HOURS[h].equals(hour))
				return h + 1;
		return 1;
	}

	/*
	 * Computes the number of milliseconds from this moment to the next
	 * scheduled update check. If that moment has already passed, returns 0L (start
	 * immediately).
	 */
	private long computeDelay(IPreferenceStore pref) {

		int target_d = getDay(pref);
		int target_h = getHour(pref);

		Calendar calendar = Calendar.getInstance();
		// may need to use the BootLoader locale
		int current_d = calendar.get(Calendar.DAY_OF_WEEK);
		// starts with SUNDAY
		int current_h = calendar.get(Calendar.HOUR_OF_DAY);
		int current_m = calendar.get(Calendar.MINUTE);
		int current_s = calendar.get(Calendar.SECOND);
		int current_ms = calendar.get(Calendar.MILLISECOND);

		long delay = 0L; // milliseconds

		if (target_d == -1) {
			// Compute the delay for "every day at x o'clock"
			// Is it now ?
			if (target_h == current_h && current_m == 0 && current_s == 0)
				return delay;

			int delta_h = target_h - current_h;
			if (target_h <= current_h)
				delta_h += 24;
			delay = ((delta_h * 60 - current_m) * 60 - current_s) * 1000 - current_ms;
			return delay;
		}
		// Compute the delay for "every Xday at x o'clock"
		// Is it now ?
		if (target_d == current_d && target_h == current_h && current_m == 0 && current_s == 0)
			return delay;

		int delta_d = target_d - current_d;
		if (target_d < current_d || target_d == current_d && (target_h < current_h || target_h == current_h && current_m > 0))
			delta_d += 7;

		delay = (((delta_d * 24 + target_h - current_h) * 60 - current_m) * 60 - current_s) * 1000 - current_ms;
		return delay;
	}

	/*
	 * Computes the number of milliseconds for the polling frequency.
	 * We have already established that there is a schedule, vs. only
	 * on startup.
	 */
	private long computePoll(IPreferenceStore pref) {

		int target_d = getDay(pref);
		if (target_d == -1) {
			// Every 24 hours
			return 24 * 60 * 60 * 1000;
		}
		return 7 * 24 * 60 * 60 * 1000;
	}

	private void removeUpdateListener() {
		// Remove the current listener if there is one
		if (listener != null && checker != null) {
			checker.removeUpdateCheck(listener);
			listener = null;
		}
	}
}
