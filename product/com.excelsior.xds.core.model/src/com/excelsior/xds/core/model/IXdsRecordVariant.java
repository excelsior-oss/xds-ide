package com.excelsior.xds.core.model;

import java.util.Collection;

public interface IXdsRecordVariant extends IXdsSyntheticElement
                                         , IXdsContainer
                                         , ISourceBound
{
    public boolean isElseVariant();
    
    public Collection<IXdsRecordVariantLabel> getLabels();
    
}
