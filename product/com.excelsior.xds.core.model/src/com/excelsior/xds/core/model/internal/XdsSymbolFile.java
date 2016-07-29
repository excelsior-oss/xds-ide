package com.excelsior.xds.core.model.internal;

import org.eclipse.core.resources.IResource;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsSymbolFile;

public class XdsSymbolFile extends    XdsWorkspaceCompilationUnit
                           implements IXdsSymbolFile
{
    public XdsSymbolFile( IXdsProject xdsProject, IResource resource
                        , IXdsContainer parent ) 
    {
        super(xdsProject, resource, parent);
    }

}
