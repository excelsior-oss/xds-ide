package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsRecordVariantLabel;
import com.excelsior.xds.core.model.SourceBinding;

public class XdsRecordVariantLabel extends    SourceBoundXdsElement 
                                   implements IXdsRecordVariantLabel
{
    public XdsRecordVariantLabel( String name
                                , IXdsProject project
                                , IXdsContainer parent
                                , SourceBinding sourceBinding ) 
    {
        super(name, project, parent, sourceBinding);        
    }
    
}
