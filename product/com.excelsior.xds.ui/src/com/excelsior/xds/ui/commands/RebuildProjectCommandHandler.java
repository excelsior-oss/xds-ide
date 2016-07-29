package com.excelsior.xds.ui.commands;

import java.util.Map;

import org.eclipse.core.resources.IncrementalProjectBuilder;

import com.excelsior.xds.core.builders.XdsSourceBuilderConstants;
import com.excelsior.xds.core.utils.collections.XMapUtils;

public class RebuildProjectCommandHandler extends AbstractBuildProjectCommandHandler {
    public RebuildProjectCommandHandler() {
        // TODO : supply correct progress monitor
        super(IncrementalProjectBuilder.FULL_BUILD);
    }
    
    /*
     * TODO : get rid of this method. Use BuilderUtils 
     */
    @Override
    protected Map<String, String> getCommonProperties() {
    	Map<String, String> commonProperties = 
    			XMapUtils.newHashMap(XdsSourceBuilderConstants.REBUILD_PROJECT_KEY, Boolean.TRUE.toString());
        commonProperties.putAll(super.getCommonProperties());
        return commonProperties;
    }
}
