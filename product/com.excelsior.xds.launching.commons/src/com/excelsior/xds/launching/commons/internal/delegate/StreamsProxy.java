package com.excelsior.xds.launching.commons.internal.delegate;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.IStreamsProxy2;

import com.excelsior.xds.core.utils.io.StreamUtils;

/**
 * Standard implementation of a streams proxy for IStreamsProxy.
 */

public class StreamsProxy implements IStreamsProxy, IStreamsProxy2 {
	/**
	 * The monitor for the output stream (connected to standard out of the process)
	 */
	private OutputStreamMonitor fOutputMonitor;
	/**
	 * The monitor for the error stream (connected to standard error of the process)
	 */
	private OutputStreamMonitor fErrorMonitor;
	/**
	 * The monitor for the input stream (connected to standard in of the process)
	 */
	private InputStreamMonitor fInputMonitor;
	/**
	 * Records the open/closed state of communications with
	 * the underlying streams.  Note: fClosed is initialized to
	 * <code>false</code> by default.
	 */
	private boolean fClosed;
	
	
	/**
	 * Creates a <code>StreamsProxy</code> on the streams
	 * of the given system process.
	 *
	 * @param process system process to create a streams proxy on
	 * @param encoding the process's encoding or <code>null</code> if default
	 */
	public StreamsProxy(Process process, String encoding) {
		this(process.getInputStream(), process.getErrorStream(), process.getOutputStream(), encoding);
	}
	
	public StreamsProxy(Process process, String stdoutPipeProxy, String stderrPipeProxy, String stdinPipeProxy, String encoding) throws FileNotFoundException {
		this(createInputStream(stdoutPipeProxy, process.getInputStream()),
				createInputStream(stderrPipeProxy, process.getErrorStream()),
				createOutputStream(stdinPipeProxy, process.getOutputStream()),
				encoding);
	}
	
	public StreamsProxy(InputStream stdoutProxy, InputStream stderrProxy, OutputStream stdinProxy, String encoding) {
		fOutputMonitor= new OutputStreamMonitor(stdoutProxy, encoding);
		fErrorMonitor= new OutputStreamMonitor(stderrProxy, encoding);
		fInputMonitor= new InputStreamMonitor(stdinProxy, encoding);
		fOutputMonitor.startMonitoring();
		fErrorMonitor.startMonitoring();
		fInputMonitor.startMonitoring();
	}
	
	private static InputStream createInputStream(String pipeName, InputStream defaultStream) throws FileNotFoundException {
		if (pipeName != null) {
			return StreamUtils.tryOpenInputPipe(pipeName);
		}
		else {
			return defaultStream;
		}
	}
	
	private static OutputStream createOutputStream(String pipeName, OutputStream defaultStream) throws FileNotFoundException {
		if (pipeName != null) {
			return StreamUtils.tryOpenOutputPipe(pipeName);
		}
		else {
			return defaultStream;
		}
	}

	/**
	 * Causes the proxy to close all
	 * communications between it and the
	 * underlying streams after all remaining data
	 * in the streams is read.
	 */
	public void close() {
		if (!isClosed(true)) {
			fOutputMonitor.close();
			fErrorMonitor.close();
			fInputMonitor.close();
		}
	}

	/**
	 * Returns whether the proxy is currently closed.  This method
	 * synchronizes access to the <code>fClosed</code> flag.
	 *
	 * @param setClosed If <code>true</code> this method will also set the
	 * <code>fClosed</code> flag to true.  Otherwise, the <code>fClosed</code>
	 * flag is not modified.
	 * @return Returns whether the stream proxy was already closed.
	 */
	private synchronized boolean isClosed(boolean setClosed) {
	    boolean closed = fClosed;
	    if (setClosed) {
	        fClosed = true;
	    }
	    return closed;
	}

	/**
	 * Causes the proxy to close all
	 * communications between it and the
	 * underlying streams immediately.
	 * Data remaining in the streams is lost.
	 */
	public void kill() {
	    synchronized (this) {
	        fClosed= true;
	    }
		fOutputMonitor.kill();
		fErrorMonitor.kill();
		fInputMonitor.close();
	}

	/**
	 * @see IStreamsProxy#getErrorStreamMonitor()
	 */
	@Override
	public OutputStreamMonitor getErrorStreamMonitor() {
		return fErrorMonitor;
	}

	/**
	 * @see IStreamsProxy#getOutputStreamMonitor()
	 */
	@Override
	public OutputStreamMonitor getOutputStreamMonitor() {
		return fOutputMonitor;
	}

	/**
	 * @see IStreamsProxy#write(String)
	 */
	@Override
	public void write(String input) throws IOException {
		if (!isClosed(false)) {
			fInputMonitor.write(input);
		} else {
			throw new IOException();
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStreamsProxy2#closeInputStream()
     */
    @Override
	public void closeInputStream() throws IOException {
        if (!isClosed(false)) {
            fInputMonitor.closeInputStream();
        } else {
            throw new IOException();
        }

    }

}
