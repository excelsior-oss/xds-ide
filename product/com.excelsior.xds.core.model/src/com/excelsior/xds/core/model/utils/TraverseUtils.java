package com.excelsior.xds.core.model.utils;

import java.util.Collection;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;

public abstract class TraverseUtils 
{
    public static void walk(IXdsElement xdsElement, IVisitOperation operation) {
        operation.execute(xdsElement);
        if (xdsElement instanceof IXdsContainer) {
        	Collection<IXdsElement> children = ((IXdsContainer) xdsElement).getChildren();
            for (IXdsElement child : children) {
                walk(child, operation);
            }
        }
    }
}
