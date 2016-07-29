package com.excelsior.xds.ui.commands;

import java.util.Map;

import org.eclipse.core.resources.IncrementalProjectBuilder;

import com.excelsior.xds.core.builders.XdsSourceBuilderConstants;
import com.excelsior.xds.core.utils.collections.XMapUtils;

public class BuildProjectCommandHandler extends AbstractBuildProjectCommandHandler {
    public BuildProjectCommandHandler() {
        super(IncrementalProjectBuilder.FULL_BUILD);
    }
    
    /*
     * TODO : get rid of this method. Use BuilderUtils 
     */
    @Override
    protected Map<String, String> getCommonProperties() {
    	Map<String, String> commonProperties = 
    			XMapUtils.newHashMap(XdsSourceBuilderConstants.BUILD_PROJECT_KEY, Boolean.TRUE.toString());
        commonProperties.putAll(super.getCommonProperties());
        return commonProperties;
    }
}
