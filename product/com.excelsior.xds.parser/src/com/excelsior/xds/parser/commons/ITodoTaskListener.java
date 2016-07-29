package com.excelsior.xds.parser.commons;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.core.todotask.TodoTask;

/**
 * Interface of an object listening to "to do" task entries in a source code.
 *
 * @author lion
 */
public interface ITodoTaskListener
{
    /**
     * Notifies that the "to do" task in the given position has been discovered.
     * 
     * @param file a source file in which the task takes place or <tt>null</tt>
     * @param position a start position of the task
     * @param endOffset a end offset of the task
     * @param task instance of discovered task.
     * @param message text of the task
     */
    public void taskTag( IFileStore file, TextPosition position, int endOffset
                       , TodoTask task, String message );
    
}
