package com.excelsior.xds.core.todotask;


/**
 * @noextend This class is not intended to be subclassed by clients.
 * 
 * <pre>
 * RECOGNIZED OPTIONS:
 * Define the Automatic Task Tags
 *    When the tag list is not empty, indexer will issue a task marker whenever it encounters
 *    one of the corresponding tags inside any comment in source code.
 *    Generated task messages will include the tag, and range until the next line separator or comment ending.
 *    Note that tasks messages are trimmed. If a tag is starting with a letter or digit, then it cannot be leaded by
 *    another letter or digit to be recognized ("fooToDo" will not be recognized as a task for tag "ToDo", but "foo#ToDo"
 *    will be detected for either tag "ToDo" or "#ToDo"). Respectively, a tag ending with a letter or digit cannot be followed
 *    by a letter or digit to be recognized ("ToDofoo" will not be recognized as a task for tag "ToDo", but "ToDo:foo" will
 *    be detected either for tag "ToDo" or "ToDo:").
 * 
 * Define the Automatic Task Priorities
 *    In parallel with the Automatic Task Tags, this list defines the priorities (high, normal or low)
 *    of the task markers issued by the translation.
 */
public class TodoTaskManager
{
    /**
     * Thread-safe singleton support.
     */
    public static TodoTaskManager getInstance(){
        return TodoTaskManagerHolder.INSTANCE;
    }
    
    /**
     * Returns a default "to do" tasks.
     * 
     * @return a default "to do" tasks
     */
    public TodoTask getDefaultTask() {
        return DEFAULT_TODO_TASK;
    }
    
    /**
     * Returns an array of all registered "to do" tasks.
     * 
     * @return an array containing the registered "to do" tasks.
     */
    public TodoTask[] getAllTasks() {
        return DEFAULT_TODO_TASKS;
    }

    /**
     * Returns the case-sensitivity of the "to do" task tags.
     * 
     * @return @true if "to do" task tags are case-sensitive, @false otherwise. 
     */
    public boolean isCaseSensitive() {
        return true;
    }
    
    
    private static final TodoTask DEFAULT_TODO_TASK = 
        new TodoTask("TODO",  TodoTaskPriority.Normal);    //$NON-NLS-1$ 
    
    private static final TodoTask DEFAULT_TODO_TASKS[] = new TodoTask[]{
        DEFAULT_TODO_TASK,
        new TodoTask("FIXME", TodoTaskPriority.Hight),     //$NON-NLS-1$
        new TodoTask("XXX",  TodoTaskPriority.Low)         //$NON-NLS-1$
    }
    ; 

    private static class TodoTaskManagerHolder {
        static TodoTaskManager INSTANCE = new TodoTaskManager();
    }
    
}
