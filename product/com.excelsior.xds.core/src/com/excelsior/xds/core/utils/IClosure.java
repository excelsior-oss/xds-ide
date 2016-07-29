package com.excelsior.xds.core.utils;

/**
 * Abstract operation to be performed on its parameter. 
 * See <a href="http://en.wikipedia.org/wiki/Closure_(computer_science)">http://en.wikipedia.org/wiki/Closure_(computer_science)</a>
 * 
 * @author lsa80
 * @param <T> - parameter of the operation
 */
public interface IClosure<T> extends IBaseClosure<T, RuntimeException> {
}