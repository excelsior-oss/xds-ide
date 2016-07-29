package com.excelsior.xds.core.model.internal;

import org.eclipse.core.resources.IResource;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;

public class XdsTextFile extends XdsResource {

    public XdsTextFile(IXdsProject xdsProject, IResource resource, IXdsContainer parent) {
        super(xdsProject, parent, resource);
    }
}
