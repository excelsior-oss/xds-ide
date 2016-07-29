package com.excelsior.xds.launching.commons.delegate;

import java.io.IOException;

import org.apache.commons.lang.SystemUtils;

import com.excelsior.xds.core.console.ColorStreamType;
import com.excelsior.xds.core.console.IXdsConsole;
import com.excelsior.xds.core.log.LogHelper;

public abstract class AbstractProcessListener extends ProcessListenerAdapter {
	private final IXdsConsole console;
	private String consoleEncoding;

	public AbstractProcessListener(IXdsConsole console, String consoleEncoding) {
		this.console = console;
		this.consoleEncoding = consoleEncoding;
	}
	
	protected void println(ColorStreamType cst, String text) {
		print(cst, text);
		print(cst, SystemUtils.LINE_SEPARATOR);
	}
	
	protected void print(ColorStreamType cst, String text) {
		try {
			console.getConsoleStream(cst).write(text.getBytes(consoleEncoding));
		} catch (IOException e) {
			LogHelper.logError(e);
		}
	}
}
