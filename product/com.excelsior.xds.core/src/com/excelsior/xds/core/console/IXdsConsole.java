
package com.excelsior.xds.core.console;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.swt.graphics.RGB;



public interface IXdsConsole {
    public String getName();
    public void clearConsole();
    /**
     * Opens the console in the UI
     */
    public void show();
	
    public OutputStream getConsoleStream(ColorStreamType cs);
    public InputStream  getInputStream();
    
    /**
     * Set action to perform when Terminate button will be clicked in console window 
     * @param al
     */
    public void setTerminateCallback(IXdsConsoleTerminateCallback terminateCallback);
    public void enableTerminateButton(boolean enable);

    /**
     * If console is opened prints the line
     * @param line - line to print
     * @param cs   - color stream to use
     * @param link - link to goto on click (may be null)
     */
    public void println(final String line, final ColorStreamType cs, final XdsConsoleLink link);


    /**
     * It is println(line, cs, null);
     */
    public void println(String line, ColorStreamType cs);

    /**
     * If console is opened prints the line (with the NORMAL color stream)
     * @param line - line to print
     */
    public void println(String line);

    /**
     * If console is opened prints the line
     * This line will be hidden when console filter is turned on
     *  
     * @param line - line to print
     * @param cs   - color stream to use
     */
    public void printlnFiltered(final String line, final ColorStreamType cs);

    public void setEncoding(String encoding);
    
    public void setFilteredLog(Boolean isFiltered);
	public void goPrevNextMessage(boolean b);
	public void setBackground(RGB rgb);
}
