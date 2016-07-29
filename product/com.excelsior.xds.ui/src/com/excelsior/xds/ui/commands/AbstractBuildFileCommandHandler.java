package com.excelsior.xds.ui.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.excelsior.xds.core.builders.XdsSourceBuilderConstants;
import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.utils.BuilderUtils;
import com.excelsior.xds.ui.commons.utils.SelectionUtils;
import com.excelsior.xds.ui.internal.nls.Messages;

public abstract class AbstractBuildFileCommandHandler extends AbstractHandler{
    private int buildKind;
    
    protected AbstractBuildFileCommandHandler(int buildKind) {
        this.buildKind = buildKind;
    }
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        List<IResource> resources = SelectionUtils.getObjectsFromStructuredSelection(selection, IResource.class);
        int i = 0;
        if (resources.isEmpty()) {
            IResource resource = CoreEditorUtils.getActiveEditorInputAsResource();
            if (resource != null) {
                resources = new ArrayList<IResource>(1);
                resources.add(resource);
            }
        }
        
        if (!CoreEditorUtils.saveEditors(false)) {
            return null;
        }

        final ArrayList<BuilderUtils.MultipleBuildItem> itemsToBuild = new ArrayList<BuilderUtils.MultipleBuildItem>(); 

        for (IResource res : resources) {
            Map<String, String> args = new HashMap<String, String>();
            args.putAll(getCommonProperties());
            String absolutePath = ResourceUtils.getAbsolutePath(res);
            args.put(XdsSourceBuilderConstants.SOURCE_FILE_PATH_KEY_PREFIX
                    + (++i), absolutePath);

            IProject p = res.getProject();

            itemsToBuild.add(new BuilderUtils.MultipleBuildItem(buildKind, p, args));
        }
        
        BuilderUtils.invokeMultipleBuild(itemsToBuild, new BuilderUtils.IMiltipleBuildFinishListener() {
            @Override
            public void allItemsAreFinished() {
                AbstractBuildProjectCommandHandler.printSummary(itemsToBuild);
            }
        });

        return null;
    }
    
    protected String getVerb() {
        return Messages.AbstractBuildFileCommandHandler_Building;
    }
    
    protected Map<String, String> getCommonProperties() {
        return new HashMap<String, String>();
    }
}
