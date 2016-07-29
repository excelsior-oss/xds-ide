package com.excelsior.xds.core.utils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.excelsior.xds.core.log.LogHelper;

public final class StreamUtils {
	private static final int DEFAULT_WAIT_TIME = 200;
	private static final int DEFAULT_ATTEMPT_COUNT = 300;

	public static FileInputStream tryOpenInputPipe(String pipeName) throws FileNotFoundException {
		return tryOpenInputPipe(pipeName, DEFAULT_ATTEMPT_COUNT, DEFAULT_WAIT_TIME);
	}
	
	public static FileInputStream tryOpenInputPipe(String pipeName, int attemptCount, int waitTime) throws FileNotFoundException {
		for (int i = 0; i < attemptCount; i++) {
			try{
				return new FileInputStream(pipeName);
			}
			catch(FileNotFoundException e) {
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e1) {
					LogHelper.logError(e);
				}
			}
		}
		throw new FileNotFoundException("Pipe " + pipeName);
	}
	
	public static FileOutputStream tryOpenOutputPipe(String pipeName) throws FileNotFoundException {
		return tryOpenOutputPipe(pipeName, DEFAULT_ATTEMPT_COUNT, DEFAULT_WAIT_TIME);
	}
	
	public static FileOutputStream tryOpenOutputPipe(String pipeName, int attemptCount, int waitTime) throws FileNotFoundException {
		for (int i = 0; i < attemptCount; i++) {
			try{
				return new FileOutputStream(pipeName);
			}
			catch(FileNotFoundException e) {
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e1) {
					LogHelper.logError(e);
				}
			}
		}
		throw new FileNotFoundException();
	}
	
	public static InputStreamReader createInputStreamReader(File file, String charsetName) throws FileNotFoundException, UnsupportedEncodingException {
		Charset charset = charsetName == null? Charset.defaultCharset() : Charset.forName(charsetName); 
		return createInputStreamReader(file, charset);
	}
	
	public static InputStreamReader createInputStreamReader(File file, Charset charset) throws FileNotFoundException, UnsupportedEncodingException {
		FileInputStream is = new FileInputStream(file);
		return new InputStreamReader(is, charset);
	}
	
	private StreamUtils(){
	}
}