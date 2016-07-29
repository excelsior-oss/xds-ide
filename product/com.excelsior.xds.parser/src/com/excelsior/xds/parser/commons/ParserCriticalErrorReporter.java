package com.excelsior.xds.parser.commons;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.core.todotask.TodoTask;

/**
 * A default parser warning listener implementation.
 * This class cannot be used without OSGi running.
 */
public class ParserCriticalErrorReporter implements IParserEventListener 
{
    /**
     * Thread-safe singleton support.
     */
    public static ParserCriticalErrorReporter getInstance(){
        return InternalErrorParseReporterHolder.INSTANCE;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void endFileParsing(IFileStore file) {
        // do nothing
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void taskTag( IFileStore file, TextPosition position, int endOffset
                       , TodoTask task, String message )
    {    
        // do nothing
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void warning(IFileStore file,  CharSequence chars, TextPosition position, int length, String message, Object... arguments) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(IFileStore file,  CharSequence chars, TextPosition position, int length, String message, Object... arguments) {
        // do nothing
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void logInternalError(IFileStore file, String message, Throwable exception) {
        LogHelper.logError(getLogMessage(file, message), exception);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logInternalError(IFileStore file, String message) {
        LogHelper.logError(getLogMessage(file, message));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logInternalError(IFileStore file, Throwable exception) {
        LogHelper.logError(getLogMessage(file, ""));
    }

    
    private static String getLogMessage(IFileStore file, String message) {
        if (file != null) {
            try {
				message = "[" + ResourceUtils.getAbsolutePath(file) + "] " + message;
			} catch (CoreException e) {
				LogHelper.logError(e);
			}
        }
        return message;
    }
    
    private static class InternalErrorParseReporterHolder{
        static ParserCriticalErrorReporter INSTANCE = new ParserCriticalErrorReporter();
    }
}
