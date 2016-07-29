package com.excelsior.xds.core.utils.collections;

import java.util.ArrayList;

public final class XListUtils {
	private XListUtils(){
		super();
	}
	public static <T> ArrayList<T> getElementsAsArrayList(Iterable<T> iterable) {
		ArrayList<T> list = new ArrayList<T>();
		for (T e : iterable) {
			list.add(e);
		}
		return list;
	}
}
