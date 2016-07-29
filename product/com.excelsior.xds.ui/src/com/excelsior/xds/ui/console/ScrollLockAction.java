package com.excelsior.xds.ui.console;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.excelsior.xds.ui.images.ImageUtils;

/**
 * Toggles console auto-scroll
 */

public class ScrollLockAction extends Action {

    private XdsConsolePage fXdsConsolePage;

    public ScrollLockAction(XdsConsolePage xdsCPage) {
        super("&Scroll Lock"); 
        fXdsConsolePage = xdsCPage;
        setToolTipText("Scroll Lock"); 
        setDisabledImageDescriptor(ImageDescriptor.createFromImage(ImageUtils.getImage(ImageUtils.SCROLL_LOCK_DIS)));
        setImageDescriptor(ImageDescriptor.createFromImage(ImageUtils.getImage(ImageUtils.SCROLL_LOCK)));
        setChecked(false);
    }

    /**
     * @see org.eclipse.jface.action.IAction#run()
     */
    @Override
    public void run() {
        fXdsConsolePage.setAutoScroll(!isChecked());
    }

}
