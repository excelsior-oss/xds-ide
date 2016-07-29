package com.excelsior.xds.core.model;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;

public interface IXdsExternalCompilationUnit extends IXdsCompilationUnit
                                                   , IStorage 
{
	IPath getFullPath();
}
