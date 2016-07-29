package com.excelsior.xds.ui.preferences.project;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import com.excelsior.xds.core.variables.VariableUtils;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.internal.nls.Messages;

/**
 * A control for setting the working directory associated with a launch
 * configuration.
 * 
 * @since 3.5
 */
public class CompilerWorkingDirectoryBlock extends AbstractLaunchConfigurationTab {
	private Group group;
    
    private String blockTitle;
    private ActionListener actionListener;
            
    // Local directory
    private Button fWorkspaceButton;
    private Button fFileSystemButton;
    private Button fVariablesButton;
    
    //bug 29565 fix
    private Button fRbDefaultDir = null;
    private Button fRbOtherDir = null;
    private Text   fDefaultDirText = null;
    private Text   fOtherDirText = null;
    
    
    private WidgetListener fListener = new WidgetListener();
    private volatile boolean otherIsEdited = false;

    
    public CompilerWorkingDirectoryBlock(String blockTitle) {
        this.blockTitle = blockTitle;
    }
    
    public final void createControl(Composite parent) {
        Font font = parent.getFont();   
        group = SWTFactory.createGroup(parent, blockTitle, 2, 1, GridData.FILL_HORIZONTAL);
        setControl(group);
        
        //default choice
        Composite comp = SWTFactory.createComposite(group, font, 2, 2, GridData.FILL_BOTH, 0, 0);
        fRbDefaultDir = SWTFactory.createRadiobutton(comp, Messages.CompilerWorkingDirectoryBlock_Default+':', 1);
        fRbDefaultDir.addSelectionListener(fListener);
        fDefaultDirText = SWTFactory.createSingleText(comp, 1); 
        fDefaultDirText.addModifyListener(fListener);
        fDefaultDirText.setEnabled(false);
        //user enter choice
        fRbOtherDir = SWTFactory.createRadiobutton(comp, Messages.CompilerWorkingDirectoryBlock_Other+':', 1);
        fRbOtherDir.addSelectionListener(fListener);
        fOtherDirText = SWTFactory.createSingleText(comp, 1);
        fOtherDirText.addModifyListener(fListener);
        //buttons
        Composite buttonComp = SWTFactory.createComposite(comp, font, 3, 2, GridData.HORIZONTAL_ALIGN_END); 
        GridLayout ld = (GridLayout)buttonComp.getLayout();
        ld.marginHeight = 1;
        ld.marginWidth = 0;
        fWorkspaceButton = createPushButton(buttonComp, Messages.CompilerWorkingDirectoryBlock_Workspace, null); 
        fWorkspaceButton.addSelectionListener(fListener);
        fFileSystemButton = createPushButton(buttonComp, Messages.CompilerWorkingDirectoryBlock_FileSystem, null); 
        fFileSystemButton.addSelectionListener(fListener);
        fVariablesButton = createPushButton(buttonComp, Messages.CompilerWorkingDirectoryBlock_Variables, null); 
        fVariablesButton.addSelectionListener(fListener);
    }
    
    public Composite getComposite() {
    	return group;
    }
    
    public ArrayList<Control> getFirstColumnControls() {
        // used for layouts
        ArrayList<Control> al = new ArrayList<Control>();
        al.add(fRbDefaultDir);
        al.add(fRbOtherDir);
        return al;
    }
    
    /**
     * Returns the selected workspace container,or <code>null</code>
     */
    public static IContainer getContainer(String path) {
        if (path.length() > 0) {
            IResource res = null;
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            if (path.startsWith("${workspace_loc:")) { //$NON-NLS-1$
                try {
                    path = VariableUtils.performStringSubstitution(path, false);
                    IPath uriPath = new Path(path).makeAbsolute();
                    IContainer[] containers = root.findContainersForLocationURI(URIUtil.toURI(uriPath));
                    if (containers.length > 0) {
                        res = containers[0];
                    }
                } 
                catch (CoreException e) {
                }
            } 
            else {      
                res = root.findMember(path);
            }
            if (res instanceof IFile) {
                res = ((IFile)res).getParent();
            }
            if (res instanceof IContainer) {
                return (IContainer)res;
            }
        }
        return null;
    }
        
    @Override
    public String getName() {
        return blockTitle; 
    }
    
    protected void log(CoreException e) {
    }


    /**
     * @return non-null string for otherWorkingDirectory or null when default working directory selected
     */
    public String getWorkingDirectory() {
        return fRbDefaultDir.getSelection() ? null : fOtherDirText.getText().trim();
    }
    
