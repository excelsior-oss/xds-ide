package com.excelsior.xds.core.exceptions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.excelsior.xds.core.log.LogHelper;

public final class ExceptionHelper {
	private ExceptionHelper(){
	}
	
	public static void rethrowAsCoreException(Exception e) throws CoreException {
		throw new CoreException(LogHelper.createExceptionStatus(e));
	}
	
	public static void throwCoreException(String pluginId, String message, int code) throws CoreException 
    {
        IStatus status= new Status(IStatus.ERROR, pluginId, code, message, null);
        throw new CoreException(status);
    }

	public static void throwCoreException(String pluginId, String message) throws CoreException{
    	throwCoreException(pluginId, message, IStatus.ERROR);
    }
}
