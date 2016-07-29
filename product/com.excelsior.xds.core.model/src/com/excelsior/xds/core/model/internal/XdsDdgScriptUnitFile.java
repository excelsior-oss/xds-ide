package com.excelsior.xds.core.model.internal;

import org.eclipse.core.resources.IResource;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsDdgScriptUnitFile;
import com.excelsior.xds.core.model.IXdsProject;

public class XdsDdgScriptUnitFile extends XdsTextFile implements
		IXdsDdgScriptUnitFile {

	public XdsDdgScriptUnitFile(IXdsProject xdsProject, IResource resource,
			IXdsContainer parent) {
		super(xdsProject, resource, parent);
	}
}
