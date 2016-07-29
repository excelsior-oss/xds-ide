package com.excelsior.xds.core.process;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InputStreamObserver {
	private final int bufferSize;
	private final InputStream inputStream;
	private Thread thread;
	
	private final List<InputStreamListener> listeners = new CopyOnWriteArrayList<InputStreamListener>();
	
	public InputStreamObserver(InputStream inputStream)	{
		this(inputStream, 1024);
	}

	public InputStreamObserver(InputStream inputStream, int bufferSize) {
		this.inputStream = inputStream;
		this.bufferSize = bufferSize;
	}
	
	public void addListener(InputStreamListener listener) {
		listeners.add(listener);
	}
	
	public void start() {
		thread = new Thread(new Runnable(){
			@Override
			public void run() {
				byte[] buffer = new byte[bufferSize];
				try {
					while (true) {
						int len = inputStream.read(buffer);
						if (len == -1) {
							onStreamClosed();
							break;
						}
						onHasData(buffer, len);
					}
				} catch (IOException e) {
				}
			}
		}, "InputStreamObserver for the InputStream " + inputStream); //$NON-NLS-1$
		thread.start();
	}
	
	public void join() throws InterruptedException {
		thread.join();
	}
	
	private void onHasData(byte[] buffer, int len) {
		for (InputStreamListener listener : listeners) {
			listener.onHasData(buffer, len);
		}
	}
	
	private void onStreamClosed() {
		for (InputStreamListener listener : listeners) {
			listener.onEndOfStreamReached();
		}
	}
}
