package com.excelsior.xds.ui.launcher;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.debug.ui.WorkingDirectoryBlock;
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

public class LauncherTabArguments extends AbstractLaunchConfigurationTab {
	private ArgumentsBlock argsCmdline;

	// Working directory
	protected WorkingDirectoryBlock fWorkingDirectoryBlock;
		
		
	public LauncherTabArguments() {
		fWorkingDirectoryBlock = new XdsWorkingDirectoryBlock();
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

                argsCmdline = new ArgumentsBlock(Messages.LauncherTabArguments_ProgramArgs+':');
		
		argsCmdline.createControl(comp);
		
		fWorkingDirectoryBlock.createControl(comp);		
	}
		
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		return fWorkingDirectoryBlock.isValid(config);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(ILaunchConfigConst.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
		config.setAttribute(ILaunchConfigConst.ATTR_DEBUGGER_ARGUMENTS, ""); //$NON-NLS-1$
		config.setAttribute(ILaunchConfigConst.ATTR_SIMULATOR_ARGUMENTS, ""); //$NON-NLS-1$
		fWorkingDirectoryBlock.setDefaults(config);
	}

	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		this.initializeFrom(workingCopy);
	}
	
	
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			argsCmdline.setText(configuration.getAttribute(ILaunchConfigConst.ATTR_PROGRAM_ARGUMENTS, "")); //$NON-NLS-1$
			fWorkingDirectoryBlock.initializeFrom(configuration);
		} catch (CoreException e) {
		    // TODO : fix this error message
			setErrorMessage("LauncherMessages.JavaArgumentsTab_Exception_occurred_reading_configuration___15" + e.getStatus().getMessage());  //$NON-NLS-1$
            LogHelper.logError(e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ILaunchConfigConst.ATTR_PROGRAM_ARGUMENTS, argsCmdline.getText());
		fWorkingDirectoryBlock.performApply(configuration);
	}

	
	@Override
	public String getName() {
		return Messages.LauncherTabArguments_Arguments; 
	}	
	
	@Override
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
		fWorkingDirectoryBlock.setLaunchConfigurationDialog(dialog);
	}	

	@Override
	public String getErrorMessage() {
		String m = super.getErrorMessage();
		if (m == null) {
			return fWorkingDirectoryBlock.getErrorMessage();
		}
		return m;
	}

	@Override
	public String getMessage() {
		String m = super.getMessage();
		if (m == null) {
			return fWorkingDirectoryBlock.getMessage();
		}
		return m;
	}
	
	@Override
	public Image getImage() {
		return ImageUtils.getImage(ImageUtils.LAUNCHER_TAB_ARGUMENTS);
	}	
	
	@Override
	public String getId() {
		return "com.excelsior.xds.ui.launcher.argumentsTab"; //$NON-NLS-1$
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
		
	}
	

	private class XdsWorkingDirectoryBlock extends WorkingDirectoryBlock {

		public XdsWorkingDirectoryBlock() {
			super(ILaunchConfigConst.ATTR_WORKING_DIRECTORY, null);
		}

		@Override
		protected IProject getProject(ILaunchConfiguration configuration) {
			return getXdsProject(configuration);
		}

		@Override
		protected void log(CoreException e) {
			setErrorMessage(e.getMessage());
		}
	}
}