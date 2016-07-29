package com.excelsior.xds.core.process;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;

public class ProcessInputStream extends InputStream {
	private volatile ArrayDeque<byte[]> qData;
	private volatile boolean eof;
	private volatile byte buf[];
	private volatile int pos;
	private Object semaphore;
	
	public ProcessInputStream() {
		qData = new ArrayDeque<byte[]>();
		eof = false;
		buf = null;
		pos = 0;
		semaphore = new Object();
	}

	@Override
	public int read() throws IOException {
		if (eof)
			return -1;
		if (buf== null || pos >= buf.length) {
			synchronized (qData) {
				if (qData.isEmpty()) {
					buf = null;
				} else {
					buf= qData.remove();
				}
			}
			if (buf == null) {
				try {
					synchronized (semaphore) {
						semaphore.wait();
					}
					synchronized (qData) {
						buf = qData.remove();
					}
				} catch (Exception e) {
					buf = null;
				}
			}
			if (buf == null || buf.length == 0) {
				eof = true;
				return -1;
			}
			pos = 0;
		}
		return buf[pos++] & 0xff;
	}
	
	public void pushData(byte[] buffer, int offset, int len) {
		if (len>0) {
			byte arr[] = new byte[len];
			System.arraycopy(buffer, offset, arr, 0, len);
			synchronized(qData) {
				qData.add(arr);
			}
			synchronized(semaphore) {
				semaphore.notifyAll();
			}
		}
	}

	public void setEOF() {
		synchronized(qData) {
			qData.add(new byte[0]); // 0-length => EOF
		}
		synchronized(semaphore) {
			semaphore.notifyAll();
		}
	}
}
