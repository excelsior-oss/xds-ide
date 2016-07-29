package com.excelsior.xds.ui.commons.controls;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

import com.excelsior.xds.core.variables.VariableUtils;
import com.excelsior.xds.ui.commons.internal.nls.Messages;
import com.excelsior.xds.ui.commons.utils.SWTFactory;


/*
 * Usage:
 * ls = new LocationSelector();
 * ls.createControl(composite, 3, "Location title");
 * ls.setLocations("c:\\temp", "", true);
 * ...
 * String loc = ls.getLocation();
 *
 * (control prototype - org.eclipse.pde.internal.ui.launcher.BaseBlock)
 */

public class LocationSelector {
    
    private Button cbUseDefLocation;
    private Link link;
    private Text text;
    private Button btnBrowseWorkspace;
    private Button btnBrowseSystem; 
    private Button btnBrowseVariables;
    private boolean fileMode;
    private String fileBrowsePath;
    private String fileBrowseExtension;
    private IProject browseProject;
    
    private String defLocation;
    private String otherLocation;
    private ActionListener actionListener;
    private boolean hideDefLocation;
    private boolean fakeDefLocation; // def. location is some string like "<SDK working dir>"
    private String  linkTxt;
    private String  linkTxtFake;
    
    private boolean isEnabled;

    public LocationSelector(boolean fileMode, boolean hideDefLocation) {
        this.fileMode = fileMode;
        this.hideDefLocation = hideDefLocation;
        isEnabled = true;
    }

    /**
     * Should be used after createControl()
     * 
     * @param defLocation  - default location text
     * @param otherLocation  - other location (may be "")
     * @param useDefLocation  - initial checkbox state
     */
    public void setLocations(String defLocation, String otherLocation, boolean useDefLocation) {
        this.defLocation = defLocation;
        this.otherLocation = otherLocation;
        cbUseDefLocation.setSelection(useDefLocation && !hideDefLocation);
        handleCbUseDefLocation();
    }
    
    /**
     * Should be used after createControl()
     * 
     * @param defLocation  - default location text
     */
    public void setDefLocation(String defLocation) {
        this.defLocation = defLocation;
        if (cbUseDefLocation.getSelection()) {
            text.setText(defLocation);
        }
        reenableAll();
    }
    
    
    /**
     * Should be used after createControl()
     * 
     * @param fake - true => def. location is some text like "<SDK working dir>"
     */
    public void setDefLocationIsFake(boolean fake) {
        fakeDefLocation = fake;
        reenableAll();
    }

    
    /**
     * Path used for file browser when no path specified in the entry filed
     */
    public void setFileBrowsePath(String fileBrowsePath) {
        this.fileBrowsePath = fileBrowsePath;
    }
    

    /**
     * ~=".exe" or null to don't filter files
     */
    public void setFileBrowseExtension (String fileBrowseExtension) {
        this.fileBrowseExtension = fileBrowseExtension;
    }
    
    /**
     * Used to start Workspace browse from this project.
     * @param prj
     */
    public void setBrowseProject(IProject fileBrowseProject) {
        this.browseProject = fileBrowseProject; 
        
    }


    /**
     * Should be used after createControl() and setLocations()
     * 
     * @return location or null when default location is selected
     */
    public String getLocation() {
        return cbUseDefLocation.getSelection() ? null : getLocationTxt();
    }
    
    
    /**
     * Set action listener - it is called (with null event) on every changes in the control
     * 
     * @param al
     */
    public void setActionListener(ActionListener al) {
        actionListener = al;
    }

