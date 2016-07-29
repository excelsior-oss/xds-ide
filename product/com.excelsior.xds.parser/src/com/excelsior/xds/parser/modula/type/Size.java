package com.excelsior.xds.parser.modula.type;

import java.util.HashMap;
import java.util.Map;

/**
 * sizeof operation support 
 * @author lsa
 * TODO : take this information from the debugger
 */
public final class Size {
	private final Map<Type, Integer> type2Size = new HashMap<Type, Integer>(); 
	private final Map<String, Type> typeName2Type = new HashMap<String, Type>();
	
	public static Integer of(Type type) {
		return SizeHolder.instance.type2Size.get(type);
	}
	
	public static Integer of(String typeName) {
		Type type = SizeHolder.instance.typeName2Type.get(typeName.toLowerCase());
		return of(type);
	}
	
	private static class SizeHolder{
		static final Size instance = new Size();
	}
	
	private Size() {
		register(XdsStandardTypes.BOOLEAN, 1);
		register(XdsStandardTypes.CHAR, 1);
		register(XdsStandardTypes.INT8, 1);
		register(XdsStandardTypes.INT16, 2);
		register(XdsStandardTypes.INT32, 4);
		register(XdsStandardTypes.INT64, 8);
		register(XdsStandardTypes.CARD8, 1);
		register(XdsStandardTypes.CARD16, 2);
		register(XdsStandardTypes.CARD32, 4);
		register(XdsStandardTypes.CARD64, 8);
		
		register(XdsStandardTypes.REAL, 4);
		register(XdsStandardTypes.LONG_REAL, 8);
		register(XdsStandardTypes.LONGLONG_REAL, 16);
		
		register(XdsStandardTypes.COMPLEX, 8);
		register(XdsStandardTypes.LONG_COMPLEX, 16);
		
		register(XdsStandardTypes.SET8, 1);
		register(XdsStandardTypes.SET16, 2);
		register(XdsStandardTypes.SET32, 4);
		register(XdsStandardTypes.SET64, 8);
	}
	
	private void register(Type type, int size) {
		typeName2Type.put(type.getName(), type);
		type2Size.put(type, size);
	}
}
