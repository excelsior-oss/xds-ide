package com.excelsior.xds.ui.commands;

import org.eclipse.core.resources.IncrementalProjectBuilder;

import com.excelsior.xds.ui.internal.nls.Messages;

public class MakeFileCommandHandler extends AbstractBuildFileCommandHandler{
    
    public MakeFileCommandHandler() {
        super(IncrementalProjectBuilder.INCREMENTAL_BUILD);
    }
    
    protected String getVerb() {
        return Messages.MakeFileCommandHandler_Making;
    }
}
