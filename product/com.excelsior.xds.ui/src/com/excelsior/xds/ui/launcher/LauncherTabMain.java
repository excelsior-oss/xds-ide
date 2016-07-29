package com.excelsior.xds.ui.launcher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.model.XdsProjectConfiguration;
import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.project.launcher.ILaunchConfigConst;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.ui.commons.controls.LocationSelector;
import com.excelsior.xds.ui.commons.utils.HelpUtils;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.internal.nls.Messages;

public class LauncherTabMain extends AbstractLauncherTab {
    private Text   textProject;
    private Button btnBrowseProject;

    private LocationSelector exeSelector;

    LauncherTabMain() {
    }

    @Override
    public void createControl(Composite parent) {
        Composite projComp = SWTFactory.createComposite(parent, parent.getFont(), 2, 2, GridData.FILL_BOTH); 
        ((GridLayout)projComp.getLayout()).verticalSpacing = 0;

        // --- Project: ------
        Group group = SWTFactory.createGroup(projComp, Messages.LauncherTabMain_Project+':', 2, 2, GridData.FILL_HORIZONTAL);
        textProject = SWTFactory.createSingleText(group, 1);
        textProject.addModifyListener(widgetListener);
        btnBrowseProject = createPushButton(group, Messages.Common_Browse, null); 
        btnBrowseProject.addSelectionListener(widgetListener);

        SWTFactory.createFreeSpace(projComp, 10, 10, 2);
        // --- Program to run: ------ 
        exeSelector = new LocationSelector(true, true);
        exeSelector.createControl(projComp, 2, Messages.LauncherTabMain_ProgramToRun);
        exeSelector.setActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                updateLaunchConfigurationDialog();
            }
        });
        SWTFactory.createLabel(projComp, "", 2); //$NON-NLS-1$
        setControl(projComp);
        HelpUtils.setHelp(getControl(), getHelpContextId());
    }
    
    @Override
	protected Button getBrowseProjectButton() {
		return btnBrowseProject;
	}

	@Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
        String prjName = null;
        String exePath = null;
        IProject xdsPrj = getCurrentXdsProject();
        if (xdsPrj != null && xdsPrj.exists()) {
            prjName = xdsPrj.getName();
            XdsProjectConfiguration conf = new XdsProjectConfiguration(xdsPrj);
            exePath = conf.getExePath();
        }
        config.setAttribute(ILaunchConfigConst.ATTR_PROJECT_NAME, prjName);
        config.setAttribute(ILaunchConfigConst.ATTR_EXECUTABLE_PATH, exePath);
    }

    @Override
    public void initializeFrom(ILaunchConfiguration config) {
        textProject.setText(getAttr(config, ILaunchConfigConst.ATTR_PROJECT_NAME));
        exeSelector.setLocations("", getAttr(config, ILaunchConfigConst.ATTR_EXECUTABLE_PATH), false); //$NON-NLS-1$
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy config) {
        config.setAttribute(ILaunchConfigConst.ATTR_PROJECT_NAME, StringUtils.trim(textProject.getText()));
        config.setAttribute(ILaunchConfigConst.ATTR_EXECUTABLE_PATH, exeSelector.getLocation());
    }

    @Override
    public String getName() {
        return Messages.LauncherTabMain_Title;
    }

    @Override
    public Image getImage() {
        return ImageUtils.getImage(ImageUtils.XDS_GREEN_IMAGE_16X16);
    }	

    protected void browseProject() {
        IProject ip = chooseXdsProject();
        if (ip != null) {
            textProject.setText(ip.getName());
        }
    }
    
    @Override
    public boolean isValid(ILaunchConfiguration config) {
        setErrorMessage(null);
        setMessage(null);
        // Project:
        String projectName = getAttr(config, ILaunchConfigConst.ATTR_PROJECT_NAME); 
        if (projectName.isEmpty()) {
            setMessage(Messages.LauncherTabMain_EnterProjectName); 
            return false;
        }

        exeSelector.setBrowseProject(null);
        
        IProject project = ProjectUtils.getProject(projectName);
        Sdk sdk = getSdk(project);
        if (!ProjectUtils.isXdsProject(project)) {
            setErrorMessage(Messages.LauncherTabMain_IncorrectProjectName); 
            return false;
        } else {
            // tune exe LocationSelector(s) according to this project settings:
            try {
                String prjdir = ProjectUtils.getProjectLocation(project);
                exeSelector.setFileBrowsePath(prjdir);
                exeSelector.setBrowseProject(project);

                String ext = null;
                if (sdk != null) {
                    ext = sdk.getExecutableFileExtensions();
                }
                exeSelector.setFileBrowseExtension(StringUtils.isBlank(ext) ? null : "." + ext); //$NON-NLS-1$
            } catch(Exception e) {
            	LogHelper.logError(e);
            }
        }

        if (!chkFileFromAttr(sdk, config, ILaunchConfigConst.ATTR_EXECUTABLE_PATH, Messages.LauncherTabMain_IncorreatApplicationFile)) {
            return false;
        }

        return true;
    }

    private Sdk getSdk(IProject project) {
    	if (project == null) {
    		return null;
    	}
    	XdsProjectSettings projectSettings = XdsProjectSettingsManager.getXdsProjectSettings(project);
        return projectSettings.getProjectSdk();
    }
}
