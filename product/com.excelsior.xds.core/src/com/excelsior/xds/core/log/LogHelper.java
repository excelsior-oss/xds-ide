package com.excelsior.xds.core.log;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.excelsior.xds.core.XdsCorePlugin;

/**
 * The logger of convenience for the Excelsior XDS plug-in.
 */
public final class LogHelper {
	
    private static final String XDS_LOG_RUNTIME_PROPERTY = "XDS_IDE_LOGGING"; //$NON-NLS-1$
    
    private LogHelper(){
    }

    public static boolean isLogModeON() {
        return "on".equals(System.getProperty(XDS_LOG_RUNTIME_PROPERTY));    //$NON-NLS-1$
    }
	
    public static void logInfoInLogMode(String message) {
        if (isLogModeON()) {
            logInfo(message);
        }
    }
	
    /**
     * Log the specified information.
     * 
     * @param message, a human-readable message, localized to the
     *           current locale.
     */
    public static void logInfo(String message) {
        log(IStatus.INFO, IStatus.OK, message, null);
    }


    /**
     * Log the specified error.
     * 
     * @param message, a human-readable message, localized to the
     *           current locale.
     * @param exception, a low-level exception, or <code>null</code>
     *           if not applicable.
     */
    public static void logError(String message, Throwable exception) {
        log(IStatus.ERROR, IStatus.ERROR, message, exception);
    }
    
    public static IStatus createExceptionStatus(Throwable exception) {
    	return createStatus(IStatus.ERROR, IStatus.ERROR, "Unexpected Exception", exception);
    }
    
    public static IStatus createErrorStatus(String errorMessage) {
    	return createStatus(IStatus.ERROR, IStatus.ERROR, errorMessage, null);
    }
    
    public static IStatus createWarningStatus(String message) {
    	return createStatus(IStatus.WARNING, IStatus.WARNING, message, null);
    }
    
    public static IStatus createInfoStatus(String message) {
    	return createStatus(IStatus.INFO, IStatus.INFO, message, null);
    }

    /**
     * Log the specified error.
     * 
     * @param message, a human-readable message, localized to the
     *           current locale.
     */
    public static void logError(String message) {
        logError(message, null);
    }

    /**
     * Log the specified error.
     * 
     * @param exception, a low-level exception.
     */
    public static void logError(Throwable exception) {
        logError("Unexpected Exception", exception); //$NON-NLS-1$
    }


    
    /**
     * Log the specified warning.
     * 
     * @param message, a human-readable message, localized to the
     *           current locale.
     * @param exception, a low-level exception, or <code>null</code>
     *           if not applicable.
     */
    public static void logWarning(String message, Throwable exception) {
        log(IStatus.WARNING, IStatus.WARNING, message, exception);
    }

    /**
     * Log the specified warning.
     * 
     * @param message, a human-readable message, localized to the
     *           current locale.
     */
    public static void logWarning(String message) {
        logWarning(message, null);
    }

    /**
     * Log the specified warning.
     * 
     * @param exception, a low-level exception.
     */
    public static void logWarning(Throwable exception) {
        logWarning("Unexpected Exception", exception); //$NON-NLS-1$
    }



    /**
     * Log the specified information.
     * 
     * @param severity, the severity; one of the following:
     *           <code>IStatus.OK</code>,
     *           <code>IStatus.ERROR</code>,
     *           <code>IStatus.INFO</code>, or
     *           <code>IStatus.WARNING</code>.
     * @param pluginId. the unique identifier of the relevant
     *           plug-in.
     * @param code, the plug-in-specific status code, or
     *           <code>OK</code>.
     * @param message, a human-readable message, localized to the
     *           current locale.
     * @param exception, a low-level exception, or <code>null</code>
     *           if not applicable.
     */
    public static void log(int severity, int code, String message, Throwable exception) {
        log(createStatus(severity, code, message, exception));
    }


    /**
     * Create a status object representing the specified information.
     * 
     * @param severity, the severity; one of the following:
     *           <code>IStatus.OK</code>,
     *           <code>IStatus.ERROR</code>,
     *           <code>IStatus.INFO</code>, or
     *           <code>IStatus.WARNING</code>.
     * @param pluginId, the unique identifier of the relevant
     *           plug-in.
     * @param code, the plug-in-specific status code, or
     *           <code>OK</code>.
     * @param message, a human-readable message, localized to the
     *           current locale.
     * @param exception, a low-level exception, or <code>null</code>
     *           if not applicable.
     * @return, the status object (not <code>null</code>).
     */
    public static IStatus createStatus(int severity, int code, String message, Throwable exception) {
        return new Status(severity, XdsCorePlugin.PLUGIN_ID, code, message, exception);
    }


    /**
     * Log the given status.
     * 
     * @param status, the status to log.
     */
    public static void log(IStatus status) {
        XdsCorePlugin.getDefault().getLog().log(status);
    }

}
