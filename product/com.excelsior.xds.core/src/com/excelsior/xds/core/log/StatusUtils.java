package com.excelsior.xds.core.log;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

public abstract class StatusUtils {
    public static boolean isError(IStatus status) {
        return 0 != (status.getSeverity() & IStatus.ERROR);
    }
    
    public static boolean isWarning(IStatus status) {
        return 0 != (status.getSeverity() & IStatus.WARNING);
    }
    
    public static void logIfErrorOrWarning(String messageIfError, String messageIfWarning, IStatus status) {
        if (isError(status)) {
            LogHelper.logError(messageIfError, new CoreException(status));
        }
        else if (isWarning(status)) {
            LogHelper.logWarning(messageIfWarning, new CoreException(status));
        }
    }
}
