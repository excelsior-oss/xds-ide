package com.excelsior.xds.xbookmarks;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.xbookmarks.commands.XBookmarksState;

public class XBookmarksUtils {
    
    /**
     * Returns the root resource of the workspace scope.
     * 
     * @return the workspace root
     */
    public static IResource getWorkspaceScope() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }
    
    
    /**
     * Logs the specified error and opens the "Error Log" view (if possible).
     * 
     * @param exception, a low-level exception.
     */
     public static void logError (Throwable exception) {
         IStatus status = new Status( IStatus.ERROR, XBookmarksPlugin.PLUGIN_ID, IStatus.ERROR
                                    , "Internal Error in xBookMarks plugin: " + exception.getMessage() //$NON-NLS-1$
                                    , exception ); 
         XBookmarksPlugin.getDefault().getLog().log(status);

         // switch the "Error Log" view
         try {
             XBookmarksPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
                     .getActivePage()
                     .showView("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
         } catch (Exception e) {
             // ignore any exception and live with the log view not
             // automatically shown: the problem is logged at least
             // and an appropriate message shown elsewhere.
         }
     }
    
     
     /**
      * Fetches the marker annotation model for the currently edited document.
      * Note, that I would prefer to declare this as returning e.g.
      * IAnnotationModel, but the method to call somewhere else is
      * <code>updateMarkers(IDocument document)</code> which is not specified by
      * IAnnotationModel (the only interface implemented by
      * AbstractMarkerAnnotationModel).
      * 
      * @return the marker annotation model or <code>null</code>
      */
     public static AbstractMarkerAnnotationModel getMarkerAnnotationModel() {
         IDocumentProvider provider = WorkbenchUtils.getActiveDocumentProvider();
         IDocument document = WorkbenchUtils.getActiveDocument();
         if ((provider != null) && (document != null)) {
             IAnnotationModel model = provider.getAnnotationModel(document);
             if (model instanceof AbstractMarkerAnnotationModel) {
                 return (AbstractMarkerAnnotationModel) model;
             }
         }
         return null;
     }
     
    
     /**
      * Retrieves all existing xBookmarks from the given resource or an
      * empty ArrayList.
      * 
      * @param resource - the scope resource to find xBookmarks in
      * @number - xBookmark number
      * @return - ArrayList
      */
     public static ArrayList<IMarker> fetchBookmarks(IResource resource, int number) {
         ArrayList<IMarker> res = new ArrayList<IMarker>();
         if (resource == null) {
             return res;
         }

         AbstractMarkerAnnotationModel annotationModel = getMarkerAnnotationModel();
         IDocument document = WorkbenchUtils.getActiveDocument();
         if (annotationModel != null && document != null) {
             try {
                 annotationModel.updateMarkers(document);
             } catch (CoreException e) {
                 XBookmarksUtils.logError(e);
                 // ignored by intention, we work with the markers not updated
             }
         }

         try {
             IMarker[] rawMarkers = resource.findMarkers(XBookmarksPlugin.BOOKMARK_MARKER_ID, true, IResource.DEPTH_INFINITE);
             for (int i = 0; i < rawMarkers.length; i++) {
                 IMarker marker = rawMarkers[i];
                 if (!marker.exists()) {
                     continue;
                 }
                 int markerNumber = marker.getAttribute(XBookmarksPlugin.BOOKMARK_NUMBER_ATTR, -1);
                 // If the marker doesn't have the number attribute,
                 // there is no point in keeping it.
                 if (markerNumber < 0) {
                     marker.delete();
                     continue;
                 }
                 if (markerNumber == number) {
                     res.add(marker);
                 }
             }
         } catch (CoreException e) {
             // just ignore, we have logged and switched to view the log
             XBookmarksUtils.logError(e);
         }
         return res;
     }

     
     /**
      * Retrieves one xBookmark from the given resource or null.
      * This method used when only one marker with the given number expected
      * in the given scope but if it is not single - return some of them
      * without error.
      * 
      * @param resource - the scope resource to find xBookmarks in
      * @number - xBookmark number
      * @return - IMarker or null
      */
     public static IMarker fetchBookmark(IResource resource, int number) {
         ArrayList<IMarker> al = fetchBookmarks(resource, number);
         return al.isEmpty() ? null : al.get(0); 
     }

     
     /**
      * Returns numbers of currently activated xBookmarks.
      * 
      * @return a set of the activated xBookmarks numbers.
      */
     public static HashSet<Integer> getActivatedBookmarkNumbers() {
         HashSet<Integer> res = new HashSet<Integer>();
         try {
             IMarker[] rawMarkers = getWorkspaceScope().findMarkers(XBookmarksPlugin.BOOKMARK_MARKER_ID, true, IResource.DEPTH_INFINITE);
             for (int i = 0; i < rawMarkers.length; i++) {
                 IMarker marker = rawMarkers[i];
                 if (marker.exists()) {
                     int markerNumber = marker.getAttribute(XBookmarksPlugin.BOOKMARK_NUMBER_ATTR, -1);
                     if (markerNumber >= 0) {
                         res.add(markerNumber);
                     }
                 }
             }
         } catch (CoreException e) {}
         return res;
     }

     
     public static void removeBookmarksFrom (final IResource res) {
         Display.getDefault().asyncExec(new Runnable() {
             public void run() {
                 IResource scope = getWorkspaceScope();
                 if (scope == null) {
                     return;
                 }

                 int removedMarkerCount = 0;
                 for (int markerNumber = 0; markerNumber <= 9; ++markerNumber) {
                     IMarker marker = fetchBookmark(scope, markerNumber);
                     if (marker != null) {
                         if (res.equals(marker.getResource())) {
                             try{
                                 marker.delete();
                                 removedMarkerCount++;
                             }
                             catch (CoreException e) {
                             }
                         }
                     }
                 }
                 XBookmarksState xBbookmarkStateService = (XBookmarksState)WorkbenchUtils.getSourceProvider(XBookmarksState.EXIST_STATE);
                 if (xBbookmarkStateService != null) {
                	 xBbookmarkStateService.fireBookmarkRemoved(removedMarkerCount);
                 }
             }
         });
     }
     
}
