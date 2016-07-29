package com.excelsior.xds.ui.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

import com.excelsior.xds.core.console.IXdsConsole;
import com.excelsior.xds.core.console.IXdsConsoleFactory;

public final class XdsConsoleManager implements IXdsConsoleFactory{
    public XdsConsoleManager() {
	}
    
	public IXdsConsole getXdsConsole(String id) {
        XdsConsole console = null;

        IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager()
                .getConsoles();
        for (IConsole con : consoles) {
            if (con.getName().equals(id)) {
                console = (XdsConsole) con;
                break;
            }
        }

        if (console == null) {
            console = new XdsConsole(id);
            ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
        }
        return console;
    }
}
