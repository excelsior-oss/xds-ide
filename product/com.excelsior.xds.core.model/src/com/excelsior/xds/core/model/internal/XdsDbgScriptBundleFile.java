package com.excelsior.xds.core.model.internal;

import org.eclipse.core.resources.IResource;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsDbgScriptBundleFile;
import com.excelsior.xds.core.model.IXdsProject;

public class XdsDbgScriptBundleFile extends XdsTextFile implements
		IXdsDbgScriptBundleFile {

	public XdsDbgScriptBundleFile(IXdsProject xdsProject, IResource resource,
			IXdsContainer parent) {
		super(xdsProject, resource, parent);
	}
}