    private String getOtherDirectoryText() {
        return fOtherDirText.getText().trim();
    }

    /**
     * Sets the text of the default working directory.
     * @param dir the directory to set the widget to
     */
    public void setDefaultWorkingDirectoryText(String dir) {
        fDefaultDirText.setText(dir);
    }
    
    /**
     * Sets the directory of the other working directory to be used.
     * @param dir the directory to set the widget to
     */
    public void setOtherWorkingDirectoryText(String dir) {
        otherIsEdited = true;
        fOtherDirText.setText(dir);
        otherIsEdited = false;
    }

    public void setMode(boolean setToDefault) {
        fRbDefaultDir.setSelection(setToDefault);
        fRbOtherDir.setSelection(!setToDefault);
        handleRadiobuttonSelected(setToDefault);
    }
    
    /**
     * @param actionListener - listener will be called for all changes in the block data
     */
    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }
    
    
    
    ////// Enent handling:

    
    /**
     * Show a dialog that lets the user select a working directory
     */
    private void handleWorkingDirBrowseButtonSelected() {
        DirectoryDialog dialog = new DirectoryDialog(getShell());
        dialog.setMessage(Messages.CompilerWorkingDirectoryBlock_SelectXcWorkDir); 
        String currentWorkingDir = getOtherDirectoryText();
        if (!currentWorkingDir.trim().equals("")) { //$NON-NLS-1$
            File path = new File(currentWorkingDir);
            if (path.exists()) {
                dialog.setFilterPath(currentWorkingDir);
            }       
        }
        String selectedDirectory = dialog.open();
        if (selectedDirectory != null) {
            setOtherWorkingDirectoryText(selectedDirectory);
            if (actionListener != null) {
                actionListener.actionPerformed(null);
            }
        }       
    }

    /**
     * Show a dialog that lets the user select a working directory from 
     * the workspace
     */
    private void handleWorkspaceDirBrowseButtonSelected() {
        IContainer currentContainer= getContainer(getOtherDirectoryText());
        if (currentContainer == null) {
            currentContainer = ResourcesPlugin.getWorkspace().getRoot();
        } 
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), currentContainer, false, Messages.CompilerWorkingDirectoryBlock_SelectWorkspaceRelWorkDir+':'); 
        dialog.showClosedProjects(false);
        dialog.open();
        Object[] results = dialog.getResult();      
        if ((results != null) && (results.length > 0) && (results[0] instanceof IPath)) {
            IPath path = (IPath)results[0];
            String containerName = path.makeRelative().toString();
            setOtherWorkingDirectoryText("${workspace_loc:" + containerName + "}"); //$NON-NLS-1$ //$NON-NLS-2$
            if (actionListener != null) {
                actionListener.actionPerformed(null);
            }
        }           
    }
    
    /**
     * The working dir variables button has been selected
     */
    private void handleWorkingDirVariablesButtonSelected() {
        StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
        dialog.open();
        String variableText = dialog.getVariableExpression();
        if (variableText != null) {
            otherIsEdited = true;
            fOtherDirText.insert(variableText);
            otherIsEdited = false;
            if (actionListener != null) {
                actionListener.actionPerformed(null);
            }
        }
    }

    /**
     * The radio button has been selected.
     */
    private void handleRadiobuttonSelected(boolean isDefault) {
        fWorkspaceButton.setEnabled(!isDefault);
        fOtherDirText.setEnabled(!isDefault);
        fVariablesButton.setEnabled(!isDefault);
        fFileSystemButton.setEnabled(!isDefault);
    }

    

    /**
     * A listener to update for text changes and widget selection
     */
    private class WidgetListener extends SelectionAdapter implements ModifyListener {
        public void modifyText(ModifyEvent e) {
            if (e.getSource() == fOtherDirText && actionListener != null && !otherIsEdited) {
                actionListener.actionPerformed(null);
            }
        }
        public void widgetSelected(SelectionEvent e) {
            Object source= e.getSource();
            if (source == fWorkspaceButton) {
                handleWorkspaceDirBrowseButtonSelected();
            }
            else if (source == fFileSystemButton) {
                handleWorkingDirBrowseButtonSelected();
            } 
            else if (source == fVariablesButton) {
                handleWorkingDirVariablesButtonSelected();
            } 
            
            else if(source == fRbDefaultDir || source == fRbOtherDir) {
                handleRadiobuttonSelected(fRbDefaultDir.getSelection());
                if (actionListener != null) {
                    actionListener.actionPerformed(null);
                }
            }
        }
    }



    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    }
}

