package com.excelsior.xds.core.model.internal;

import java.util.List;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsExternalCompilationUnitContainer;
import com.excelsior.xds.core.model.IXdsProject;

public class XdsExternalCompilationUnitContainer extends XdsVirtualContainer implements IXdsExternalCompilationUnitContainer {
	public XdsExternalCompilationUnitContainer(IXdsProject xdsProject,
			String name, String path, IXdsContainer parent,
			List<IXdsElement> children) {
		super(xdsProject, name, path, parent, children);
	}
}
