package com.excelsior.xds.parser.commons;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.core.todotask.TodoTask;

/**
 * A parser event listener to ignore all parser warnings, errors etc.
 */
public class NullParseEventReporter implements IParserEventListener
{
    /**
     * Thread-safe singleton support.
     */
    public static NullParseEventReporter getInstance() {
        return NullParseWarningReporterHolder.INSTANCE;
    }

    /**
     * Returns a string representation of the token position.
     *  
     * @param position token position
     * @return a string representation of the token position.
     */
    public static String positionToString(TextPosition position) {
        if (position == null) {
            return "0:0";
        }
        else {
            return "" + position.getLine() + ":" + position.getColumn();
        }
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
    public void warning(IFileStore file, CharSequence chars, TextPosition position, int length, String message, Object... arguments) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(IFileStore file, CharSequence chars, TextPosition position, int lenght, String message, Object... arguments) {
        // do nothing
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void logInternalError(IFileStore file, String message, Throwable exception) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logInternalError(IFileStore file, String message) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logInternalError(IFileStore file, Throwable exception) {
        // do nothing
    }

    private static class NullParseWarningReporterHolder {
        static NullParseEventReporter INSTANCE = new NullParseEventReporter();
    }
}
