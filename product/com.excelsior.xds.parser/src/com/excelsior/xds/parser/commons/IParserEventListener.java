package com.excelsior.xds.parser.commons;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.text.TextPosition;

/**
 * Interface of an object listening to parser events such as errors, warnings etc.
 * 
 * @author lsa80, lion
 */
public interface IParserEventListener extends ITodoTaskListener
{
    /**
     * Notifies that parsing of some file are finished.  
     * 
     * @param file, a source file which has been parsed.
     * @throws CoreException 
     */
    public void endFileParsing(IFileStore sourceFile); 
    
    
    /**
     * Notifies that the warning in the given position has been discovered.
     * 
     * @param file, a source file in which the warning takes place or <tt>null</tt>
     * @param position position of the warning
     * @param length length of invalid area 
     * @param message text of the warning message
     * @param arguments arguments of the given warning message
     * @throws CoreException 
     */
    public void warning( IFileStore sourceFile, CharSequence chars
                       , TextPosition position, int length
                       , String message, Object... arguments );
    
    /**
     * Notifies that the error in the given position has been discovered.
     * 
     * @param file, a source file in which the error takes place or <tt>null</tt>
     * @param position position of the error
     * @param length length of invalid area 
     * @param message text of the error message
     * @param arguments arguments of the given error message
     * @throws CoreException 
     */
    public void error( IFileStore sourceFile, CharSequence chars
                     , TextPosition position, int length
                     , String message, Object... arguments );
    
    
    /**
     * Log the specified internal error of parser and scanner.
     * 
     * @param file, a source file in which the internal error takes place or <tt>null</tt>
     * @param message, a human-readable message, localized to the current locale.
     * @param exception, a low-level exception, or <code>null</code> if not applicable.
     */
    public void logInternalError(IFileStore sourceFile, String message, Throwable exception);

    /**
     * Log the specified internal error of parser and scanner.
     * 
     * @param file, a source file in which the internal error takes place or <tt>null</tt>
     * @param message, a human-readable message, localized to the current locale.
     */
    public void logInternalError(IFileStore sourceFile, String message);

    /**
     * Log the specified internal error of parser and scanner.
     * 
     * @param file, a source file in which the internal error takes place or <tt>null</tt>
     * @param exception, a low-level exception.
     */
    public void logInternalError(IFileStore sourceFile, Throwable exception);
    
}
