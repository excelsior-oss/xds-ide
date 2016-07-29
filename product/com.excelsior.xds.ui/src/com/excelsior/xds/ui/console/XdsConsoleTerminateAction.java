package com.excelsior.xds.ui.console;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchWindow;

import com.excelsior.xds.core.console.IXdsConsoleTerminateCallback;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.internal.nls.Messages;

public class XdsConsoleTerminateAction extends Action {
	
	private XdsConsole fConsole;

	/**
	 * Creates a terminate action for the console 
	 */
	public XdsConsoleTerminateAction(IWorkbenchWindow window, XdsConsole console) {
		super(Messages.XdsConsoleTerminateAction_Terminate); 
		fConsole = console;
		setToolTipText(Messages.XdsConsoleTerminateAction_Terminate); 
		setImageDescriptor(ImageDescriptor.createFromImage(ImageUtils.getImage(ImageUtils.BTN_TERMINATE)));
		setDisabledImageDescriptor(ImageDescriptor.createFromImage(ImageUtils.getImage(ImageUtils.BTN_TERMINATE_DISABLED)));
		setEnabled(false);
		console.setXdsConsoleTerminateAction((Action)this);
	}

	@Override
	public void run() {
		if (fConsole != null) {
			IXdsConsoleTerminateCallback terminateCallback = fConsole.getTerminateCallback();
			if (terminateCallback != null) {
				terminateCallback.terminate();
			}
		}
	}

    public void dispose() {
	    fConsole = null;
	}

}
