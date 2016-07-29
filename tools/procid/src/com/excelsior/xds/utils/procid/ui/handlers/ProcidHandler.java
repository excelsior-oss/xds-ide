package com.excelsior.xds.utils.procid.ui.handlers;

import java.lang.management.ManagementFactory;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.excelsior.xds.utils.procid.ui.dialogs.ProcidDialog;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ProcidHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public ProcidHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		ProcidDialog d = new ProcidDialog(window.getShell(), getProcessId());
		d.open();
		
		return null;
	}
	
	static String getProcessId() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		int idx = name.indexOf('@');
		if (idx > -1) {
			return name.substring(0, idx);
		}
		else {
			return name;
		}
	}
}
