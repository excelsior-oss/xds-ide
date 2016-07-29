package com.excelsior.xds.core.utils;


/**
 * Tests some condition on given object of class T
 * @author lion
 * @param <T> 
 */
public interface IPredicate<T> {
	/**
	 * @param object the object to evaluate, should not be changed
	 * @return
	 */
	boolean evaluate(T object);
}
