package com.excelsior.xds.core.utils.collections;

/**
 * Helper interface, to perform search using the 
 * com.excelsior.xds.core.utils.collections.CollectionsUtils.binarySearch
 * @author lsa80
 * @see ISearchDirector#direct(Object)
 */
public interface ISearchDirector<T> {
	/**
	 * returns -1, 0 or 1 if the search should look left, succeeded (i.e. element is found), or right.
	 * @return 
	 */
	int direct(T key);
}
