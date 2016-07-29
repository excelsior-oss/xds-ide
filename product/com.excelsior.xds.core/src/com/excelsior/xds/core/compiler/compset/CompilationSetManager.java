package com.excelsior.xds.core.compiler.compset;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import com.excelsior.xds.core.compiler.driver.CompilationTarget;
import com.excelsior.xds.core.compiler.driver.CompileDriver;
import com.excelsior.xds.core.console.IXdsConsole;
import com.excelsior.xds.core.fileset.FileSetManager;
import com.excelsior.xds.core.progress.IListenableProgressMonitor;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;

public class CompilationSetManager extends FileSetManager {
	protected final Map<IProject, Set<String>> project2AbsolutePathesOfParents = new ConcurrentHashMap<IProject, Set<String>>();
	
	/**
	 * Mapping of project name to name lookup. Name lookup is a map of file name to absolute path, if it is in the compilation set.
	 */
	protected final Map<String, Map<String,String>> projectName2NameLookup = new ConcurrentHashMap<String, Map<String,String>>();
	
	protected final List<ICompilationSetListener> compilationSetListeners = new CopyOnWriteArrayList<ICompilationSetListener>();
	
	public Set<String> getCompilationSet(String projectName) {
		Lock readLock = readWriteLock.readLock();
		readLock.lock();
		try {
			return get(projectName);
		} finally {
			readLock.unlock();
		}
	}
	
	/**
	 * TODO : get rid of console parameter 
	 * @param project
	 * @param console
	 * @param monitor
	 * @return true if compilation set was updated, false if no action was taken
	 */
	public boolean updateCompilationSet(IProject project, IXdsConsole console,  IListenableProgressMonitor monitor) {
		if (!project.exists()) {
			return false;
		}
		XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(project);
		Sdk projectSdk = xdsProjectSettings.getProjectSdk();
		if (projectSdk != null) {
			CompileDriver compileDriver = new CompileDriver(projectSdk, console, monitor);
			CompilationTarget compilationTarget = new CompilationTarget(xdsProjectSettings);
			List<String> compilationSetFiles = compilationTarget.getCompilationSetFiles(compileDriver);
			replaceCompilationSet(xdsProjectSettings.getProject(), compilationSetFiles);
		}
		return projectSdk != null;
	}
	
