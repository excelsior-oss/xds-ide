package com.excelsior.xds.parser.commons.ast;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

/**
 * Base class for node types in the AST tree. 
 */
public class CompositeType <T extends PstCompositeNode> extends ElementType {

    private final Constructor<T> nodeConstructor;
    private final Class<T> nodeClass;

    public CompositeType(String debugName, Class<T> nodeClass) {
        super(debugName);
        this.nodeClass = nodeClass;
        nodeConstructor = getConstructor(nodeClass);
    }
    
    public Class<T> getNodeClass() {
        return nodeClass;
    }

    public T createNode() {
        try {
            return nodeConstructor.newInstance(this);
        } catch (IllegalArgumentException e) {
            LogHelper.logError(e);
        } catch (InstantiationException e) {
            LogHelper.logError(e);
        } catch (IllegalAccessException e) {
            LogHelper.logError(e);
        } catch (InvocationTargetException e) {
            LogHelper.logError(e);
        }
        return null;
    }
    
    private Constructor<T> getConstructor(Class<T> nodeClass) {
        try {
            return nodeClass.getConstructor(ModulaCompositeType.class);
        } catch (SecurityException e) {
            LogHelper.logError(e);
        } catch (NoSuchMethodException e) {
            LogHelper.logError(e);
        }
        return null;
    }
        
}
