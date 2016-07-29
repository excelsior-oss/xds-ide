package com.excelsior.xds.parser.commons;

import java.io.PrintStream;
import java.text.MessageFormat;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.core.todotask.TodoTask;


public class ConsoleParseEventReporter implements IParserEventListener 
{
    protected int errorCount;
    protected int warningCount;
    protected int fatalErrorCount;

    public ConsoleParseEventReporter() {
        errorCount   = 0;
        warningCount = 0;
        fatalErrorCount = 0;
    }
    
    public int getErrorCount() {
        return errorCount;
    }
    
    public int getWarningCount() {
        return warningCount;
    }

    public int getFatalErrorCount() {
        return fatalErrorCount;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void endFileParsing(IFileStore file) {
        if (file != null) {
        	printAbsolutePath(System.out, file);
        }
        System.out.println( "-- Total errors: " + errorCount + " warnings: " + warningCount); 
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void taskTag( IFileStore file, TextPosition position, int endOffset
                       , TodoTask task, String message )
    {    
        if (file != null) {
        	printAbsolutePath(System.out, file);
        }
        System.out.println( "-- [" + NullParseEventReporter.positionToString(position) + ".." + endOffset   //$NON-NLS-1$ //$NON-NLS-2$
                          +  " Todo Task] " + message
                          );
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void warning(IFileStore file,  CharSequence chars, TextPosition position, int length, String message, Object... arguments) {
        warningCount++;
        if (file != null) {
        	printAbsolutePath(System.out, file); 
        }
        System.out.println( "-- [" + NullParseEventReporter.positionToString(position) + " Warning] " 
                          + MessageFormat.format(message, arguments)
                          );
    }
    
    private static void printAbsolutePath(PrintStream stream, IFileStore file) {
    	if (file != null) {
    		try {
    			stream.println( "-- [" + ResourceUtils.getAbsolutePath(file) + "]");
			} catch (CoreException e) {
				e.printStackTrace();
			} 
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(IFileStore file,  CharSequence chars, TextPosition position, int length, String message, Object... arguments) {
        errorCount++;
        if (file != null) {
        	printAbsolutePath(System.err, file);
        }
        System.err.println( "-- [" + NullParseEventReporter.positionToString(position)  + " Error] " 
                          + MessageFormat.format(message, arguments)
                          );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logInternalError(IFileStore file, String message, Throwable exception) {
        fatalErrorCount++;
        if (file != null) {
        	printAbsolutePath(System.out, file);
        }
        System.out.println( "-- [ Fatal Error] " + message); 
        if (exception != null) {
            exception.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logInternalError(IFileStore file, String message) {
        logInternalError(file, message, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logInternalError(IFileStore file, Throwable exception) {
        logInternalError(file, exception.getMessage(), exception);
    }
	
}
