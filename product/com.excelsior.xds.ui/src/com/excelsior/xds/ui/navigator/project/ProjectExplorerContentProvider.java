package com.excelsior.xds.ui.navigator.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;

import com.excelsior.xds.core.model.IElementChangedListener;
import com.excelsior.xds.core.model.IXdsCompilationUnit;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsDdgScriptUnitFile;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsExternalCompilationUnit;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsProjectFile;
import com.excelsior.xds.core.model.IXdsResource;
import com.excelsior.xds.core.model.IXdsSdkLibraryContainer;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.ui.XdsPlugin;
import com.excelsior.xds.ui.utils.SingleUiUpdateRunnable;

public class ProjectExplorerContentProvider implements ITreeContentProvider
                                                     , IPipelinedTreeContentProvider
                                                     , IElementChangedListener
                                                     , ILazyTreeContentProvider
{
    protected static final Object[] NO_CHILDREN= new Object[0];
    private TreeViewer treeViewer;
    private IExtensionStateModel stateModel; 
    private ExtensionStateModelAccessor stateModelAccess;
    
    private final AtomicBoolean isUpdateRequestPending = new AtomicBoolean(false);
    
    @Override
    public void dispose() {
        XdsModelManager.getModel().removeElementChangedListener(this);
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        XdsModelManager.getModel().addElementChangedListener(this);
        treeViewer = (TreeViewer)viewer;
    }

    @Override
    public Object[] getElements(Object parent) {
        return getChildren(parent);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (!exists(parentElement)) return NO_CHILDREN;
        
        if (parentElement instanceof IWorkspaceRoot) {
            return XdsModelManager.getModel().getXdsProjects().toArray();
        }
        else if (parentElement instanceof IProject) {
            IProject parent = (IProject)parentElement;
            IXdsProject project = XdsModelManager.getModel().getXdsProjectBy(parent);
            if (project == null)  return NO_CHILDREN; // project was deleted
            return filterXdsElements(project.getChildren()).toArray();
        }
        else if (parentElement instanceof IXdsExternalCompilationUnit) {
            return NO_CHILDREN;
        }
        else if (parentElement instanceof IXdsContainer) {
            return filterXdsElements(((IXdsContainer)parentElement).getChildren()).toArray();
        }
        
        return NO_CHILDREN;
    }
    
    private List<Object> filterXdsElements(Collection<IXdsElement> xdsElements) {
        if (xdsElements == null) return null;
        List<Object> objects = new ArrayList<Object>();
        for (IXdsElement xdsElement : xdsElements) {
            if (xdsElement instanceof IXdsExternalCompilationUnit) {
                objects.add(xdsElement);
            }
            else if (xdsElement instanceof IXdsSdkLibraryContainer) {
                objects.add(xdsElement);
            }
            else if (xdsElement instanceof IXdsCompilationUnit) {
            	filterXdsElement(objects, xdsElement);
            }
            else if (xdsElement instanceof IXdsContainer) {
            	objects.add(xdsElement);
            }
            else if ( xdsElement instanceof IXdsResource ||
            		xdsElement instanceof IXdsProjectFile || xdsElement instanceof IXdsDdgScriptUnitFile) {
            	filterXdsElement(objects, xdsElement);
            }
        }
        return objects;
    }

	private void filterXdsElement(List<Object> objects, IXdsElement xdsElement) {
		boolean isAdd = stateModelAccess.isShowResources();
		if (!isAdd) {
			Sdk sdk = xdsElement.getXdsProject().getXdsProjectSettings().getProjectSdk();
			if (isSdkSettingsAllowXdsElementInNavigator(sdk, xdsElement)) {
				isAdd = true;
			}
		}
		
		if (isAdd) {
			objects.add(xdsElement);
		}
	}
	
	private String getNameWithExtension(IXdsElement xdsElement) {
		if (xdsElement instanceof IXdsResource) {
			IXdsResource xdsResource = (IXdsResource) xdsElement;
			IResource resource = xdsResource.getResource();
			return resource.getName();
		}
		else if (xdsElement instanceof IXdsCompilationUnit) {
			IXdsCompilationUnit compilationUnit = (IXdsCompilationUnit) xdsElement;
			return compilationUnit.getAbsoluteFile().fetchInfo().getName();
		}
		
		return null;
	}
    
    /**
     * Tests whether primary extension defined in the SDK permits resource to be shown in navigator tree
     * @param sdk
     * @param resource
     * @return
     */
    private boolean isSdkSettingsAllowXdsElementInNavigator( Sdk sdk, IXdsElement  xdsElement) {
    	String fullPath = getNameWithExtension(xdsElement);
    	if (fullPath == null) {
    		return false;
    	}
    	String[] extensions;
    	if (sdk != null) {
    		extensions = sdk.getPrimaryFileExtensionsAsArray();
    	}
    	else {
            extensions = Sdk.getDefaultPrimaryFileExtensionsAsArray();
    	}
    	String resourceName = StringUtils.lowerCase(fullPath);
		if (ArrayUtils.indexOf(extensions, FilenameUtils.getExtension(resourceName))> -1) {
			return true;
    	}
    	
    	return false;
    }
    
    @Override
    public Object getParent(Object element) {
        if (element instanceof IResource) {
            return XdsModelManager.getModel().getParentXdsElement((IResource)element);
        }
        else if (element instanceof IXdsProject) {
            return ResourceUtils.getWorkspaceRoot();
        }
        else if (element instanceof IXdsElement) {
            IXdsElement xdsElement = (IXdsElement)element;
            IXdsElement parent = xdsElement.getParent();
            if (parent instanceof IXdsProject) {
                return ((IXdsProject)parent).getProject();
            }
            return parent;
            
        }
        
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof IResource) {
            return !ResourceUtils.getImmediateChildren((IResource)element).isEmpty();
        }
        else if (element instanceof IXdsCompilationUnit) {
            return false;
        }
        else if (element instanceof IXdsContainer) {
            IXdsContainer xdsElement = (IXdsContainer)element;
            return !CollectionUtils.isEmpty(filterXdsElements(xdsElement.getChildren()));
        }
        return false;
    }

    protected boolean exists(Object element) {
        if (element == null) {
            return false;
        }
        if (element instanceof IResource) {
            return ((IResource)element).exists();
        }
        return true;
    }

    @Override
    public void init(ICommonContentExtensionSite commonContentExtensionSite) {
    	this.stateModel = commonContentExtensionSite.getExtensionStateModel();
    	this.stateModelAccess = new ExtensionStateModelAccessor(stateModel);
    }
    
    @Override
    public void restoreState(IMemento memento) {
    	IPreferenceStore store = XdsPlugin.getDefault().getPreferenceStore();
    	stateModelAccess.setShowResources(store.getBoolean(IExtensionStateConstants.IS_SHOW_RESOURCES));
    }

    @Override
    public void saveState(IMemento memento) {
    	IPreferenceStore store = XdsPlugin.getDefault().getPreferenceStore();
    	store.setValue(IExtensionStateConstants.IS_SHOW_RESOURCES, stateModelAccess.isShowResources());
    }

    @Override
    public void getPipelinedChildren(Object parent, @SuppressWarnings("rawtypes") Set currentChildren) {
        customize(getChildren(parent), currentChildren);
    }

    @Override
    public void getPipelinedElements(Object input, @SuppressWarnings("rawtypes") Set currentElements) {
        customize(getElements(input), currentElements);
    }

    @Override
    public Object getPipelinedParent(Object object, Object suggestedParent) {
        return getParent(object);
    }

    @Override
    public PipelinedShapeModification interceptAdd(
            PipelinedShapeModification anAddModification) {
    	anAddModification.getChildren().clear();
        return anAddModification;
    }

    @Override
    public PipelinedShapeModification interceptRemove(
            PipelinedShapeModification aRemoveModification) {
        return aRemoveModification;
    }

    @Override
    public boolean interceptRefresh(
            PipelinedViewerUpdate aRefreshSynchronization) {
        return false;
    }

    @Override
    public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
        return false;
    }
    
    @SuppressWarnings("unchecked")
	private synchronized void customize(Object[] xdsElements, @SuppressWarnings("rawtypes") Set proposedChildren) {
        Set<Object> newChildren = new HashSet<>();
        for (int i= 0; i < xdsElements.length; i++) {
            Object element= xdsElements[i];
            if (element instanceof IXdsResource) {
            	IXdsResource cElement = (IXdsResource)element;
                IResource resource = cElement.getResource();
                if (resource != null) {
                	newChildren.remove(resource);
                }
                newChildren.add(element);
            } else if (element != null) {
            	newChildren.add(element);
            }
        }
        proposedChildren.clear();
        proposedChildren.addAll(newChildren);
    }

    @Override
    public void elementChanged() {
        if (isUpdateRequestPending.compareAndSet(false, true)) {
        	Display.getDefault().asyncExec(new SingleUiUpdateRunnable(isUpdateRequestPending) {
				@Override
				protected void doRun() {
    				if (treeViewer != null && !treeViewer.getControl().isDisposed()) {
    					treeViewer.getControl().setRedraw(false);
    					treeViewer.refresh();
    					treeViewer.getControl().setRedraw(true);
    				}
				}
			});
        }
    }

    @Override
    public void updateElement(Object parent, int index) {
    }

    @Override
    public void updateChildCount(Object element, int currentChildCount) {
    }
    
}
