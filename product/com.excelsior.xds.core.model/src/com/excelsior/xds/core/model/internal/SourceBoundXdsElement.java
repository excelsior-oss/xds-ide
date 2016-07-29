package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.ISourceBound;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.SourceBinding;

public class SourceBoundXdsElement extends    SimpleXdsElement
                                   implements ISourceBound
{
    private final SourceBinding sourceBinding;

    public SourceBoundXdsElement( String name
                                , IXdsProject project
                                , IXdsContainer parent
                                , SourceBinding sourceBinding ) 
    {
        super(name, project, parent);
        this.sourceBinding = sourceBinding;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SourceBinding getSourceBinding() {
        return sourceBinding;
    }
    
}
