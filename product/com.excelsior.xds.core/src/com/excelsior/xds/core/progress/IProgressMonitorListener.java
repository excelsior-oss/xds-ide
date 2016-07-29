package com.excelsior.xds.core.progress;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IProgressMonitorListener {
	void onSetCanceled(IProgressMonitor monitor, boolean value);
}