	public void replaceCompilationSet(IProject project, Collection<String> absolutePathes){
		Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			removeFromCompilationSet(project);
			addToCompilationSet(project, absolutePathes);
		} finally {
			writeLock.unlock();
		}
	}
	
    public void addToCompilationSet(IProject project, Collection<String> absolutePathes){
    	Lock writeLock = readWriteLock.writeLock();
    	writeLock.lock();
    	try{
    		add(project.getName(), absolutePathes);
    		fillParents(project, absolutePathes);
    		fillNameLookup(project.getName(), absolutePathes);
    	}
    	finally{
    		writeLock.unlock();
    	}
    	notifyListenersAboutAdded(project, absolutePathes);
    }

    public void removeFromCompilationSet(IProject project) {
    	Set<String> absolutePathes;
    	String projectName = project.getName();
    	Lock writeLock = readWriteLock.writeLock();
    	writeLock.lock();
    	
    	try{
    		absolutePathes = projectName2AbsolutePathes
        			.get(projectName);
        	remove(projectName);
        	removeParents(project);
        	removeNameLookups(projectName);
    	}
    	finally{
    		writeLock.unlock();
    	}
    	
    	if (absolutePathes == null){
    		absolutePathes = Collections.emptySet();
    	}
    	
    	notifyListenersAboutRemoval(projectName, absolutePathes);
    }

    public void removeFromCompilationSet(IProject project, String absolutePath) {
    	Lock writeLock = readWriteLock.writeLock();
    	writeLock.lock();
    	String projectName = project.getName();
    	try{
    		if (isInCompilationSet(projectName, absolutePath)) {
        		remove(projectName, absolutePath);
        		Set<String> parents = getParents(project);
        		if (parents == null) {
        			parents = Collections.emptySet();
        		}
        		Set<String> allAncestorsInsideProject = getAllAncestorsInsideProject(project, absolutePath);
        		parents.removeAll(allAncestorsInsideProject);
        		removeSingleLookup(projectName, absolutePath);
        	}
    	}
    	finally{
    		writeLock.unlock();
    	}
    	notifyListenersAboutRemoval(projectName, Collections.singleton(absolutePath));
    }
    
	public boolean isInCompilationSet(IFile ifile) {
		return isInCompilationSet(ifile.getProject().getName(), ResourceUtils.getAbsolutePath(ifile));
	}

    public boolean isInCompilationSet(String projectName, String absolutePath)  {
    	Lock readLock = readWriteLock.readLock();
		readLock.lock();
		try {
			return contained(projectName, absolutePath);
		} finally {
			readLock.unlock();
			
		}
    }
    
    public boolean isHasCompilationSetChildren(IProject project, String absolutePath) {
    	if (absolutePath == null) {
    		return false;
    	}
    	Lock readLock = readWriteLock.readLock();
    	readLock.lock();
    	try{
    		Set<String> parents = getParents(project);
    		if (parents == null) {
    			parents = Collections.emptySet();
    		}
			return parents.contains(absolutePath);
    	}
    	finally{
    		readLock.unlock();
    	}
    }
    
    public String lookup(IProject project, String moduleFileName) {
    	if (project == null) {
    		return null;
    	}
    	return lookup(project.getName(), moduleFileName);
    }
    
    /**
     * Searches for the module in the compilation set.
     * @return
     * 
     * @param projectName - project name to search in 
     * @param moduleFileName - module file name (with extension) to search
     * @return
     */
    public String lookup(String projectName, String moduleFileName) {
    	if (moduleFileName == null) {
    		return null;
    	}
    	Lock readLock = readWriteLock.readLock();
    	readLock.lock();
    	try{
    		Map<String, String> lookup = getNameLookup(projectName);
    		if (lookup == null) {
    			return null;
    		}
    		return lookup.get(moduleFileName);
    	}
    	finally{
    		readLock.unlock();
    	}
    }
    
    public void addCompilationSetListener(ICompilationSetListener csListener) {
    	compilationSetListeners.add(csListener);
    }
    
    public void removeCompilationSetListener(ICompilationSetListener csListener) {
    	compilationSetListeners.remove(csListener);
    }
    
    private static class CompilationSetManagerHolder{
        static CompilationSetManager INSTANCE = new CompilationSetManager();
    }
    
    public static CompilationSetManager getInstance(){
        return CompilationSetManagerHolder.INSTANCE;
    }
    
    private Set<String> getParents(IProject project) {
    	return project2AbsolutePathesOfParents
				.get(project);
	}
	
	private void fillParents(IProject project,
			Collection<String> absolutePathes) {
		Set<String> parents = getParents(project);
		if (parents == null) {
			parents = CollectionsUtils.newConcurentHashSet();
			project2AbsolutePathesOfParents.put(project, parents);
		}
		String absoluteProjectPath = ResourceUtils.getAbsolutePath(project);
		for (String path : absolutePathes) {
			if (!path.startsWith(absoluteProjectPath)) continue;
			
			Set<String> allAncestorsInsideProject = getAllAncestorsInsideProject(project, path);
			parents.addAll(allAncestorsInsideProject);
		}
	}
	
	private Set<String> removeParents(IProject project) {
		return project2AbsolutePathesOfParents.remove(project);
	}
	
	private void fillNameLookup(String projectName, Collection<String> absolutePathes) {
    	Map<String, String> nameLookup = getNameLookup(projectName);
    	if (nameLookup == null) {
    		nameLookup = new ConcurrentHashMap<String, String>();
    		projectName2NameLookup.put(projectName, nameLookup);
    	}
    	
    	for (String path : absolutePathes) {
    		nameLookup.put(FilenameUtils.getName(path), path);
		}
	}

	private Map<String, String> getNameLookup(String projectName) {
		return projectName2NameLookup.get(projectName);
	}
	
	private void removeNameLookups(String projectName) {
		projectName2NameLookup.remove(projectName);
	}
	
	private void removeSingleLookup(String projectName, String absolutePath) {
		Map<String, String> lookup = getNameLookup(projectName);
		if (lookup == null) {
			return;
		}
		String fileName = FilenameUtils.getName(absolutePath);
		String filePath = lookup.get(fileName);
		if (ResourceUtils.equalsPathesAsInFS(absolutePath, filePath)) {
			lookup.remove(fileName);
		}
	}
	
	private static Set<String> getAllAncestorsInsideProject(IProject project, String path) {
		String absoluteProjectPath = ResourceUtils.getAbsolutePath(project);
		Set<String> allParentsInsideProject = new HashSet<String>();
		String absoluteParentPath = ResourceUtils.getAbsoluteParentPathAsInFS(path);
		do{
			allParentsInsideProject.add(absoluteParentPath);
			if (absoluteProjectPath.equals(absoluteParentPath)) break;
			absoluteParentPath = ResourceUtils.getAbsoluteParentPathAsInFS(absoluteParentPath);
			if (absoluteParentPath == null) { // it is external compilation unit, so it is not inside the project
				allParentsInsideProject.clear();
				break;
			}
		}while(absoluteParentPath != null);
		return allParentsInsideProject;
	}
	
	private void notifyListenersAboutRemoval(String projectName,
			Set<String> compilationSet) {
		for (ICompilationSetListener listener : compilationSetListeners) {
			listener.removed(projectName, compilationSet);
		}
	}
	
	private void notifyListenersAboutAdded(IProject project,
			Collection<String> absolutePathes) {
		for (ICompilationSetListener listener : compilationSetListeners) {
			listener.added(project.getName(), absolutePathes);
		}
	}
}
