package com.excelsior.xds.ui.internal.update;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.ui.sdk.scheduler.AutomaticUpdateMessages;
import org.eclipse.equinox.internal.p2.ui.sdk.scheduler.AutomaticUpdatePlugin;
import org.eclipse.equinox.internal.p2.ui.sdk.scheduler.PreferenceConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.WorkbenchJob;

import com.excelsior.xds.ui.internal.nls.Messages;

@SuppressWarnings("restriction")
public class UpdateAvailableDialog extends Dialog {
	
	private IPreferenceStore prefs;
	private WorkbenchJob remindJob;
	private long remindDelay;
	private IPropertyChangeListener prefListener;
	
	public static final String[] ELAPSED_VALUES = {PreferenceConstants.PREF_REMIND_30Minutes, PreferenceConstants.PREF_REMIND_60Minutes, PreferenceConstants.PREF_REMIND_240Minutes};
	public static final String[] ELAPSED_LOCALIZED_STRINGS = {AutomaticUpdateMessages.AutomaticUpdateScheduler_30Minutes, AutomaticUpdateMessages.AutomaticUpdateScheduler_60Minutes, AutomaticUpdateMessages.AutomaticUpdateScheduler_240Minutes};
	
	private static final long MINUTE = 60 * 1000L;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public UpdateAvailableDialog(Shell parentShell) {
		super(parentShell);
		this.prefs = AutomaticUpdatePlugin.getDefault().getPreferenceStore();
		remindDelay = computeRemindDelay();
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		
		Label lblNewHelpUpdates = new Label(container, SWT.NONE);
		lblNewHelpUpdates.setText(Messages.UpdateAvailableDialog_UpdatesPending);

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				UpdateManager.refreshSourcesAndRunUpdateHelpPluginRoutines();
				
			}
		});
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(304, 124);
	}
	
	@Override
	public int open() {
		prefListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				handlePreferenceChange(event);
			}
		};
		prefs.addPropertyChangeListener(prefListener);
		return super.open();
	}

	@Override
	public boolean close() {
		if (prefs.getBoolean(PreferenceConstants.PREF_REMIND_SCHEDULE)){
			scheduleRemindJob();
		}
		else{
			cancelRemindJob();
		}
		if (prefListener != null) {
			prefs.removePropertyChangeListener(prefListener);
			prefListener = null;
		}
		return super.close();
	}

	private void cancelRemindJob() {
		if (remindJob != null) {
			remindJob.cancel();
			remindJob = null;
		}
	}

	private void scheduleRemindJob() {
		// Cancel any pending remind job if there is one
		if (remindJob != null)
			remindJob.cancel();
		// If no updates have been found, there is nothing to remind
		if (remindDelay < 0)
			return;
		remindJob = new WorkbenchJob(
				AutomaticUpdateMessages.AutomaticUpdatesPopup_ReminderJobTitle) {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				open();
				return Status.OK_STATUS;
			}
		};
		remindJob.setSystem(true);
		remindJob.setPriority(Job.INTERACTIVE);
		remindJob.schedule(remindDelay);
	}
	
	/*
	 * Computes the number of milliseconds for the delay
	 * in reminding the user of updates
	 */
	long computeRemindDelay() {
		if (prefs.getBoolean(PreferenceConstants.PREF_REMIND_SCHEDULE)) {
			String elapsed = prefs.getString(PreferenceConstants.PREF_REMIND_ELAPSED);
			for (int d = 0; d < ELAPSED_VALUES.length; d++)
				if (ELAPSED_VALUES[d].equals(elapsed))
					switch (d) {
						case 0 :
							// 30 minutes
							return 30 * MINUTE;
						case 1 :
							// 60 minutes
							return 60 * MINUTE;
						case 2 :
							// 240 minutes
							return 240 * MINUTE;
					}
		}
		return -1L;
	}
	
	private void handlePreferenceChange(PropertyChangeEvent event) {
		if (PreferenceConstants.PREF_REMIND_SCHEDULE.equals(event.getProperty())) {
			// Reminders turned on
			if (prefs.getBoolean(PreferenceConstants.PREF_REMIND_SCHEDULE)) {
				computeRemindDelay();
				scheduleRemindJob();
			} else { // reminders turned off
				cancelRemindJob();
			}
		} else if (PreferenceConstants.PREF_REMIND_ELAPSED.equals(event.getProperty())) {
			// Reminding schedule changed
			computeRemindDelay();
			scheduleRemindJob();
		}
	}
}
