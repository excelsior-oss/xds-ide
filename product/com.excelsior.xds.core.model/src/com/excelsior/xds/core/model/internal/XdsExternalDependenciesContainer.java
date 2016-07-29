package com.excelsior.xds.core.model.internal;

import java.util.List;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsExternalDependenciesContainer;
import com.excelsior.xds.core.model.IXdsProject;

public class XdsExternalDependenciesContainer extends XdsVirtualContainer
		implements IXdsExternalDependenciesContainer {

	public XdsExternalDependenciesContainer(IXdsProject xdsProject, String name, String path,
			IXdsContainer parent, List<IXdsElement> children) {
		super(xdsProject, name, path, parent, children);
	}

}
