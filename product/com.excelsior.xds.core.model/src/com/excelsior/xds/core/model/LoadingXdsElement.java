package com.excelsior.xds.core.model;

import com.excelsior.xds.core.model.internal.SimpleXdsElement;

/**
 * XDS element being loaded
 */
public class LoadingXdsElement extends SimpleXdsElement
{
    public LoadingXdsElement(String name, IXdsProject project, IXdsContainer parent) 
    {
        super(name, project, parent);
    }

}