    /**
     * @param en - enable state
     */
    public void setEnabled(boolean en) {
        isEnabled = en;
        reenableAll();
    }

    
    /**
     * 
     * @param parent
     * @param hspan (must be >= 2 when frameText == null)
     * @param frameText (null to don't create frame)
     */
    public void createControl(Composite parent, int hspan, String frameText) {
        if (frameText != null) {
            Group gr = SWTFactory.createGroup(parent, frameText, 2, hspan, GridData.FILL_HORIZONTAL);
            parent = gr;
            hspan = 2;
        }
        cbUseDefLocation = SWTFactory.createCheckbox(parent, Messages.LocationSelector_UseDefLocation, hspan);
        cbUseDefLocation.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleCbUseDefLocation();
            }
        });
        if (hideDefLocation) {
            cbUseDefLocation.setVisible(false);
            GridData gd = (GridData)cbUseDefLocation.getLayoutData();
            gd.exclude = true;
            cbUseDefLocation.setLayoutData(gd);
        }
        
        link = new Link(parent, SWT.NONE);
        String s = Messages.LocationSelector_Location+':';
        linkTxt     = (hideDefLocation ? "" : "      ") + "<a href=\"location\">" + s + "</a>";   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        linkTxtFake = (hideDefLocation ? "" : "      ") + s; //$NON-NLS-1$ //$NON-NLS-2$
        link.setText(linkTxt);
        link.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    showPath();
                }
        });
        
        text = SWTFactory.createSingleText(parent, hspan-1);
        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                textModified();
            }
        });
        
        //buttons
        Composite buttonComp = SWTFactory.createComposite(parent, parent.getFont(), 3, hspan, GridData.HORIZONTAL_ALIGN_END); 
        GridLayout ld = (GridLayout)buttonComp.getLayout();
        ld.marginHeight = 1;
        ld.marginWidth = 0;
        btnBrowseWorkspace = SWTFactory.createPushButton(buttonComp, Messages.LocationSelector_Workspace, null); 
        btnBrowseWorkspace.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                browseWorkspace();
            }
        });
        btnBrowseSystem = SWTFactory.createPushButton(buttonComp, Messages.LocationSelector_FileSystem, null); 
        btnBrowseSystem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                browseFileSystem();
            }
        });
        btnBrowseVariables = SWTFactory.createPushButton(buttonComp, Messages.LocationSelector_Variables, null); 
        btnBrowseVariables.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                browseVariables();
            }
        });
        
    }

    private String getLocationTxt() {
        return text.getText().trim();
    }

    private void textModified() {
        if (!cbUseDefLocation.getSelection()) {
            otherLocation = text.getText();
        }
        if (actionListener != null) {
            actionListener.actionPerformed(null);
        }
    }
    
    private void handleCbUseDefLocation() {
        boolean def = cbUseDefLocation.getSelection();
        text.setEditable(!def);
        text.setText(def ? defLocation : otherLocation);
        reenableAll();
        if (actionListener != null) {
            actionListener.actionPerformed(null);
        }
    }

    private void reenableAll() {
        boolean def = cbUseDefLocation.getSelection();
        cbUseDefLocation.setEnabled(isEnabled);
        link.setEnabled(isEnabled);
        text.setEnabled(isEnabled);
        btnBrowseWorkspace.setEnabled(isEnabled && !def);
        btnBrowseSystem.setEnabled(isEnabled && !def);
        btnBrowseVariables.setEnabled(isEnabled && !def);
        link.setText(fakeDefLocation && def ? linkTxtFake : linkTxt);
    }

    /**
     * Returns the selected workspace container,or <code>null</code>
     */
    protected IContainer getContainer() {
        String path = getLocationTxt();
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
                } catch (CoreException e) {
                }
            } else {
                res = root.findMember(path);
            }
            if (res instanceof IContainer) {
                return (IContainer) res;
            }
        }
        return (browseProject != null) ? browseProject : ResourcesPlugin.getWorkspace().getRoot();
    }
    
    private void browseWorkspace() {
        if (fileMode) {
            WorkbenchContentProvider wcp;
            if (StringUtils.isBlank(fileBrowseExtension)) {
                wcp = new WorkbenchContentProvider();
            } else {
                wcp = new WorkbenchContentProvider() {
                    @ Override
                    public Object[] getChildren(Object element) {
                        Object[] arr = super.getChildren(element);
                        ArrayList<Object> al = new ArrayList<Object>();
                        for (Object o : arr) {
                            if (o instanceof IFile) {
                                if (((IFile)o).getName().toLowerCase().endsWith(fileBrowseExtension.toLowerCase())) {
                                    al.add(o);
                                }
                            } else {
                                al.add(o);
                            }
                        }
                        return al.toArray();
                    }
                };
            }
            ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(text.getShell(), new WorkbenchLabelProvider(), wcp);
            dialog.setTitle(Messages.LocationSelector_SelectAFile); 
            dialog.setMessage(Messages.LocationSelector_ChooseLocationRelToWspace+':'); 
            if (browseProject != null) {
                dialog.setInput(browseProject);
            } else {
                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
            }
            dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
            dialog.addFilter(new SpecialFoldersFilter(false));
            if (dialog.open() == IDialogConstants.OK_ID) {
                IResource resource = (IResource) dialog.getFirstResult();
                if(resource != null) {
                    String arg = resource.getFullPath().toString();
                    String fileLoc = VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression("workspace_loc", arg); //$NON-NLS-1$
                    text.setText(fileLoc);
                }
            }
        } else {
            /////////////////
            WorkbenchContentProvider wcp;
            wcp = new WorkbenchContentProvider();
            ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(text.getShell(), new WorkbenchLabelProvider(), wcp);
            dialog.setTitle(Messages.LocationSelector_SelectAFile); 
            dialog.setMessage(Messages.LocationSelector_ChooseLocationRelToWspace+':'); 
            if (browseProject != null) {
                dialog.setInput(browseProject);
            } else {
                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
            }
            dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
            dialog.addFilter(new SpecialFoldersFilter(true));
            if (dialog.open() == Window.OK) {
                Object o = dialog.getFirstResult();
                if (o instanceof IFolder) { 
                    String arg = ((IFolder)o).getProjectRelativePath().makeRelative().toString();
                    String fileLoc = VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression("workspace_loc", arg); //$NON-NLS-1$
                    text.setText(fileLoc);
                }
            }
        }
    }

    private void browseFileSystem() {
        String result = null;
        if (fileMode) {
            result = SWTFactory.browseFile(text.getShell(), false,
                                           Messages.LocationSelector_SelectAFile, 
                                           new String[]{"*" + (fileBrowseExtension == null ? "" : fileBrowseExtension)}, //$NON-NLS-1$ //$NON-NLS-2$
                                           fileBrowsePath);
        } else {
            DirectoryDialog dialog = new DirectoryDialog(text.getShell());
            dialog.setFilterPath(getLocationTxt());
            dialog.setText(Messages.LocationSelector_DirSelection);
            dialog.setMessage(Messages.LocationSelector_ChooseADir+':');
            result = dialog.open();
        }
        if (result != null)
            text.setText(result);
    }
    
    private void browseVariables() {
        StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(text.getShell());
        if (dialog.open() == Window.OK)
            text.insert(dialog.getVariableExpression());
    }
            
    private void showPath() {
        try{
            String path = VariableUtils.performStringSubstitution(getLocationTxt(), false);
            File f = new File(path);
            if (fileMode) {
                if (f.isFile()) {
                    try {
                        ProcessBuilder builder = new ProcessBuilder(new String[] { 
                                "explorer.exe",  //$NON-NLS-1$
                                "/select,\"" + f.getCanonicalPath() + "\""});  //$NON-NLS-1$ //$NON-NLS-2$
                        builder.start();
                    } catch (IOException exc) {
                        // Oops. can't show (or it is not windows?)
                        // Show in messagebox:
                        SWTFactory.ShowMessageBox(null, Messages.LocationSelector_SelectedFile, f.getCanonicalPath(), SWT.OK);
                    }
                } else {
                    MessageDialog.openWarning(text.getShell(), Messages.LocationSelector_SelectFile, Messages.LocationSelector_FileNotFound);
                }
            } else {
                if (f.isDirectory()) {
                    Program.launch(f.getCanonicalPath());
                } else {
                    MessageDialog.openWarning(text.getShell(), Messages.LocationSelector_OpenDir, Messages.LocationSelector_DirNotFound);
                }
            }
        } catch (Exception ex) {
            MessageDialog.openWarning(text.getShell(), Messages.LocationSelector_OpenLocation, Messages.LocationSelector_ErrorOccured + ": " + ex); //$NON-NLS-1$
        }
    }

}
