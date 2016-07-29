package com.excelsior.xds.core.model.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.excelsior.xds.builder.buildsettings.BuildSettingsCache;
import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsExternalDependenciesContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsSdkLibraryContainer;
import com.excelsior.xds.core.model.XdsProjectConfiguration;
import com.excelsior.xds.core.model.internal.nls.Messages;
import com.excelsior.xds.core.model.utils.XdsElementUtils;
import com.excelsior.xds.core.project.SpecialFolderNames;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.utils.JavaUtils;
import com.excelsior.xds.core.utils.Lambdas;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;

public class XdsProject extends XdsFolderContainer implements IXdsProject {
    private IXdsExternalDependenciesContainer externalSourcesFolder;
    private IXdsSdkLibraryContainer sdkLibraryContainer; 
    
    private XdsModel model;
	public XdsProject(IProject project, XdsModel model) {
	    super(null, project, null);
	    this.model = model;
	}
	
	@Override
	protected Collection<String> getRelativeFolderPathesToSkip() {
		return SpecialFolderNames.getIgnoredSpecialFolderNames();
	}

	@Override
	public XdsProject getXdsProject() {
		return this;
	}

	public synchronized void setModel(XdsModel model) {
		this.model = model;
	}
	
	
	@Override
	public XdsModel getModel() {
		return model;
	}

	@Override
    public XdsProjectSettings getXdsProjectSettings() {
		return XdsProjectSettingsManager.getXdsProjectSettings(getProject());
    }
	
    @Override
    public XdsProjectConfiguration getProjectConfiguration() {
        return new XdsProjectConfiguration(this.getProject());
    }
	
	public IProject getProject() {
        return (IProject)getResource();
    }
	
