package com.excelsior.xds.ui.dialogs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.ui.commons.utils.IconUtils;
import com.excelsior.xds.ui.images.ImageUtils;

public class SelectModulaSourceFolderDialog extends FilteredItemsSelectionDialog {
    
    private List<IResource> moduleFilePathes = new ArrayList<IResource>();
    
    public static interface IResourceFilter {
    	public boolean showItem(IResource resource);
    }

    // Select items with the given (lowercase) extensions
	public SelectModulaSourceFolderDialog(Shell shell, String dlgTitle, IContainer rootContainer, final Set<String> excludedFolders) {
		super(shell);
		this.setTitle(dlgTitle);
		IResourceFilter flt = new IResourceFilter() {
			@Override
			public boolean showItem(IResource resource) {
			    if (!(resource instanceof IFolder)) {
			        return false;
			    }
			    
			    IFolder f = ResourceUtils.getProjectLevelContainer(resource);
			    if (XdsModelManager.getModel().getXdsProjectBy(f) == null ||
			        excludedFolders.contains(f.getProjectRelativePath().toPortableString())) 
			    {
			        return false;
			    }
			    return true;
			}
		};
        init(rootContainer, flt);
	}

    // Select items filtered by iFilter
	public SelectModulaSourceFolderDialog(Shell shell, IProject xdsProject, IResourceFilter iFilter) {
        super(shell);
        init(xdsProject, iFilter);
	}
	
	private void init(IContainer rootContainer, final IResourceFilter iFilter) {
        
        try {
            if (!(rootContainer instanceof IWorkspaceRoot)) {
                moduleFilePathes.add(rootContainer);
            }
            else {
                moduleFilePathes.addAll(ProjectUtils.getXdsProjects());
            }
            rootContainer.accept(new IResourceVisitor() {
                @Override
                public boolean visit(IResource resource) throws CoreException {
                    if (iFilter.showItem(resource)){
                        moduleFilePathes.add(resource);
                    }
                    
                    return true;
                }
            });
        } catch (CoreException e) {
            LogHelper.logError(e);
        }
        
        ILabelProvider ilp = new ILabelProvider() {
            
            @Override
            public void removeListener(ILabelProviderListener listener) {
            }
            
            @Override
            public boolean isLabelProperty(Object element, String property) {
                return false;
            }
            
            @Override
            public void dispose() {
            }
            
            @Override
            public void addListener(ILabelProviderListener listener) {
                
            }
            
            @Override
            public String getText(Object element) {
                return getElementName(element);
            }
            
            /**
             * NOTE: seems that this big code is universal and may be used to get 
             *       icons for all kings of resources.  
             */
            @Override
            public Image getImage(Object o) {
                Image res = IconUtils.getImage(o);
                return res == null ? ImageUtils.getImage(ImageUtils.PACKAGE_FRAGMENT_IMAGE_NAME) : res;
            }

        };
        
        setListLabelProvider(ilp);
        setDetailsLabelProvider(ilp);
        setInitialPattern("**"); //$NON-NLS-1$
	}
	
	public IContainer getResultFolder() {
	    return (IContainer)getResult()[0];
	}
    
    public String getResultAsRelativePath() {
        IResource result = (IResource)getResult()[0];
        return getRelativePath(result);
    }

    private String getRelativePath(IResource result) {
        return result.getFullPath().toPortableString();
    }

    @Override
    protected Control createExtendedContentArea(Composite parent) {
        return null;
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        return new DialogSettings("XDS"); //$NON-NLS-1$
    }

    @Override
    protected IStatus validateItem(Object item) {
        return Status.OK_STATUS;
    }

    @Override
    protected ItemsFilter createFilter() {
        return new ItemsFilter(){
            @Override
            public boolean matchItem(Object item) {
                String fsName = getResourceName(item);
                if (patternMatcher.matches(fsName)) {
                    return true;
                }
                String pattern = patternMatcher.getPattern();
                if (StringUtils.isEmpty(pattern)) {
                    return true;
                }
                return fsName.toLowerCase().contains(pattern.toLowerCase());
            }

            @Override
            public boolean isConsistentItem(Object item) {
                IResource resource = (IResource)item;
                return !resource.isPhantom();
            }
        };
    }
    
    private String getResourceName(Object item) {
        if (item == null) return ""; //$NON-NLS-1$
        IResource resource = (IResource)item;
        return getRelativePath(resource);
    }

    @Override
    protected Comparator<IResource> getItemsComparator() {
        return new Comparator<IResource>(){
            @Override
            public int compare(IResource o1, IResource o2) {
                return getResourceName(o1).compareToIgnoreCase(getResourceName(o2));
            }
        };
    }

    @Override
    protected void fillContentProvider(AbstractContentProvider contentProvider,
            ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
            throws CoreException {
        for (IResource res : moduleFilePathes) {
            contentProvider.add(res, itemsFilter);
        }
    }

    @Override
    public String getElementName(Object item) {
        String s = getResourceName(item);
        if (s.startsWith("/")) { //$NON-NLS-1$
            s = s.substring(1);
        }
        return s;
    }



    @Override
    public void create() {
        super.create();
        
        this.getShell().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
            }
        });
    }
}
