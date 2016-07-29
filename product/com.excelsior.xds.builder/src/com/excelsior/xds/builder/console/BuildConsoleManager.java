package com.excelsior.xds.builder.console;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

import com.excelsior.xds.builder.internal.nls.Messages;
import com.excelsior.xds.builder.internal.services.ServiceHolder;
import com.excelsior.xds.core.console.ColorStreamType;
import com.excelsior.xds.core.console.IXdsConsole;
import com.excelsior.xds.core.console.IXdsConsoleFactory;
import com.excelsior.xds.core.console.XdsConsoleSettings;

/**
 * It seems that purpose of this class is to hold references to several consoles associated with building of the corresponding projects.
 * 
 * @author lsa80
 */
public final class BuildConsoleManager{
    private static IXdsConsoleFactory consoleFactory;
    private static Map<String, IXdsConsole> projectNameToXdsConsole = new HashMap<String, IXdsConsole>(); // IProject name -> consoleProxy
    
    public static IXdsConsole getConsole(IProject project){
        return instance().internalGetConsole(project);
    }
    
    public IXdsConsole getAndConfigureConsole(String id) {
		final IXdsConsole console = consoleFactory.getXdsConsole(id);
		 // default encoding required to print text with println(String) to ConsoleProxy.getInstance()
		console.setEncoding(null);
		if (XdsConsoleSettings.getShowOnBuild()) {
		    ConsolePlugin.getDefault().getConsoleManager().showConsoleView((IConsole)console);
		}
        if (XdsConsoleSettings.getClearBeforeBuild()) {
            console.clearConsole();
        }
		
		// Console.setBackground is ignored in console constructor (console has no view at that time) so set it here:
		Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                console.setBackground(ColorStreamType.BACKGROUND.getRgb());
            }
		});
		return console;
	}
	
	public static IXdsConsole reinitializeConsole(IXdsConsole console) {
		return instance().getAndConfigureConsole(console.getName());
	}
    
    private IXdsConsole internalGetConsole(IProject project){
        String projectName = getProjectName(project);
		IXdsConsole console = projectNameToXdsConsole.get(projectName);
        if (console == null) {
            console = getAndConfigureConsole(createConsoleName(project));
            projectNameToXdsConsole.put(projectName, console);
        }
        return console;
    }
    
    private BuildConsoleManager() {
        if (consoleFactory == null) {
        	consoleFactory = ServiceHolder.getInstance().getConsoleFactory();
        }
    }
    
    private static BuildConsoleManager instance() {
    	return ConsoleProxyHolder.INSTANCE;
    }
    
    private String getProjectName(IProject p) {
    	return p == null ? "" : p.getName();
    }
    
    private static String createConsoleName(IProject p) {
    	String title = Messages.XdsSourceBuilder_ConsoleName;
    	if (p != null) {
            title = String.format(Messages.XdsSourceBuilder_ConsoleOfProject, Messages.XdsSourceBuilder_ConsoleName, p.getName());
        }
    	return title;
    }
    
	private static final class ConsoleProxyHolder {
		final static BuildConsoleManager INSTANCE = new BuildConsoleManager();
	}
}