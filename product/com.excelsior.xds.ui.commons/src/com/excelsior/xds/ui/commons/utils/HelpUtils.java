package com.excelsior.xds.ui.commons.utils;

import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

/**
 * Common utilities that is used to access the workbench help system.
 */
public abstract class HelpUtils {

    /**
     * Sets the given help context id on the given control.
     * 
     * @param control  the control on which to register the context id
     * @param contextId  the context id to use when F1 help is invoked
     */
    public static void setHelp(Control control, String contextId) {
        PlatformUI.getWorkbench().getHelpSystem().setHelp(control, contextId);
    }
    
}
