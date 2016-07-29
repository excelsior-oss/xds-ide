package com.excelsior.xds.core.todotask;

public class TodoTask
{
    public final String tag;
    public final TodoTaskPriority priority;

    TodoTask(String tag, TodoTaskPriority priority) {
        this.tag = tag;
        this.priority = priority;
    }
    
}
