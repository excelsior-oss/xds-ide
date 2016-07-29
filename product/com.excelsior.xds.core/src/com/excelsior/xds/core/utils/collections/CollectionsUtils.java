package com.excelsior.xds.core.utils.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public final class CollectionsUtils {
	/**
	 * Binary search implementation
	 * @param list to search at
	 * @param director 'directs' the binary search where to look for the element
	 * @return element found or null
	 * @see ISearchDirector
	 */
	public static <T> T binarySearch(List<T> list, ISearchDirector<T> director) {
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		
		int idx = indexBinarySearch(list, director);
		if (idx > -1) {
			return list.get(idx);
		}
		else {
			return null;
		}
	}
	
	/**
	 * @param list to search at
	 * @param director director 'directs' the binary search where to look for the element
	 * @return index of the element, or value from which is possible to devise the insertion position
	 * @see {@link java.util.Collections#binarySearch}
	 */
	public static <T> int indexBinarySearch(List<T> list, ISearchDirector<T> director) {
		if (CollectionUtils.isEmpty(list)) {
			return -1;
		}
		
		int left = 0;
		int right = list.size() -1;
		
		while(left <= right)
		{
			int middle = (left + right) >>> 1;
			T key = list.get(middle);
			int direction = director.direct(key);
			if (direction > 0)
			{
				left = middle + 1;
			}
			else if (direction < 0)
			{
				right = middle - 1;
			}
			else{
				return middle;
			}
		}
		
		return -(left + 1);
	}
	
	/**
	 * Founds all values between (inclusive) the two boundaries
	 * @param list
	 * @param lowerBoundDirector - search director to found the lower bound
	 * @param upperBoundDirector - search director to found the upper bound
	 * @return values between
	 * @see {@link ISearchDirector}
	 */
	public static <T> List<T> between(List<T> list, ISearchDirector<T> lowerBoundDirector, ISearchDirector<T> upperBoundDirector) {
		int lowerBoundIdx = indexBinarySearch(list, lowerBoundDirector);
		int upperBoundIdx = indexBinarySearch(list, upperBoundDirector);
		if (lowerBoundIdx < 0 ) {
			lowerBoundIdx = -lowerBoundIdx - 1;
		}
		
		if (upperBoundIdx < 0 ) {
			upperBoundIdx = -upperBoundIdx - 1;
		}
		else {
			++upperBoundIdx;
		}
		
		return list.subList(lowerBoundIdx, upperBoundIdx);
	}
	
	public static <T> Set<T> newConcurentHashSet() {
		return Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>());
	}
	
	public static <T> Set<T> newConcurentHashSet(int initialCapacity) {
		return Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>(initialCapacity));
	}
	
	/**
	 * Create collection of the first elements of the pair using collections of pairs.
	 */
	public static <T,U> Collection<T> bindFirst(Collection<Pair<T,U>> pairs) {
		return Collections2.transform(pairs, new Function<Pair<T,U>, T>() {
			@Override
			public T apply(Pair<T, U> input) {
				return input.getFirst();
			}
		});
	}
	
	/**
	 * Create collection of the second elements of the pair using collections of pairs.
	 */
	public static <T,U> Collection<U> bindSecond(Collection<Pair<T,U>> pairs) {
		return Collections2.transform(pairs, new Function<Pair<T,U>, U>() {
			@Override
			public U apply(Pair<T, U> input) {
				return input.getSecond();
			}
		});
	}
	
	/**
	 * @param c
	 * @return size of {@link c} if c is not {@code null}, 0 otherwise
	 */
	public static int size(Collection<?> c) {
		return c != null? c.size() : 0;
	}
	
	public static <T> T last(List<T> list) {
        return list != null && !list.isEmpty() ? list.get(list.size() - 1) : null;
    }
	
	public static <T> List<T> unmodifiableArrayList(Collection<T> elements) {
		return Collections.unmodifiableList(new ArrayList<T>(elements));
	}
	
	public static <T> List<T> unmodifiableArrayList(Collection<? extends T> elements, Class<T> castClass) {
		return Collections.unmodifiableList(new ArrayList<T>(elements));
	}
	
	@SafeVarargs
	public static <T> T[] array(T...ts) {
		return ts;
	}
	
	 /**
     * Cannot instantiate this class, static methods only 
     */
	private CollectionsUtils(){
	}
}