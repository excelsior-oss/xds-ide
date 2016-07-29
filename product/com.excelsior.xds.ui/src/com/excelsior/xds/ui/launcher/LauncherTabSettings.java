package com.excelsior.xds.ui.launcher;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.natures.NatureIdRegistry;
import com.excelsior.xds.core.project.launcher.ILaunchConfigConst;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.internal.nls.Messages;

public class LauncherTabSettings extends AbstractLaunchConfigurationTab {
    private boolean isPktMode;
    private ArgumentsBlock argsSimulator;
    private ArgumentsBlock argsDebugger;
        
        
    public LauncherTabSettings(boolean isPktMode) {
        this.isPktMode = isPktMode;
    }
    
    @Override
    public void createControl(Composite parent) {
        Font font = parent.getFont();
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        comp.setLayout(layout);
        comp.setFont(font);
        
        GridData gd = new GridData(GridData.FILL_BOTH);
        comp.setLayoutData(gd);
        setControl(comp);

        argsSimulator = new ArgumentsBlock(Messages.LauncherTabSettings_SimulatorArgs+':');
        argsDebugger = new ArgumentsBlock(Messages.LauncherTabSettings_DebuggerArgs+':');
        
        argsSimulator.createControl(comp);
        argsDebugger.createControl(comp);
        
    }
        
    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
        config.setAttribute(ILaunchConfigConst.ATTR_DEBUGGER_ARGUMENTS, ""); //$NON-NLS-1$
        config.setAttribute(ILaunchConfigConst.ATTR_SIMULATOR_ARGUMENTS, ""); //$NON-NLS-1$
    }

    @Override
    public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
        this.initializeFrom(workingCopy);
    }
    
    
    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            argsDebugger.setText(configuration.getAttribute(ILaunchConfigConst.ATTR_DEBUGGER_ARGUMENTS, "")); //$NON-NLS-1$
            argsSimulator.setText(configuration.getAttribute(ILaunchConfigConst.ATTR_SIMULATOR_ARGUMENTS, "")); //$NON-NLS-1$
            IProject ip = getXdsProject(configuration);
            boolean visible = false;
            if (ip != null) {
                visible = true;
            }
            argsSimulator.setVisible(visible && !isPktMode);
        } catch (CoreException e) {
            // TODO : fix this error message
            setErrorMessage("LauncherMessages.JavaSettingsTab_Exception_occurred_reading_configuration___15" + e.getStatus().getMessage());  //$NON-NLS-1$
            LogHelper.logError(e);
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(ILaunchConfigConst.ATTR_DEBUGGER_ARGUMENTS, argsDebugger.getText());
        configuration.setAttribute(ILaunchConfigConst.ATTR_SIMULATOR_ARGUMENTS, argsSimulator.getText());
    }

    
    @Override
    public String getName() {
        return Messages.LauncherTabSettings_Title; 
    }   
    
    @Override
    public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
        super.setLaunchConfigurationDialog(dialog);
    }   

    @Override
    public Image getImage() {
        return ImageUtils.getImage(ImageUtils.LAUNCHER_TAB_SETTINGS);
    }   
    
    private IProject getXdsProject(ILaunchConfiguration config) {
        try {
            String prjname = config.getAttribute(ILaunchConfigConst.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
            if (prjname.length() > 0) {
                IProject ip = ResourcesPlugin.getWorkspace().getRoot().getProject(prjname);
                if (ip.exists() && ip.getNature(NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID) != null) {
                    return ip;
                }
            }
        } catch (CoreException e) {}
        return null;
    }
    
    private class ArgumentsBlock {
        private Group group;
        private Button btnVariables;
        private Text textControl;
        private String groupText;
        
        public ArgumentsBlock(String groupText) {
            this.groupText = groupText;
        }
        
        public void createControl(Composite parent) {
            group = new Group(parent, SWT.NONE);
            group.setFont(parent.getFont());
            GridLayout layout = new GridLayout();
            group.setLayout(layout);
            group.setLayoutData(new GridData(GridData.FILL_BOTH));
            group.setText(groupText);
            
            textControl = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
            textControl.addTraverseListener(new TraverseListener() {
                public void keyTraversed(TraverseEvent e) {
                    switch (e.detail) {
                        case SWT.TRAVERSE_ESCAPE:
                        case SWT.TRAVERSE_PAGE_NEXT:
                        case SWT.TRAVERSE_PAGE_PREVIOUS:
                            e.doit = true;
                            break;
                        case SWT.TRAVERSE_RETURN:
                        case SWT.TRAVERSE_TAB_NEXT:
                        case SWT.TRAVERSE_TAB_PREVIOUS:
                            if ((textControl.getStyle() & SWT.SINGLE) != 0) {
                                e.doit = true;
                            } else {
                                if (!textControl.isEnabled() || (e.stateMask & SWT.MODIFIER_MASK) != 0) {
                                    e.doit = true;
                                }
                            }
                            break;
                    }
                }
            });
            GridData gd = new GridData(GridData.FILL_BOTH);
            textControl.setLayoutData(gd);
            textControl.setFont(parent.getFont());
            textControl.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent evt) {
                    scheduleUpdateJob();
                }
            });
            // ControlAccessibleListener.addListener(textControl, group.getText());
            
            btnVariables = createPushButton(group, Messages.Common_Variables, null); 
            btnVariables.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            btnVariables.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
                    dialog.open();
                    String variable = dialog.getVariableExpression();
                    if (variable != null) {
                        textControl.insert(variable);
                    }
                }
            });
        }
        
        public void setText(String s) {
            textControl.setText(s == null ? "" : s); //$NON-NLS-1$
        }
        
        public String getText() {
            String s = textControl.getText();
            return s == null ? "" : s.trim(); //$NON-NLS-1$
        }
        
        public void setVisible(boolean b) {
            ((GridData)group.getLayoutData()).exclude = !b;
            ((GridData)btnVariables.getLayoutData()).exclude = !b;
            ((GridData)textControl.getLayoutData()).exclude = !b;
            group.setVisible(b);
            btnVariables.setVisible(b);
            textControl.setVisible(b);
            group.getParent().pack();
        }
        
    } // class ArgumentsBlock
    

}

