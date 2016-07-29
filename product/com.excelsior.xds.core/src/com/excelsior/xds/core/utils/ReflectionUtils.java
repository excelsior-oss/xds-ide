package com.excelsior.xds.core.utils;


import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Utility class for breaking encapsulation via reflection.
 * <br/>
 * All methods of this class throw AssertionError if any exception is thrown from
 * reflection facilities.
 *
 * @author kit
 * @author alexm
 */
public class ReflectionUtils {

    /**
     * Returns <code>true</code> if specified class is annotated with annotation of
     * <code>annotationClass</code> type.
     */
    public static Annotation getAnnotation(Class<?> c, Class<? extends Annotation> annotationClass) {
        return c.getAnnotation(annotationClass);
    }
    
    /**
     * Returns <code>true</code> if specified class is annotated with annotation of
     * <code>annotationClass</code> type.
     */
    public static boolean annotatedWith(Class<?> c, Class<? extends Annotation> annotationClass) {
        return (c.getAnnotation(annotationClass) != null);
    }

    /**
     * Returns attribute value of given annotation with <code>attrName</code> attribute name.
     */
    public static Object getAnnotationAttribute(Annotation a, String attrName) {
        try {
            Method method = a.annotationType().getMethod(attrName);
            return method.invoke(a);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        } catch (SecurityException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns <code>true</code> if field with given name exists in given class.
     */
    public static boolean fieldExists(Class<?> clazz, String fname) {
        try {
            clazz.getDeclaredField(fname);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    /**
     * Sets field with specified value in spite of access permission.
     *
     * @throws AssertionError if field is final
     */
    public static void setField(Field f, Object this_, Object value) {
        try {
            f.setAccessible(true);
            f.set(this_, value);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Sets field with specified value in spite of access permission.
     *
     * @throws AssertionError if no such field in the class or field is final
     */
    public static void setField(Class<?> clazz, String fname, Object this_, Object value, boolean isSearchSuperclass) {
        try {
        	Field f = findField(clazz, fname, isSearchSuperclass);
            setField(f, this_, value);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Gets value of field in spite of access permission.
     */
    public static Object getField(Field f, Object this_) {
        try {
            f.setAccessible(true);
            return f.get(this_);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }
    
    public static Object getDeclaredField(String name, Object this_) {
		try {
			Field field = this_.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return getField(field, this_);
		} catch (SecurityException e) {
			throw new AssertionError(e);
		} catch (NoSuchFieldException e) {
			throw new AssertionError(e);
		}
    }
    
    /**
     * Gets value of field in spite of access permission.
     *
     * @throws AssertionError if no such field in the class
     */
    public static Object getField(Class<?> clazz, String fname, Object this_, boolean isSearchSuperclass) {
        try {
            Field f = findField(clazz, fname, isSearchSuperclass);
            return getField(f, this_);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

	private static Field findField(Class<?> clazz, String fname,
			boolean isSearchSuperclass) throws NoSuchFieldException,
			AssertionError {
		Field f = null;
		try{
			f = clazz.getDeclaredField(fname);
		}
		catch(NoSuchFieldException e) {
			
		}
		if (f == null && isSearchSuperclass) {
			do{
				clazz = clazz.getSuperclass();
				if (clazz == null) {
					throw new NoSuchFieldException("Class " + clazz + " has no such field: %s" + fname); //$NON-NLS-1$ //$NON-NLS-2$
				}
				try{
					f = clazz.getDeclaredField(fname);
				}
				catch(NoSuchFieldException e) {
					
				}
			}
			while(f == null);
		}
		return f;
	}
    
    /**
     * Creates new object using default constructor without access check.
     *
     * @return an instance of specified class.
     */
    public static <T> T createObject(Class<T> clazz) {
        try {
            Constructor<T> constr = clazz.getDeclaredConstructor();
            constr.setAccessible(true);
            return constr.newInstance();
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        } catch (SecurityException e) {
            throw new AssertionError(e);
        } catch (InstantiationException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Creates new object using constructor with given parameters, without access check.
     *
     * @return an instance of specified class.
     */
    public static <T> T createObject(Class<T> clazz, Class<?>[] paramTypes, Object... args) {
        try {
            Constructor<T> constr = clazz.getDeclaredConstructor(paramTypes);
            constr.setAccessible(true);
            return constr.newInstance(args);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        } catch (SecurityException e) {
            throw new AssertionError(e);
        } catch (InstantiationException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Returns return type of given method in given class.
     */
    public static Class<?> getMethodReturnType(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getDeclaredMethod(methodName);
            return method.getReturnType();
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        } catch (SecurityException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Looks for non-overloaded method declaring in given class.
     * 
     * @throws AssertionError if method is not found, or more than 1 methods found.
     */
    public static Method findMethod(Class<?> clazz, String methodName) {
        Method[] methods;
        try {
            methods = clazz.getDeclaredMethods();
        } catch (SecurityException e) {
            throw new AssertionError(e);
        }
        Method found = null;
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                if (found != null) {
                    throw new AssertionError("Multiple methods " + methodName + " found in " + clazz + ": " + found + ", " + m); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
                found = m;
            }
        }
        if (found == null) {
            throw new AssertionError("Method " + methodName + " is not found in " + clazz); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return found;
    }
    
    /**
     * Looks for method with given name and parameter types declared in given class.
     * 
     * @throws AssertionError if method is not found.
     */
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        } catch (SecurityException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Looks for methods annotated with {@code annot} in specified class.
     * 
     * @return list of annotated methods (empty if none found)
     */
    public static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annot) {
        try {
            final List<Method> foundMethods = new LinkedList<Method>();
            
            final Method[] allMethods = clazz.getDeclaredMethods();
            for (Method m : allMethods) {
                if (m.getAnnotation(annot) != null) {
                    foundMethods.add(m);
                }
            }
            
            return foundMethods;
            
        } catch (SecurityException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Invokes given method with given arguments.
     * For instance method the 1st argument is used as {@code this}. 
     * 
     * @throws AssertionError if error happened or method has thrown an exception.
     */
    public static Object invokeMethod(Method method, Object... args) {
        try {
            return invokeMethodHandleException(method, args);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Invokes given method with given arguments.
     * For instance method the 1st argument is used as {@code this}.
     * <br/>
     * Unlike {@link #invokeMethod(Method, Object...)} an exception thrown from target method is
     * not treated as error.  
     * 
     * @throws AssertionError if error happened.
     */
    public static Object invokeMethodHandleException(Method method, Object... args) throws InvocationTargetException {
        try {
            method.setAccessible(true);
            if (Modifier.isStatic(method.getModifiers())) {
                return method.invoke(null, args);
            } else {
                Object this_ = args[0];
                Object[] newArgs = new Object[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, args.length - 1);
                return method.invoke(this_, newArgs);
            }
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Return {@code true} if given class has instance fields.
     * Does not check superclass.
     */
    public static boolean hasInstanceFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            if (!Modifier.isStatic(f.getModifiers())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Looks for a class with given name.
     * 
     * @throws AssertionError if class is not found.
     */
    public static Class<?> findClass(String className) {
        try {
            return Class.forName(className, false, ReflectionUtils.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Looks for enclosed class with the given name.
     *
     * @param host enclosing class
     * @param className enclosed class name
     * @return found class or {@code null} if no such class.
     */
    public static Class<?> findDeclaredClass(Class<?> host, String className) {
        String fqName = host.getName() + "$" + className; //$NON-NLS-1$
        for (Class<?> c: host.getDeclaredClasses()) {
            if (c.getName().equals(fqName))
                return c;
        }
        return null;
    }
    
    /**
     * Walks object graph
     */
    public static void visitObjectTree(Object obj, IVisitFieldsFilter filter, IClosure<VisitedFieldInfo> visitor) {
    	VisitedFieldInfo fieldInfo = new VisitedFieldInfo(new ObjectTreePath(new StringBuilder(obj.getClass().getSimpleName().toString())), null, obj);
    	visitObjectTree(fieldInfo, filter, new HashSet<Object>(), visitor);
    }
    
    public static void visitObjectTree(VisitedFieldInfo fieldInfo, IVisitFieldsFilter filter, Set<Object> visitedObjects, IClosure<VisitedFieldInfo> visitor) {
    	if (fieldInfo.fieldValue == null) {
    		return;
    	}
    	visitor.execute(fieldInfo);
    	if (!visitedObjects.add(fieldInfo.fieldValue)) {
    		return;
    	}
    	
    	Object obj = fieldInfo.fieldValue;
    	Class<? extends Object> c = obj.getClass();
    	if (filter != null && (filter.reject(getPackage(obj)) || filter.reject(obj.getClass()))) {
    		return;
    	}
    	while(c != null) {
    		Field[] declaredFields = c.getDeclaredFields();
    		for (int i = 0; i < declaredFields.length; i++) {
    			Field declaredField = declaredFields[i];
    			Class<?> fieldType = declaredField.getType();
				if (fieldType.isPrimitive()) {
    				continue;
    			}
				Object declFieldValue = getField(declaredField, obj);
				if (declFieldValue == null) {
					continue;
				}
				String classHint = String.format("(%s)", declFieldValue.getClass().getSimpleName());
				if (!fieldType.isArray()) {
					VisitedFieldInfo subfieldInfo = new VisitedFieldInfo(fieldInfo.objectTreePath.append(declaredField.getName()+classHint), declaredField, declFieldValue);
					visitObjectTree(subfieldInfo, filter, visitedObjects, visitor);
				}
				else {
					int length = Array.getLength(declFieldValue);
					for (int j = 0; j < length; j++) {
						Object declFieldArrayValue = Array.get(declFieldValue, j);
						VisitedFieldInfo subfieldInfo = new VisitedFieldInfo(fieldInfo.objectTreePath.append(String.format("[%s]%s", j, classHint)), null, declFieldArrayValue);
						visitObjectTree(subfieldInfo, filter, visitedObjects, visitor);
					}
				}
    		}
    		c = c.getSuperclass();
    	}
    }
    
    private static String getPackage(Object obj) {
    	Package p = obj.getClass().getPackage();
    	if (p == null) {
    		return "";
    	}
    	else{
    		return p.getName();
    	}
    }
    
    @SuppressWarnings("rawtypes")
    public static final IVisitFieldsFilter COMMON_FIELDS_FILTER = new com.excelsior.xds.core.utils.ReflectionUtils.VisitFieldsFilter(Arrays.asList((Class)String.class), Arrays.asList("org.eclipse"));
    
    @SuppressWarnings("rawtypes")
    public static class VisitFieldsFilter implements IVisitFieldsFilter{
		private final Set<Class> rejectedClasses;
    	private final Set<String> packages;
    	
		public VisitFieldsFilter(Collection<Class> rejectedClasses,
				Collection<String> packages) {
			this.rejectedClasses = new HashSet<Class>(rejectedClasses);
			this.packages = new HashSet<String>(packages);
		}

		@Override
		public boolean reject(Class<?> c) {
			return rejectedClasses.contains(c);
		}

		@Override
		public boolean reject(String pkg) {
			for (String p : packages) {
				if (pkg.contains(p)) {
					return true;
				}
			}
			return false;
		}
    }
    
    /**
     * Skip visiting of the rejected classes fields  
     * @author lsa80
     */
    public static interface IVisitFieldsFilter {
    	boolean reject(Class<?> c);
    	boolean reject(String packageName);
    }
    
    public static class VisitedFieldInfo {
    	public final ObjectTreePath objectTreePath;
    	public final Field field;
    	public final Object fieldValue;
    	
		public VisitedFieldInfo(ObjectTreePath objectTreePath, Field field,
				Object fieldValue) {
			this.objectTreePath = objectTreePath;
			this.field = field;
			this.fieldValue = fieldValue;
		}
    }
    
    public static class ObjectTreePath{
    	private final StringBuilder path;
    	
    	public ObjectTreePath() {
    		path = new StringBuilder();
    	}
    	
    	public ObjectTreePath(StringBuilder path) {
			this.path = path;
		}

		public ObjectTreePath append(String fieldName) {
			StringBuilder prefix =  new StringBuilder(path);
			if (prefix.length() > 0) {
				if (!fieldName.contains("[")) {
					prefix.append('.');
				}
			}
			return new ObjectTreePath(prefix.append(fieldName));
    	}
		
		public boolean isEmpty() {
			return this.path.length() == 0;
		}

		@Override
		public String toString() {
			return path.toString();
		}
    }
}
