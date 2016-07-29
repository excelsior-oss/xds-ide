package com.excelsior.xds.core.resource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import com.excelsior.xds.core.utils.XdsFileUtils;

public class XdsSourceIFilePredicate extends IFileFromXdsProjectPredicate {
	@Override
	public boolean apply(IResource r) {
		if (!super.apply(r)) {
			return false;
		}
		
		IFile f = (IFile)r;
		return XdsFileUtils.isCompilationUnitFile(f);
	}
}
