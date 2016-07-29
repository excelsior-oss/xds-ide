package com.excelsior.xds.ui.commons.perspectives;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;

public final class PerspectiveUtils {
	public static void promptAndOpenPerspective(IWorkbench workbench, String perspectiveId, String title, String message) throws WorkbenchException {
        IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();

        if (!perspectiveId.equals(activeWindow.getActivePage().getPerspective().getId())) {
            if (MessageDialog.openConfirm(activeWindow.getShell(), title, message)) {
                workbench.showPerspective(perspectiveId, activeWindow);
            }
        }
    }
	
	public static void showPerspective(String perspectiveId){
		showPerspective(perspectiveId, WorkbenchUtils.getActiveWorkbenchWindow());
	}
    
    public static void showPerspective(String perspectiveId, IWorkbenchWindow window) {
    	Display.getDefault().asyncExec(() -> {
    		IWorkbench workbench = PlatformUI.getWorkbench();
			try {
				workbench.showPerspective(perspectiveId, window);
			} catch (WorkbenchException e) {
				LogHelper.logError(e);
			}
    	});
    }
    
    /**
	 * Static methods only
	 */
	private PerspectiveUtils(){
	}
}