package com.excelsior.xds.ui.commands;

import org.eclipse.core.resources.IncrementalProjectBuilder;

import com.excelsior.xds.ui.internal.nls.Messages;

public class RebuildFileCommandHandler extends AbstractBuildFileCommandHandler {
    
    public RebuildFileCommandHandler() {
        super(IncrementalProjectBuilder.FULL_BUILD);
    }
    
    protected String getVerb() {
        return Messages.RebuildFileCommandHandler_Rebuilding;
    }
}
