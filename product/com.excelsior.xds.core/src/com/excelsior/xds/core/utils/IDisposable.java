package com.excelsior.xds.core.utils;

/**
 * This interface should be implemented by the classes holding references to native resources 
 * and dispose them on dispose call.
 * @author lsa80
 */
public interface IDisposable extends AutoCloseable {
	/**
	 * Dispose the managed resource(s). If dispose was already called, should do nothing.
	 */
	void dispose();
	
	default void close() throws Exception {
		dispose();
	}
}
