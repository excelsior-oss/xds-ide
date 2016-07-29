package com.excelsior.xds.core.utils.collections;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * XMapUtils in order not to confuse with org.apache.commons.collections.MapUtils
 * @author lsa80
 */
public final class XMapUtils {
    private XMapUtils(){
    	super();
    }
	
	public static <T,U> Map<T,U> newHashMap(T key, U value) {
		Map<T,U> m = new HashMap<T, U>();
		
		m.put(key, value);
		
		return m;
	}
	
	public static <T,U> Map<T,U> newLinkedHashMap(T key, U value) {
		Map<T,U> m = new LinkedHashMap<T, U>();
		
		m.put(key, value);
		
		return m;
	}
	
	public static <T,U> Map<T,U> newHashMap(T[] keys, U[] values) {
		Map<T,U> m = new HashMap<T, U>();
		
		for (int i = 0; i < keys.length; i++) {
			m.put(keys[i], values[i]);
		}
		
		return m;
	}
}