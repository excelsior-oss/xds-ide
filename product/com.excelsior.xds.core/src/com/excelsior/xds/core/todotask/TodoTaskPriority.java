package com.excelsior.xds.core.todotask;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;

/**
 * Possible priority of "to do" tasks.  
 */
public enum TodoTaskPriority
{
    Hight ("high",   IMarker.PRIORITY_HIGH),
    Normal("normal", IMarker.PRIORITY_NORMAL) ,
    Low   ("low",    IMarker.PRIORITY_LOW);

    private final String id;
    public  final int markerPriority;
    
    private TodoTaskPriority(String id, int markerPriority) {
        this.id = id;
        this.markerPriority = markerPriority; 
    }
    
    public static final Map<String, TodoTaskPriority> ID_TO_PRIORITY = new HashMap<String, TodoTaskPriority>();
    static {
        for (TodoTaskPriority priority: TodoTaskPriority.values()) {
            ID_TO_PRIORITY.put(priority.id, priority);
        }
    };
    
}
