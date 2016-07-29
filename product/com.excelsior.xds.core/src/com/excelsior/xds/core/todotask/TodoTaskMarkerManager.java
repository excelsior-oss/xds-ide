package com.excelsior.xds.core.todotask;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.text.TextPosition;

/**
 * This class manages creation and registration of 'to-do' tasks' markers.
 *   
 * @noextend This class is not intended to be subclassed by clients.
 */
public final class TodoTaskMarkerManager
{
    private TodoTaskMarkerManager() {
	}

	/**
     * Creates and returns the "to do" task marker with the specified position 
     * on the given resource. 
     * 
     * @param project a project to operate on, must not be <tt>null</tt>
     * @param file a source file in which the task takes place or <tt>null</tt>
     * @param task instance of discovered task.
     * @param message text of the task
     * @param position a start position of the task
     * @param endOffset a end offset of the task
     * 
     * @return the handle of the new marker. 
     */
    public static IMarker createMarker( IResource resource
                                      , TextPosition position, int endOffset 
                                      , TodoTask task, String message
                                      ) throws CoreException
    {
        IMarker marker = resource.createMarker(IMarker.TASK);
        marker.setAttribute(IMarker.MESSAGE,     createMessage(task, message));
        marker.setAttribute(IMarker.PRIORITY,    task.priority.markerPriority);
        marker.setAttribute(IMarker.LINE_NUMBER, position.getLine());
        marker.setAttribute(IMarker.CHAR_START,  position.getOffset());
        marker.setAttribute(IMarker.CHAR_END,    endOffset);
        marker.setAttribute(IMarker.USER_EDITABLE, Boolean.FALSE);
        
        return marker;
    }

    /**
     * Creates and returns the "to do" task marker with the specified position 
     * on the given file. All previous markers with position up to the given will
     * be removed. 
     * 
     * @param project a project to operate on, must not be <tt>null</tt>
     * @param file a source file in which the task takes place or <tt>null</tt>
     * @param task instance of discovered task.
     * @param message text of the task
     * @param position a start position of the task
     * @param endOffset a end offset of the task
     * @param oldMarkers markers from the previous parsing to be reused or removed.
     * 
     * @return the handle of the new marker, or <tt>null</tt> if marker wasn't created. 
     */
    public static IMarker updateMarkers( IResource resource
                                       , TextPosition position, int endOffset 
                                       , TodoTask task, String message
                                       , IMarker[] oldMarkers
                                       ) throws CoreException 
    {
        int startOffs = position.getOffset();
        IMarker newMarker = null;
        String newMessage = createMessage(task, message);
        
        // Remove or reuse 'to-do' markers from previous parsing
        for (int i = 0; i < oldMarkers.length; ++i) {
            IMarker marker = oldMarkers[i];
            if (marker != null) {
                int markerOffs = marker.getAttribute(IMarker.CHAR_START, 0);
                if (markerOffs <= startOffs) {
                	// to remove all editable markers
                	boolean isMarkerNotEditable = Boolean.FALSE.equals(marker.getAttribute(IMarker.USER_EDITABLE));
                    boolean isMarkerEqual = (markerOffs == startOffs)
                                         && newMessage.equals(marker.getAttribute(IMarker.MESSAGE, null))
                                         && isMarkerNotEditable;
                    if (isMarkerEqual) {
                        newMarker = marker;  // reuse the marker from previous parsing
                    } else {
                        marker.delete();
                    }
                    oldMarkers[i] = null;
                }
            }
        }
        
        if (newMarker == null) {          
            newMarker = createMarker(resource, position, endOffset, task, message);
        }

        return newMarker;
    }

    /**
     * Returns all markers of the specified type on this resource, and on its 
     * children. Returns an empty array if there are no matching markers.
     * 
     * @param resource a resource to operate on, must not be <tt>null</tt>
     * @throws CoreException 
     */    
    public static IMarker[] findMarkers(IResource resource) throws CoreException 
    {
        return resource.findMarkers(IMarker.TASK, true, IResource.DEPTH_INFINITE);
    }
    
    private static String createMessage(TodoTask task, String message) {
        return task.tag + " " + message;     //$NON-NLS-1$
    }
}
