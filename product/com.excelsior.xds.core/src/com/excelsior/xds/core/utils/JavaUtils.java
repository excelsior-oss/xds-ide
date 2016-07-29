package com.excelsior.xds.core.utils;

import java.math.BigInteger;

public final class JavaUtils
{
    /**
     * Casts {@link entity} to {@link targetClass} or return null, if it is impossible
     * 
     * @param targetClass
     * @param entity
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T as(Class<T> targetClass, Object entity) {
        T result = null;
        if ((entity != null) && targetClass.isAssignableFrom(entity.getClass())) 
        {
            result = (T)entity;
        }
        return result;
    }
    
    /**
     * Tests for {@code o.getClass()} to be one of {@code possibleClasses} in the sense of {@code java.lang.Class.isAssignableFrom}
     */
    public static boolean isOneOf(Object o, Class<?>... possibleClasses) {
    	if (o == null) {
    		return false;
    	}
    	for (Class<?> clazz : possibleClasses) {
			if (clazz.isAssignableFrom(o.getClass())) return true;
		}
		return false;
    }

    public static boolean areFlagsSet(int bitset, int mask) {
    	return (bitset & mask) != 0;
    }
    
    /**
     * Allows to get stack depth of the current call.
     * 
     * NOTE : this is very slow. Please use with caution. 
     * @return
     */
    public static int getCurrentCallStackDepth() {
    	return new Exception().getStackTrace().length;
    }
    
    public static boolean isCalledFrom(String methodName) {
    	StackTraceElement[] stackTrace = new Exception().getStackTrace();
    	for (int i = stackTrace.length - 1; i > -1; i--) {
			StackTraceElement stackTraceElement = stackTrace[i];
			String methodFullName = getMethodFullName(stackTraceElement);
			if (methodFullName.startsWith(methodName)) {
				return true;
			}
		}
    	return false;
    }

	protected static String getMethodFullName(
			StackTraceElement stackTraceElement) {
		return stackTraceElement.getClassName()+ "." + stackTraceElement.getMethodName();
	}
	
	public static String getAncestorCallerName(int levelUp) {
		return getAncestorCallerName(levelUp + 1, false);
	}
    
    public static String getAncestorCallerName(int levelUp, boolean isAddPosition) {
    	StackTraceElement[] stackTrace = new Exception().getStackTrace();
		return getMethodFullName(stackTrace[levelUp]) + (isAddPosition?  " " + stackTrace[levelUp].getLineNumber() : "");
    }
    
    public static <T extends Comparable<? super T>> int compare(T c1, T c2) {
    	return compare(c1, c2, false);
    }
    
    public static <T extends Comparable<? super T>> int compare(T c1, T c2, boolean isNullGreater) {
    	if (c1 == c2) {
    		return 0;
    	} else if (c1 == null) {
    		return (isNullGreater ? 1 : -1);
    	} else if (c2 == null) {
    		return (isNullGreater ? -1 : 1);
    	}
    	return c1.compareTo(c2);
    }
    
    /**
     * Returns either an n converted to int, or 0 if n equals {@code null} 
     * @param n
     * @param defaultValue
     * @return
     */
    public static int toInt(Integer n) {
    	return n != null ? n : 0;
    }
    
    /**
     * Returns either an n converted to int, or default value if n equals {@code null} 
     * @param n
     * @param defaultValue
     * @return
     */
    public static int toInt(Integer n, int defaultValue) {
    	return n != null ? n : defaultValue;
    }

    /**
     * Parses 0x representation of the {@link BigInteger} from string
     * @param val string to parse
     * @return parsed
     */
    public static BigInteger toBigIntegerFromHex(String val) {
    	BigInteger n = new BigInteger(val.replace("0x", ""), 16);
    	return n;
    }
    
    /**
     * Cannot instantiate this class, static methods only 
     */
    private JavaUtils(){
    }
}
