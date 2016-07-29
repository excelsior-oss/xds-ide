package com.excelsior.xds.core.model.internal;

import java.util.List;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsSdkLibraryContainer;

public class XdsSdkLibraryContainer extends    XdsVirtualContainer 
                                    implements IXdsSdkLibraryContainer 
{
	public XdsSdkLibraryContainer( IXdsProject xdsProject
	                             , String name, String path
	                             , IXdsContainer parent
	                             , List<IXdsElement> children ) 
	{
		super(xdsProject, name, path, parent, children);
	}
}