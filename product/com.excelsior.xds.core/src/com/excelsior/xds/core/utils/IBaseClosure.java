package com.excelsior.xds.core.utils;

/**
 * 
 * Base class for the {@link IClosure}.<br><br>
 * Allows to specify throwed exception.
 * <br>
 * @author lsa80
 *
 * @param <T>
 * @param <E>
 */
public interface IBaseClosure<T,E extends Throwable> {
	void execute(T param) throws E;
}	
