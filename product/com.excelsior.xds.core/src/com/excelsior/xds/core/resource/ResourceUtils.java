package com.excelsior.xds.core.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;

import com.excelsior.xds.core.XdsCorePlugin;
import com.excelsior.xds.core.exceptions.ExceptionHelper;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.natures.NatureIdRegistry;
import com.excelsior.xds.core.project.NatureUtils;
import com.excelsior.xds.core.project.SpecialFolderNames;
import com.excelsior.xds.core.utils.IBaseClosure;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public final class ResourceUtils
{
	private static final String REGEX_RESOURCE_FILTER_ID = "org.eclipse.core.resources.regexFilterMatcher"; //$NON-NLS-1$

	/**
	 * Only static method are allowed - this class should have no state
	 */
	private ResourceUtils() {
	}
    /**
     * @param path - valid path in FS - either relative or absolute
     * @return system dependent form of path (absolute) - exactly as in the FS. This solves Windows problems, 
     * that paths in different casings maps to the same FS object. Please see {@link File#getCanonicalPath()}  for more info.
     */
    public static String getAbsolutePathAsInFS(String path) {
    	if (path == null) {
    		return null;
    	}
        File absolutePathAsInFS = getAbsolutePathAsInFS(new File(path));
        if (absolutePathAsInFS != null) {
        	return absolutePathAsInFS.getPath();
        }
        else{
        	return null;
        }
    }
    
    public static File getAbsolutePathAsInFS(File path) {
    	if (path == null) {
    		return null;
    	}
        try {
            return path.getCanonicalFile();
        } catch (IOException e) {
            LogHelper.logError(e);
        }
        return null;
    }
    
    public static String toAbsolutePathString(File file) {
    	if (file == null) {
    		return null;
    	}
    	
    	return file.getAbsolutePath();
    }
    
    public static String getAbsolutePath(File file) {
    	File absoluteFile = ResourceUtils.getAbsolutePathAsInFS(file);
    	if (absoluteFile == null) {
    		return null;
    	}
		return absoluteFile.getAbsolutePath();
    }
    
    public static String getAbsolutePath(IFileStore fileStore) throws CoreException {
    	return getAbsolutePath(fileStore, new NullProgressMonitor());
    }
    
    /**
     * Works only for the file:// based file stores.
     * @param fileStore
     * @param monitor
     * @return
     * @throws CoreException
     */
    public static String getAbsolutePath(IFileStore fileStore, IProgressMonitor monitor) throws CoreException {
    	if (fileStore == null) {
    		return null;
    	}
    	URI uri = fileStore.toURI();
    	if (!EFS.SCHEME_FILE.equals(uri.getScheme())) {
    		return null;
    	}
		return getAbsolutePath(new File(uri));
    }
    
    public static String toString(IFileStore fileStore) throws CoreException {
    	return toString(fileStore, new NullProgressMonitor());
    }
    
    public static String toString(IFileStore fileStore, IProgressMonitor monitor) throws CoreException {
    	try {
    		if (fileStore.fetchInfo().exists()) {
    			try(InputStream stream = fileStore.openInputStream(EFS.NONE , monitor)){
    				return IOUtils.toString(stream);
    			}
    		}
		} catch (IOException e) {
			ExceptionHelper.rethrowAsCoreException(e);
		}
    	return null;
    }
    
    
    /**
     * Compares two path string, by converting to canonical filenames in FS and then "equals" them 
     * @param path1
     * @param path2
     * @return
     */
    public static boolean equalsPathesAsInFS(String path1, String path2) {
        String absolutePathAsInFS1 = getAbsolutePathAsInFS(path1);
		String absolutePathAsInFS2 = getAbsolutePathAsInFS(path2);
		if (absolutePathAsInFS1 == null || absolutePathAsInFS2 == null) {
			return false;
		}
		return absolutePathAsInFS1.equals(absolutePathAsInFS2);
    }
    
    /**
     * Compares canonicalized (via java.io.File.getCanonicalPath) pathes. Returns false if either of pathes corresponds
     * to the non-existent file-system object 
     */
    public static boolean equalsPathesAsInFS(File path1, File path2) {
    	File absolutePathAsInFS1 = getAbsolutePathAsInFS(path1);
    	File absolutePathAsInFS2 = getAbsolutePathAsInFS(path2);
		if (absolutePathAsInFS1 == null || absolutePathAsInFS2 == null) {
			return false;
		}
		return absolutePathAsInFS1.equals(absolutePathAsInFS2);
    }
    
    /**
     * Tests IResources for equality. If either of them is null - returns null.
     * @return
     */
    public static boolean equals(IResource resource1, IResource resource2) { 
    	if (resource1 == null || resource2 == null) {
    		return false;
    	}
    	
    	return resource1.equals(resource2);
    }
    
    public static String getAbsoluteParentPathAsInFS(String path) {
    	path = new File(path).getParent();
    	if (path == null) return null;
    	return getAbsolutePathAsInFS(path);
    }
    
    public static String getAbsolutePathAsInFS(IResource resource) {
        IPath loc = resource.getLocation();
        if (loc == null){
        	return null;
        }
		return getAbsolutePathAsInFS(loc.toOSString());
    }
    
    public static QualifiedName createPersistentPropertyQualifiedName(String base, String postfix) {
    	return new QualifiedName(XdsCorePlugin.PLUGIN_ID + "." + base, FilenameUtils.separatorsToUnix(postfix));
    }
    
    public static String getAbsolutePathOfLinkedResource(IResource resource) {
    	try {
    		QualifiedName qn = ResourceUtils.createPersistentPropertyQualifiedName(IResourceAttributes.LINKED_RESOURCE_ORIGINAL_PATH_ATTR_NAME, resource.getFullPath().toOSString());
			IProject project = resource.getProject();
			if (project != null && project.isOpen()) {
				String path = project.getPersistentProperty(qn);
				if (path != null) {
					return getAbsolutePathAsInFS(path);
				}
			}
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
    	
    	return null;
    }
    
    public static void setAbsolutePathOfLinkedResourceAttribute(IResource resource, String absolutePath) {
    	QualifiedName qn = ResourceUtils.createPersistentPropertyQualifiedName(IResourceAttributes.LINKED_RESOURCE_ORIGINAL_PATH_ATTR_NAME, resource.getFullPath().toOSString());
    	try {
			resource.getProject().setPersistentProperty(qn, absolutePath);
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
    }
    
    public static void setSyntheticResourceAttribute(IResource resource) {
    	QualifiedName qn = ResourceUtils.createPersistentPropertyQualifiedName(IResourceAttributes.SYNTHETIC_RESOURCE_FLAG, resource.getFullPath().toOSString());
    	try {
			resource.getProject().setPersistentProperty(qn, Boolean.TRUE.toString());
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
    }
    
    public static String getAbsolutePath(IResource resource) {
    	if (resource == null) {
    		return null;
    	}
    	
		String path = getAbsolutePathOfLinkedResource(resource);
		if (path != null) {
			return path;
		}
        IPath location = resource.getLocation();
        if (location == null) {
        	return null;
        }
		return getAbsolutePathAsInFS(location.toOSString());
    }
    
    /**
     * @see #getExtension(IResource, boolean)
     */
    public static String getExtension(IResource resource) {
    	return getExtension(resource, false);
    }
    
    /**
     * Gets extension of the resource
     * @param resource
     * @param isLowercase if true and resource has an extension then it will be converted to lowercase
     * @return extension or null (i.e. resource passed is null  or it has no extension)
     */
    public static String getExtension(IResource resource, boolean isLowercase) {
    	if (resource == null){
    		return null;
    	}
    	String ext = resource.getFileExtension();
    	if (ext != null && isLowercase) {
    		ext = ext.toLowerCase();
    	}
    	return ext;
    }
    
    public static File getAbsoluteFile(IResource resource) {
        String absolutePath = getAbsolutePath(resource);
        if (absolutePath == null) {
        	return null;
        }
		return new File(absolutePath);
    }
    
    public static File getAbsoluteFile(IFileStore file) throws CoreException {
    	String absolutePath = getAbsolutePath(file);
        if (absolutePath == null) {
        	return null;
        }
		return new File(absolutePath);
    }
    
    public static IFileStore toFileStore(File file) {
    	if (file == null){
    		return null;
    	}
		return EFS.getLocalFileSystem().getStore(file.toURI());
	}
    
    public static IFileStore toFileStore(IResource r) {
    	if (r == null){
    		return null;
    	}
    	else if (r.getLocation() == null) {
    		return toFileStore(r.getLocationURI());
    	}
    	else {
    		return EFS.getLocalFileSystem().getStore(r.getLocation());
    	}
	}
    
    public static IFileStore toFileStore(URI uri) {
    	if (uri == null){
    		return null;
    	}
    	if (isFileScheme(uri)) {
    		return EFS.getLocalFileSystem().getStore(uri);
    	}
    	else {
    		try {
				IFileSystem fs = EFS.getFileSystem(uri.getScheme());
				return fs.getStore(uri);
			} catch (CoreException e) {
				LogHelper.logError(e);
			}
    		return null;
    	}
	}
    
    public static boolean isFileScheme(URI uri) {
    	return EFS.SCHEME_FILE.equals(uri.getScheme());
    }
    
    public static IFileSystem getFileSystem(URI uri) throws CoreException {
    	return EFS.getFileSystem(uri.getScheme());
    }
    
    public static IFileStore getSibling(IFileStore file, String name) {
    	IFileStore parent = file.getParent();
    	if (parent == null) {
    		return null;
    	}
    	
    	return parent.getChild(name);
    }
    
    public static Collection<IFileStore> convertIFilesToFileStores(Collection<IFile> ifiles) {
    	return ifiles.stream().map(ResourceUtils::toFileStore).collect(Collectors.toList());
    }
    
    public static Collection<IFileStore> convertFilesToFileStores(Collection<File> ifiles) {
    	return ifiles.stream().map(ResourceUtils::toFileStore).collect(Collectors.toList());
    }
    
    public static Collection<File> convertIFilesToFiles(Collection<IFile> ifiles) {
		Collection<File> files = Collections2.transform(ifiles, new Function<IFile, File>() {
			@Override
			public File apply(IFile ifile) {
				File file = ResourceUtils.getAbsoluteFile(ifile);
				return file;
			}
		});
		return files;
	}
    
    public static File getAbsoluteParent(IResource resource) {
        return new File(getAbsolutePath(resource)).getParentFile();
    }
    
    public static IWorkspaceRoot getWorkspaceRoot() {
        IWorkspace workspace = getWorkspace();
        if (workspace == null){
        	return null;
        }
		return workspace.getRoot();
    }
    
	/**
	 * Gets the Eclipse workspace.
	 * @return wokrspace or null (when org.eclipse.core.resources is not loaded). 
	 */
	public static IWorkspace getWorkspace() {
		try {
            return ResourcesPlugin.getWorkspace();
        } catch (IllegalStateException e) {
            return null;
        }
	}
    
    public static String getWorkspaceRootPath() {
        return ResourceUtils.getAbsolutePath(ResourceUtils.getWorkspaceRoot());
    }

    public static String getWorkspaceRelativePath(IResource resource) {
        return resource.getFullPath().toPortableString();
    }
    
    public static String getWorkspaceRelativePathOfParent(String workspaceRelativePath) {
        int idx = workspaceRelativePath.lastIndexOf('/');
        if (idx < 1) return null;
        return workspaceRelativePath.substring(0, idx);
    }
    
    // TODO : replace with resource.getProjectRelativePath() 
    public static String getProjectRelativePath(IResource resource) {
        String wsFullPath = resource.getFullPath().toPortableString();
        return getProjectRelativePath(wsFullPath);
    }

    public static String getProjectRelativePath(String workspaceRelativePath) {
        String path = workspaceRelativePath.substring(1);
        int idx = path.indexOf('/');
        if (idx < 0) return null;
        return path.substring(idx);
    }
    
    public static String getRelativePath(IProject project, String absolutePath) {
    	if (project == null) {
    		return null;
    	}
        URI uri = new File(absolutePath).toURI();
        IWorkspaceRoot root = getWorkspaceRoot();
        if (root == null) {
            return null;
        }
        IResource[] resources = root.findFilesForLocationURI(uri);
        if (resources.length == 0) {
            resources = root.findContainersForLocationURI(uri);
        }
        if (resources.length == 0) 
            return null;
        
        for (IResource res : resources) {
        	if (res.getProject() == null){ // Workspace root.
        		return null;
        	}
			if (res.getProject().getName().equals(project.getName())) return getProjectRelativePath(res);
        }
        
        return null;
    }
    
    /**
     * Resolves collection of absolute pathes against the given {@link IProject}
     * @param project - project to resolve absolute pathes against
     * @param absolutePathes - absolute path (in the local FS) to resolve
     * @return mapping of {@link File} (from original absolute path) to the {@link IResource} 
     */
    public static Map<File, IResource> resolve(IProject project, Collection<String> absolutePathes) {
    	Map<File, IResource> file2Resource = new HashMap<File, IResource>();
    	
    	for (String absolutePath : absolutePathes) {
			File absoluteFile = getAbsolutePathAsInFS(new File(absolutePath));
			if (absoluteFile.exists()) {
				file2Resource.put(absoluteFile, null);
			}
		}

    	getProjectResources(project, r -> true).forEach(r -> {
    		File resourceFile = getAbsoluteFile(r);
    		if (file2Resource.containsKey(resourceFile)) {
    			file2Resource.put(resourceFile, r);
    		}
    	});
    	
    	absolutePathes.forEach(absolutePath -> {
    		if (file2Resource.get(absolutePath) == null){
    			file2Resource.remove(absolutePath);
    		}
    	});
    	
    	return file2Resource;
    }
    
    /**
     * Searches for existent resource with the given URI from the project
     * @param project
     * @param uri
     * @return Returns first {@link IResource} from the {@link IProject} with the given {@link URI}
     */
    public static IResource getResource(IProject project, URI uri) {
    	IWorkspaceRoot root = getWorkspaceRoot();
        if (root == null) {
            return null;
        }
        IResource[] fileResources = root.findFilesForLocationURI(uri);
        IResource[] folderResources = root.findContainersForLocationURI(uri);
        
        Iterable<IResource> iterable = Stream.concat(Arrays.stream(fileResources), Arrays.stream(folderResources))::iterator;
        for (IResource r : iterable) {
        	if (ObjectUtils.equals(r.getProject(), project) && r.exists() && !isInsideXdsRelativeFolderPathesToSkip(r)) return r;
		}
        
        return null;
    }
    
    public static IResource[] getResourcesFrom(ResourceMapping mapping) {
    	List<IResource> resources = new ArrayList<IResource>();
		try {
			ResourceTraversal[] traversals = mapping.getTraversals(null, null);
			for (ResourceTraversal traversal : traversals) {
				resources.addAll(Arrays.asList(traversal.getResources()));
			}
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
		return resources.toArray(new IResource[0]);
	}
    
    public static int findMaxProblemSeverity(IResource[] resources, String type, boolean includeSubtypes, int depth) throws CoreException {
    	int max = -1;
    	for (IResource res : resources) {
    		if (res.getProject().isOpen() && res.exists()) {
    			max = Math.max(max, res.findMaxProblemSeverity(type, includeSubtypes, depth));
    			if (max >= IMarker.SEVERITY_ERROR) {
    				break;
    			}
    		}
		}
    	
    	return max;
    }
    
    public static int findMaxProblemSeverity(IResource[] resources, String type, int depth) throws CoreException {
    	return findMaxProblemSeverity(resources, type, true, depth);
    }

    public static Path getRelativePath(String resourcePath1, String resourcePath2) {
    	Path pathBase = Paths.get(resourcePath1);
    	Path pathAbsolute = Paths.get(resourcePath2);
    	
        Path result = pathBase.relativize(pathAbsolute);
		return result;
    }
    
    /**
     * Checks whether resource exist with different casing and returns name of such a resource. Returns null otherwise (doesnot exist or exist with same casing).
     * @param directory
     * @param resourceName
     * @return
     */
    public static String checkResourceExistWithDifferentCasing(String directory, String resourceName) {
        File dir = new File(directory);
        String[] resources = dir.list();
        for (String res : resources) {
            if (res.equalsIgnoreCase(resourceName) && !res.equals(resourceName)) return res;
        }
        return null;
    }
    
    public static InputStream getPluginResourceAsStream(Bundle bundle, String entry) throws IOException {
        URL fileURL = bundle.getEntry(entry);
        return fileURL.openStream();
    }
    
    /**
     * See {@link #checkResourceExistWithDifferentCasing(String, String)} 
     */
    public static String checkResourceExistWithDifferentCasing(IContainer containerResource, String resourceName) {
        return checkResourceExistWithDifferentCasing(getAbsolutePath(containerResource), resourceName);
    }
    
    public static List<IResource> getImmediateChildren(IResource resource) {
        final List<IResource> children = new ArrayList<IResource>();
        try {
            resource.accept(new IResourceVisitor() {
                @Override
                public boolean visit(IResource resource) throws CoreException {
                    children.add(resource);
                    return false;
                }
            }, IResource.DEPTH_ZERO, false);
        } catch (CoreException e) {
            LogHelper.logError(e);
        }
        return children;
    }
    
    /**
     * Gets all resources (see {@link IResource}) matches specified {@link filter} 
     * 
     * @param project
     * @param filter - filter to match, if null - matches everything
     * @return all resources (see {@link IResource}) matches specified filter
     */
    public static <T extends IResource> List<T> getProjectResources(IProject project, final Predicate<IResource> filter) {
    	final List<T> resources = new ArrayList<T>();
    	try {
			project.accept(new IResourceVisitor() {
				@Override
				@SuppressWarnings("unchecked")
				public boolean visit(IResource r) throws CoreException {
					if (filter == null || filter.apply(r)) {
						resources.add((T)r);
					}
					return true; 
				}
			});
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
    	return resources;
    }

    public static boolean isInsideXdsRelativeFolderPathesToSkip(IResource resource) {
    	for (String relativeFolderPath : SpecialFolderNames.getIgnoredSpecialFolderNames()) {
            if (ResourceUtils.isInsideFolder(relativeFolderPath, resource)) {
                return true;
            }
        }
    	return false;
    }
    
    public static void scheduleWorkspaceRunnable(final IWorkspaceRunnable operation, String jobCaption) throws CoreException {
    	scheduleWorkspaceRunnable(operation, jobCaption, false);
    }

    public static void scheduleWorkspaceRunnable(final IWorkspaceRunnable operation, String jobCaption, boolean user) throws CoreException {
    	IWorkspace workspace = ResourcesPlugin.getWorkspace();
    	scheduleWorkspaceRunnable(operation, workspace.getRoot(), jobCaption, user);
    }
    
    public static void scheduleWorkspaceRunnable(final IWorkspaceRunnable operation,
    		final ISchedulingRule rule, String jobCaption, boolean user) {
    	scheduleWorkspaceRunnable(operation, rule, jobCaption, null, user);
    }

    public static Job scheduleWorkspaceRunnable(final IWorkspaceRunnable operation,
    		final ISchedulingRule rule, String jobCaption, final Object assignedFamily, boolean user) {
    	Job job = new Job(jobCaption) {
    		public IStatus run(IProgressMonitor monitor) {
    			try {
    				runWorkspaceRunnable(operation, rule);
    			} catch (CoreException e) {
    				LogHelper.logError(e);
    			}
    			return Status.OK_STATUS;
    		}

			@Override
			public boolean belongsTo(Object family) {
				if (assignedFamily != null){
					return assignedFamily == family;
				}
				else {
					return super.belongsTo(family);
				}
			}
    	};
    	job.setRule(rule);
    	job.setUser(user);
    	job.schedule();
    	return job;
    }
    
    public static void scheduleJob(final IBaseClosure<Void, CoreException> jobRunnable, String jobCaption) {
    	scheduleJob(jobRunnable, null, jobCaption, false);
    }
    
    public static void scheduleJob(final IBaseClosure<Void, CoreException> jobRunnable, ISchedulingRule rule, String jobCaption, boolean user) {
    	Job job = new Job(jobCaption) {
    		public IStatus run(IProgressMonitor monitor) {
    			try {
    				jobRunnable.execute(null);
    			} catch (CoreException e) {
    				LogHelper.logError(e);
    			}
    			return Status.OK_STATUS;
    		}
    	};
    	job.setRule(rule);
    	job.setUser(user);
    	job.schedule();
    }
    
    public static void runWorkspaceRunnable(IWorkspaceRunnable operation, ISchedulingRule rule)
            throws CoreException {
    	IWorkspace workspace = ResourcesPlugin.getWorkspace();
    	try {
			workspace.run(operation, rule, IWorkspace.AVOID_UPDATE, null);
		} catch (CoreException e1) {
			LogHelper.logError(e1);
		}
    }
    
    public static boolean isPathValid(String path) {
        File f = new File(path);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Get container - immediate child of project. Say, for .setting/subfolder/res.txt it will be .setting folder
     */
    public static IFolder getProjectLevelContainer(IResource r) {
        IProject p = r.getProject();
        String relPath = r.getProjectRelativePath().toPortableString();
        int idx = relPath.indexOf('/');
        if (idx > 0) {
            return p.getFolder(relPath.substring(0, idx));
        }
        else {
            return p.getFolder(relPath);
        }
    }
    
    /**
     * @param relativeFolderPath
     * @param resource
     * @return
     */
    public static boolean isInsideFolder(String relativeFolderPath, IResource resource) {
        IProject project = resource.getProject();
        if (project == null){ // workspace resource
        	return false;
        }
        IFolder folder = project.getFolder(relativeFolderPath);
        return folder.getFullPath().isPrefixOf(resource.getFullPath());
    }
    
    public static boolean isAutoBuilding() {
        IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
        return description.isAutoBuilding();
    }
    
    public static void refreshLocalAsync(IResource r, IProgressMonitor monitor) {
    	try {
			r.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
    }
    public static void refreshLocalSync(IResource r) {
    	refreshLocalSync(r, new NullProgressMonitor());
    }
    
    public static void refreshLocalSync(IResource r, final IProgressMonitor progressMonitor) {
    	final Object monitor = new Object(); 
        try {
            synchronized (monitor) {
                final boolean[] isReady = new boolean[]{false};
                r.refreshLocal(IResource.DEPTH_INFINITE, new ProgressMonitorWrapper(progressMonitor) {
                    @Override
                    public void done() {
                        isReady[0] = true;
                        monitor.notify();
                    }
                });
                while(!isReady[0]) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        } catch (CoreException e) {
            LogHelper.logError(e);
        }
    }
    
    public static Bundle getXdsResourcesPluginBundle() {
    	return Platform.getBundle("com.excelsior.xds.resources"); //$NON-NLS-1$
    }
    
    public static File getXdsPluginsDirectory() throws IOException {
    	File bundleFile = FileLocator.getBundleFile(getXdsResourcesPluginBundle());
    	return bundleFile.getParentFile();
    }
    
    public static List<IResource> applyXdsResourcesFilter(Collection<IResource> resources) {
    	List<IResource> filteredResources = new ArrayList<IResource>();
    	for (IResource r : resources) {
    		if (NatureUtils.hasNature(r.getProject(), NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID)) {
    			filteredResources.add(r);
			}
    	}
    	return filteredResources;
    }
    
    public static String getTextContent(IFile file) throws IOException {
    	String path = getAbsolutePath(file);
    	try {
            return FileUtils.readFileToString(new File(path), file.getCharset());
        } catch (CoreException e) {
            LogHelper.logError(e);
        }
    	return FileUtils.readFileToString(new File(path));
    }

	public static IProject getProject(String projectName) {
		return ResourceUtils.getWorkspaceRoot().getProject(projectName);
	}
	
	/**
	 * Gets IProject of given resource. 
	 * @param resource
	 * @return IProject or null, if resource is null
	 */
	public static IProject getProject(IResource resource) {
		IProject p = null;
		if (resource != null) {
			p = resource.getProject();
		}
		return p;
	}
	
	/**
	 * Checks whether resource belongs to the opened project
	 */
	public static boolean isOpen(IResource res) {
		return res != null && res.getProject().isOpen();
	}
	
	public static ISchedulingRule createRule(Collection<? extends IResource> resources) {
        ISchedulingRule combinedRule = null;
        IResourceRuleFactory ruleFactory = 
              ResourcesPlugin.getWorkspace().getRuleFactory();
        
        for (IResource r : resources) {
        	ISchedulingRule rule = ruleFactory.createRule(r);
            combinedRule = MultiRule.combine(rule, combinedRule);
		}
        
        return combinedRule;
     }
	
	/**
	 * 
	 *  ResourceUtils.iterateRelativeParents("c:/temp/hello/SRC/hello.mod ", true) :
	 *  yields :
	 *  
	 *  SRC
	 *	hello\SRC
	 *	temp\hello\SRC
	 *
	*  ResourceUtils.iterateRelativeParents("c:/temp/hello/SRC/hello.mod ", false) :
	 * yields :
	 * 
	 * temp\
	 * temp\hello\
	 * temp\hello\SRC\
	 *  
	 * @return
	 */
	public static Iterator<String> iterateRelativeParents(String absolutePath, boolean isReverse) {
		final String path = FilenameUtils.separatorsToSystem(absolutePath);
		
		if (isReverse) {
			return new Iterator<String>() {
				
				int lastSlashIdx = path.lastIndexOf(File.separatorChar, path.length());
				int idx = lastSlashIdx;
				
				@Override
				public void remove() {
					throw new NotImplementedException();
				}
				
				@Override
				public String next() {
					return path.substring(idx + 1, lastSlashIdx);
				}
				
				@Override
				public boolean hasNext() {
					idx = path.lastIndexOf(File.separatorChar, idx - 1);
					return idx > -1;
				}
			};
		}
		else{
			return new Iterator<String>() {
				int lastSlashIdx = path.indexOf(File.separatorChar, 0);
				int idx = lastSlashIdx;
				
				@Override
				public void remove() {
					throw new NotImplementedException();
				}
				
				@Override
				public String next() {
					return path.substring(lastSlashIdx + 1, idx + 1);
				}
				
				@Override
				public boolean hasNext() {
					idx = path.indexOf(File.separatorChar, idx + 1);
					return idx > -1;
				}
			};
		}
	}
	
	public static String getFileName(File file) {
		if (file == null) {
			return null;
		}
		
		return file.getName();
	}
	
	/**
	 * @param dir - directory to start from, included in result
	 * @return collection of sub-directories in the given directory 
	 */
	public static Collection<File> listDirectories(File dir) {
		Collection<File> dirs = new ArrayList<File>();
		listDirectoriesInternal(dir, dirs);
		return dirs;
	}
	
	public static List<IResource> getSiblings(IResource f) {
		return getSiblings(f, IResource.class);
	}
	
	/**
	 * Gets resource siblings including itself.
	 * @param f
	 * @param tagClass filter class, subclass of {@link IResource}
	 * @return list of found resources
	 */
	public static <T extends IResource, U extends IResource> List<U> getSiblings(T f, Class<U> tagClass) {
		return getChildren(f.getParent(), tagClass);
	}
	
	/**
	 * Gets resource children
	 * @param f
	 * @param tagClass filter class, subclass of {@link IResource}
	 * @return list of child resources
	 */
	public static <T extends IResource, U extends IResource> List<U> getChildren(T f, Class<U> tagClass) {
		List<U> found = new ArrayList<>();
		try {
			f.accept(new IResourceVisitor() {
				@SuppressWarnings("unchecked")
				@Override
				public boolean visit(IResource r) throws CoreException {
					if (tagClass.isAssignableFrom(r.getClass())) {
						found.add((U)r);
					}
					return true;
				}
			}, IResource.DEPTH_ONE, false);
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
		return found;
	}
	
	public static List<File> getParts(File f) {
		List<File> parts = new ArrayList<>();
		
		do{
			parts.add(f);
			f = f.getParentFile();
		}
		while(f != null);
		
		Collections.reverse(parts);
		
		return parts;
	}
	
	/**
	 * Directory of the Eclipse platform installation
	 * @return FS path of the eclipse installation or null if it is running without installation
	 * @see {@link Platform#getInstallLocation()}
	 */
	public static File getEclipseInstallationDirectory() {
		Location location = Platform.getInstallLocation();
		if (location == null || !location.isSet()){
			return null;
		}
		try {
			return new File(location.getURL().toURI());
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	/**
	 * Applies filter to the target {@link IFolder}
	 * @param targetFolder
	 * @param filterType see {@link IContainer#createFilter(int, FileInfoMatcherDescription, int, IProgressMonitor)}
	 * @param regex filtering regex
	 * @param monitor
	 * @throws CoreException
	 * @see {@link IContainer#createFilter(int, FileInfoMatcherDescription, int, IProgressMonitor)}
	 */
	public static IResourceFilterDescription  applyRegexFilter(IFolder targetFolder, int filterType, String regex, IProgressMonitor monitor) throws CoreException {
		return targetFolder.createFilter(IResourceFilterDescription.EXCLUDE_ALL | IResourceFilterDescription.FOLDERS,  
			       new FileInfoMatcherDescription(REGEX_RESOURCE_FILTER_ID,
			    		   regex), IResource.NONE, monitor);
	}
	
	/** Driver function for the {@link #listDirectories(File)}
	 * @param dir
	 * @param dirs
	 * @see #listDirectories(File)
	 */
	private static void listDirectoriesInternal(File dir, Collection<File> dirs) {
		dirs.add(dir);
		String[] subDirNames = dir.list(DirectoryFileFilter.INSTANCE);
		for (String subDirName : subDirNames) {
			File subDir = new File(dir.getAbsolutePath() + IOUtils.DIR_SEPARATOR +  subDirName);
			listDirectoriesInternal(subDir, dirs);
		}
	}
}
