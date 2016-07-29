package com.excelsior.xds.ui.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IncrementalProjectBuilder;

import com.excelsior.xds.core.builders.XdsSourceBuilderConstants;
import com.excelsior.xds.ui.internal.nls.Messages;

public class CompileFileCommandHandler extends AbstractBuildFileCommandHandler{
    
    public CompileFileCommandHandler() {
        super(IncrementalProjectBuilder.FULL_BUILD);
    }

    @Override
    protected Map<String, String> getCommonProperties() {
        Map<String, String> commonProperties = new HashMap<String, String>();
        commonProperties.putAll(super.getCommonProperties());
        commonProperties.put(XdsSourceBuilderConstants.COMPILE_FILE_KEY, Boolean.TRUE.toString());
        return commonProperties;
    }
    
    protected String getVerb() {
        return Messages.CompileFileCommandHandler_Compiling;
    }
}
