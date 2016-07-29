package com.excelsior.xds.core.resource.visitor;

/**
 * Special RuntimeException, that can be throw from visit method of org.eclipse.core.resources.IResourceVisitor,
 * in order to interrupt visitor operation
 * @author lsa80
 */
public class InterruptResourceVisitorException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -1097281615838453135L;
}
