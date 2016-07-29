package com.excelsior.xds.ui.launcher;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.variables.VariableUtils;
import com.excelsior.xds.ui.commons.utils.SelectionUtils;
import com.excelsior.xds.ui.internal.nls.Messages;

public abstract class AbstractLauncherTab extends AbstractLaunchConfigurationTab {
	protected WidgetListener widgetListener = new WidgetListener();
    
    protected IProject getCurrentXdsProject() {
		return SelectionUtils.getObjectsFromStructuredSelection(IProject.class)
				.stream().filter(ProjectUtils::isXdsProject).findFirst()
				.orElse(null);
    }
    
    protected IProject chooseXdsProject() {
        ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
        dialog.setTitle(Messages.LauncherTabMain_ProjectSelection); 
        dialog.setMessage(Messages.LauncherTabMain_SelectXdsProject+':');

        dialog.setElements(ProjectUtils.getXdsProjects().toArray());

        IProject currentProject = getCurrentXdsProject();
        if (currentProject != null) {
            dialog.setInitialSelections(new Object[] { currentProject });
        }
        if (dialog.open() == Window.OK) {			
            return (IProject) dialog.getFirstResult();
        }		
        return null;		
    }
    
    protected boolean chkFileFromAttr(Sdk sdk, ILaunchConfiguration config, String attr, String errMsg) {
        String path = getAttr(config, attr);
        try {
            path = VariableUtils.performStringSubstitution(sdk, path, false);
        } catch (CoreException e) {
        	LogHelper.logError(e);
        }
        if (!new File(path).isFile()) {
            setErrorMessage(errMsg); 
            return false;
        }
        return true;
    }
    
    protected String getAttr(ILaunchConfiguration config, String attr) {
        try {
            return config.getAttribute(attr, ""); //$NON-NLS-1$
        } catch(CoreException e) {
        	LogHelper.logError(e);
        }
        return ""; //$NON-NLS-1$
    }
    
    /**
     * A listener which handles widget change events for the controls
     * in this tab.
     */
    protected class WidgetListener implements ModifyListener, SelectionListener {

        public void modifyText(ModifyEvent e) {
            // read all fields into config and revalidate
            updateLaunchConfigurationDialog();
        }

        public void widgetSelected(SelectionEvent e) {
            Object source = e.getSource();
            if (source == getBrowseProjectButton()) {
                browseProject();
            } else {
                // read all fields into config and revalidate
                updateLaunchConfigurationDialog(); 
            }
        }

        public void widgetDefaultSelected(SelectionEvent e) {}
    }

    protected abstract Button getBrowseProjectButton();
    protected abstract void browseProject();
}
