package com.excelsior.xds.core.console;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class XdsConsoleLink {
    
    private IMarker marker; // null => open Problems view

    /**
     * Link from XdsConsole to open Problems view 
     * @return
     */
    public static XdsConsoleLink mkLinkToProblemsView() {
        return new XdsConsoleLink();
    }

    /**
     * Link from XdsConsole to some marker (with error in source file etc.) 
     * @param marker
     */
    public XdsConsoleLink(IMarker marker) {
        this.marker = marker;
    }
    
    public void gotoLink(boolean activateEditor) {
        try {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (marker != null) {
                if (marker.exists()) {
                    IDE.openEditor(page, marker, activateEditor);
                }
            } else {
            	// LSA80 : почему хардкод? Ќайди и замени на константу одного из core-ных классов эклипса, 
            	// такой в природе точно существует, не помню как называетс€.
                page.showView("org.eclipse.ui.views.ProblemView", null, IWorkbenchPage.VIEW_ACTIVATE); 
            }
        }
        catch (Exception e) { // hz (NPE, PartInitException...)
            e.printStackTrace();
        }
    }
    
    public boolean isEditorLink() {
        return marker != null;
    }
    
    private XdsConsoleLink() {
    }

}
