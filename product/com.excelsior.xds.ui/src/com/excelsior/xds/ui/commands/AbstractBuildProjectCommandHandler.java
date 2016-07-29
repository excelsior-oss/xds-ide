package com.excelsior.xds.ui.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.excelsior.xds.builder.console.BuildConsoleManager;
import com.excelsior.xds.core.builders.XdsBuildResult;
import com.excelsior.xds.core.console.ColorStreamType;
import com.excelsior.xds.core.console.IXdsConsole;
import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.natures.NatureIdRegistry;
import com.excelsior.xds.core.project.NatureUtils;
import com.excelsior.xds.core.utils.BuilderUtils;
import com.excelsior.xds.ui.commons.utils.SelectionUtils;

public abstract class AbstractBuildProjectCommandHandler extends AbstractHandler {
    private int buildKind;
    private static final String SEPARATOR_LINE = "------------------------------------------------"; //$NON-NLS-1$
    
    protected AbstractBuildProjectCommandHandler(int buildKind) {
        this.buildKind = buildKind;
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        List<Object> elements = SelectionUtils.getObjectsFromStructuredSelection(selection, Object.class);
        Set<IProject> projects = new HashSet<IProject>();
        for (Object e : elements) {
            if (e instanceof IResource) {
                IResource res = (IResource) e;
                projects.add(res.getProject());
            }
            else if (e instanceof IXdsElement) {
                IXdsElement xdsElement = (IXdsElement) e;
                projects.add(xdsElement.getXdsProject().getProject());
            }
        }
        
        if (projects.isEmpty()) {
            IResource resource = CoreEditorUtils.getActiveEditorInputAsResource();
            if (resource != null) {
                IProject project = resource.getProject();
                if (NatureUtils.hasNature(project, NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID)) {
                    projects.add(project);
                }
            }
        }

        if (!CoreEditorUtils.saveEditors(false)) {
            return null;
        }
        
        final ArrayList<BuilderUtils.MultipleBuildItem> itemsToBuild = new ArrayList<BuilderUtils.MultipleBuildItem>();

        for (IProject project : projects) {
            itemsToBuild.add(new BuilderUtils.MultipleBuildItem(buildKind, project, getCommonProperties()));
        }
        
        BuilderUtils.invokeMultipleBuild(itemsToBuild, new BuilderUtils.IMiltipleBuildFinishListener() {
            @Override
            public void allItemsAreFinished() {
                printSummary(itemsToBuild);
            }
        });

        return null;
    }
    
    protected Map<String, String> getCommonProperties() {
        return new HashMap<String, String>();
    }
    
    public static void printSummary(List<BuilderUtils.MultipleBuildItem> items) {
        if (items.size() > 1) {
            IXdsConsole console = BuildConsoleManager.getConsole(null);
            console.println(""); //$NON-NLS-1$
            console.println(SEPARATOR_LINE);
            console.println(""); //$NON-NLS-1$
            for (BuilderUtils.MultipleBuildItem it :items) {
                XdsBuildResult res = it.getBuildResult();
                ColorStreamType cs = (res == XdsBuildResult.SUCCESS) ? ColorStreamType.SYSTEM : ColorStreamType.ERROR;
                console.println(it.getSummaryString(), cs);
            }
        }
    }
    
}