    public void syncRefreshLocal() {
        try {
            	final CountDownLatch latch = new CountDownLatch(1);
                getProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor() {
                    @Override
                    public void done() {
                        latch.countDown();
                    }
                });
                latch.await();
        } catch (CoreException e) {
            LogHelper.logError(e);
        } catch (InterruptedException e) {
        	LogHelper.logError(e);
		}
    }
    
    @Override
    public String getElementName() {
        return getProject().getName();
    }
    
    @Override
    public synchronized List<IXdsElement> getChildren() {
        if (children == null) {
            // TODO : commented out code is used when project location is used as Linked Folder.
//            final String projectRootDir = getXdsProjectSettings().getProjectRootDir();
//            String relativeProjectRootDir = ResourceUtils.getRelativePath(getProject(), projectRootDir);
//            final IResource root = getProject().getFolder(relativeProjectRootDir);
//            buildChildren(root);
            buildChildren(getProject(), getResourceFilter() );
            buildExternalDependenciesChildren();
            buildSdkLibraryChildren();
        }
        return CollectionsUtils.unmodifiableArrayList(children);
    }
    
    /* (non-Javadoc)
     * @see com.excelsior.xds.core.model.internal.XdsFolderContainer#getResourceFilter()
     */
    protected Predicate<IResource> getResourceFilter() {
    	BuildSettings buildSettings = BuildSettingsCache.createBuildSettings(getProject());
    	Predicate<File> pred = newExternalDependencyDirectoryPredicate(buildSettings);
    	Sdk sdk = buildSettings.getSdk();

    	// TODO : modify this predicate to remove getRelativeFolderPathesToSkip methods
    	return r -> {
    		// accept all resources outside SpecialFolderNames.VIRTUAL_MOUNT_ROOT_DIR_NAME
    		if (!ResourceUtils.isInsideFolder(SpecialFolderNames.VIRTUAL_MOUNT_ROOT_DIR_NAME, r)) {
    			return true;
    		}
    		
    		if (!JavaUtils.isOneOf(r, IContainer.class) || r.isVirtual()) { // accept all virtual folders and leaf resources 
    			return true;
    		}
    		
    		// at this point we are testing non-virtual folder under the .mnt directory. 
    		
        	File resourceFile = ResourceUtils.getAbsoluteFile(r);
        	// skip external dependencies directories - they will be handled by the buildExternalDependenciesChildren
        	if (pred.test(resourceFile)) {  
        		return false;
        	}
        	
        	// skip library definition path directory - this will be handled by buildSdkLibraryChildren method
        	if (sdk != null && sdk.getLibraryDefinitionsPath() != null) {
        		Path libDefPath = new Path(sdk.getLibraryDefinitionsPath());
        		if (libDefPath.isPrefixOf(new Path(resourceFile.getAbsolutePath()))) {
        			return false;
        		}
        	}
        	
    		return true;
    	};
    }
    
    private synchronized void buildExternalDependenciesChildren() {
    	BuildSettings buildSettings = BuildSettingsCache.createBuildSettings(getProject());
    	Predicate<File> pred = newExternalDependencyDirectoryPredicate(buildSettings);
    	Set<File> lookupDirs = buildSettings.getLookupDirs().stream().filter(pred).collect(Collectors.toSet());
    	if (!lookupDirs.isEmpty()) {
    		boolean isHasChildren = false;
    		XdsExternalDependenciesContainer externalSourcesFolder = new XdsExternalDependenciesContainer(this, Messages.XdsProject_ExternalDependencies, null, this, new ArrayList<IXdsElement>());
    		Map<String, XdsVirtualContainer> parentPathToContainer = createDirectoryTree(externalSourcesFolder, lookupDirs);
    		for (File lookupDir : lookupDirs) {
    			IResource r = ResourceUtils.getResource(getProject(), lookupDir.toURI());
				if (r != null){
					XdsVirtualContainer lookupDirContainer = parentPathToContainer.get(lookupDir.getParent());
					XdsFolderContainer folder = new XdsFolderContainer(getXdsProject(), r, lookupDirContainer) {
						@Override
						protected Predicate<IResource> getResourceFilter() {
							return Lambdas.TRUE();
						}
					};
					lookupDirContainer.addChild(folder);
					model.putWorkspaceXdsElement(r, folder);
					isHasChildren = true;
				}
			}
    		if (isHasChildren) {
    			this.externalSourcesFolder = externalSourcesFolder; 
    			children.add(externalSourcesFolder);
    		}
    	}
    }

	/**
	 * Tests whether the given {@link File} denotes the valid external directory.
	 * @param buildSettings
	 * @return
	 */
	private Predicate<File> newExternalDependencyDirectoryPredicate(BuildSettings buildSettings) {
		Path sdkHomePath = getSdkHomePath(buildSettings);
    	Path projectPath = new Path(ResourceUtils.getAbsolutePath(getProject()));
    	return d -> {
    		if (d == null) {
    			return true; // accept virtual folders
    		}
    		Path dirPath = new Path(d.getAbsolutePath());
			boolean passes = d.isDirectory() && !projectPath.isPrefixOf(dirPath) && !dirPath.isPrefixOf(projectPath);
			if (passes && sdkHomePath != null){ // it is OK for sdkHomePath to be null, when it is not specified in settings
				passes &= !sdkHomePath.isPrefixOf(dirPath);
			}
			return passes;
    	};
	}
    
    Path getSdkHomePath(BuildSettings buildSettings) {
    	Sdk sdk = buildSettings.getSdk();
    	if (sdk == null){
    		return null;
    	}
		String sdkHomePath = sdk.getSdkHomePath();
    	if (sdkHomePath == null) {
    		return null;
    	}
		return new Path(sdkHomePath);
    }
    
    private synchronized void buildSdkLibraryChildren() {
    	XdsSdkLibraryContainer sdkLibraryContrainer = new XdsSdkLibraryContainer(this, Messages.XdsProject_SdkLibrary, null, this, new ArrayList<IXdsElement>());
    	
    	XdsProjectSettings projectSettings = XdsProjectSettingsManager.getXdsProjectSettings(getProject());
		Sdk sdk = projectSettings.getProjectSdk();
		if (sdk != null && sdk.getLibraryDefinitionsPath() != null && sdk.getSdkHomePath() != null) {
			File libDefFile = new File(sdk.getLibraryDefinitionsPath());
			File sdkHomeDir = new File(sdk.getSdkHomePath());
			if (libDefFile.isDirectory() && sdkHomeDir.isDirectory()) {
				IResource r = ResourceUtils.getResource(getProject(), libDefFile.toURI());
				if (r != null){
					XdsVirtualContainer sdkContainer = new XdsVirtualContainer(getXdsProject(), sdkHomeDir.getAbsolutePath(), sdkHomeDir.getAbsolutePath(), sdkLibraryContrainer, new ArrayList<IXdsElement>());
					XdsFolderContainer folder = new XdsFolderContainer(getXdsProject(), r, sdkContainer) {
						@Override
						protected Predicate<IResource> getResourceFilter() {
							return Lambdas.TRUE();
						}
					};
					sdkContainer.addChild(folder);
					model.putWorkspaceXdsElement(r, folder);
					sdkLibraryContrainer.addChild(sdkContainer);
				}
			}
		}
    	
		this.sdkLibraryContainer = sdkLibraryContrainer; 
        children.add(sdkLibraryContrainer);
    }
    
    private Map<String, XdsVirtualContainer> createDirectoryTree(XdsVirtualContainer externalSourcesFolder, Set<File> dirs) {
    	Map<String, XdsVirtualContainer> parentPathToContainer = new HashMap<String, XdsVirtualContainer>();
    	// 1) iterate over all given pathes (they are absolute pathes of Modula sources), create their parent and grand-parent container
    	for (File dir : dirs) {
    		String parentPath = dir.getAbsolutePath();
			
			String granParentPath = ResourceUtils.getAbsoluteParentPathAsInFS(parentPath);
			if (granParentPath != null) {
				XdsVirtualContainer grandParentContainer = parentPathToContainer.get(granParentPath);
				if (grandParentContainer == null) {
					grandParentContainer = new XdsVirtualContainer(this, getGoodLookingPath(granParentPath), granParentPath, this, new ArrayList<IXdsElement>());
					parentPathToContainer.put(granParentPath, grandParentContainer);
				}
			}
    	}
    	
    	// 2) create array of parentPathes, sorted by length. This gives an list with order like C:\, C:\Dir1, C:\Dir2\Dir2
    	// So the parents go first, children last.
    	List<String> parentPathes = new ArrayList<String>(parentPathToContainer.keySet());
		Collections.sort(parentPathes, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.length() - o2.length();
			}
		});
		
		// roots are topmost containers
		TreeMap<String, XdsVirtualContainer> roots = new TreeMap<String, XdsVirtualContainer>();
		
		// 3) if container have no parent above him, it is root
		// otherwise - create all intermediate containers between container and its grandparent
		for (String parentPath : parentPathes) {
			XdsVirtualContainer container = parentPathToContainer.get(parentPath);
			XdsVirtualContainer potentialRoot = getTopmostParentAndAddChildren(container, parentPathToContainer);
			if (!isHasGrandParent(potentialRoot, parentPathToContainer)) { // it is root indeed
				roots.put(potentialRoot.getPath(), potentialRoot);
			}
			else {
				String tempParentPath = getParent(potentialRoot.getPath());
				XdsVirtualContainer tempContainer = potentialRoot;
				while(!parentPathToContainer.containsKey(tempParentPath)) {
					XdsVirtualContainer tempParentContainer = new XdsVirtualContainer(this, getGoodLookingPath(tempParentPath), tempParentPath, this, new ArrayList<IXdsElement>());
					tempParentContainer.addChild(tempContainer);
					tempContainer = tempParentContainer;
					tempParentPath = getParent(tempParentPath);
				}
				
				XdsVirtualContainer tempParentContainer = parentPathToContainer.get(tempParentPath);
				tempParentContainer.addChild(tempContainer);
			}
		}
		
		for (Entry<String, XdsVirtualContainer> entry : roots.entrySet()) {
			XdsVirtualContainer rootContainer = entry.getValue();
			rootContainer.setElementName(rootContainer.getPath()); // roots must expose full path as caption
			externalSourcesFolder.addChild(rootContainer);
			rootContainer.setParent(externalSourcesFolder);
		}
		
		return parentPathToContainer;
    }
    
    
    private static XdsVirtualContainer getTopmostParentAndAddChildren(XdsVirtualContainer container, Map<String, XdsVirtualContainer> parentPathToContainer ) {
		String path = container.getPath();
		String parentPath = ResourceUtils.getAbsoluteParentPathAsInFS(path);
		XdsVirtualContainer parent = container;
		while(parentPath != null && parentPathToContainer.containsKey(parentPath)) {
			parent = parentPathToContainer.get(parentPath);
			if (!foundInChildren(parent, container)) {
				parent.addChild(container);
			}
			
			parentPath = ResourceUtils.getAbsoluteParentPathAsInFS(parentPath);
			container = parent;
		}
		return parent;
	}
    
    private static boolean isHasGrandParent(XdsVirtualContainer container, Map<String, XdsVirtualContainer> parentPathToContainer) {
		String parentPath = getParent(container.getPath());
		while(parentPath != null){
			if (parentPathToContainer.containsKey(parentPath)) {
				break;
			}
			parentPath = getParent(parentPath);
		}
		
		return parentPath != null;
	}
    
    private static String getParent(String path) {
		return ResourceUtils.getAbsoluteParentPathAsInFS(path); 
	}
    
    private static boolean foundInChildren(XdsVirtualContainer parent, XdsVirtualContainer expectedChild) {
    	Collection<IXdsElement> children = parent.getChildren();
		for (IXdsElement xdsElement : children) {
			if (xdsElement instanceof XdsVirtualContainer) {
				XdsVirtualContainer container = (XdsVirtualContainer) xdsElement;
				if (ResourceUtils.equalsPathesAsInFS(expectedChild.getPath(), container.getPath())) {
					return true;
				}
			}
		}
		return false;
    }
	
    private static String getGoodLookingPath(String absolutePath) {
		return FilenameUtils.getName(absolutePath);
	}

    @Override
    public synchronized void refreshExternalDependencies() {
        Assert.isNotNull(children);
        
        removeExternalDependenciesNode();
        buildExternalDependenciesChildren();
    }
    
    @Override
	public synchronized void refreshSdkLibrary() {
    	Assert.isNotNull(children);
        
    	removeSdkLibraryNode();
    	buildSdkLibraryChildren();
	}
    
    private synchronized void removeExternalDependenciesNode() {
    	if (externalSourcesFolder != null) {
    		children.remove(externalSourcesFolder);
    	}
    }
    
    private synchronized void removeSdkLibraryNode() {
    	if (sdkLibraryContainer != null) {
    		children.remove(sdkLibraryContainer);
    	}
    }

    @Override
    public synchronized IXdsExternalDependenciesContainer getXdsExternalDependenciesContainer() {
        return externalSourcesFolder;
    }
    
    @Override
    public synchronized IXdsSdkLibraryContainer getXdsSdkLibraryContainer() {
        return sdkLibraryContainer;
    }

	@Override
	public synchronized Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (ResourceMapping.class.equals(adapter)) {
			return XdsElementUtils.createResourceMappingFrom(children, this);
		}
		
		return super.getAdapter(adapter);
	}
}
