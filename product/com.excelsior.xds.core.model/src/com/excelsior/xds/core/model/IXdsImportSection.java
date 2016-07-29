package com.excelsior.xds.core.model;

import java.util.Collection;

public interface IXdsImportSection extends IXdsSyntheticElement
                                         , IXdsImportElement
                                         , IXdsContainer
                                         , ISourceBound 
{
	public Collection<IXdsImportElement> getImportElements();
}
