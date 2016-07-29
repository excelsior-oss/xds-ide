package com.excelsior.xds.core.utils.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

public final class JsonUtils {
	public static List<String> toStringList(JSONArray arr) throws JSONException {
		List<String> l = new ArrayList<String>(arr != null? arr.length() : 0);
		if (arr != null) {
			for (int i = 0; i < arr.length(); i++) {
				l.add(arr.getString(i));
			}
		}
		return l;
	}
	
	private JsonUtils(){
	}
}
