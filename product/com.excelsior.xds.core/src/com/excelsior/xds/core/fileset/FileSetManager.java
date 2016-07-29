package com.excelsior.xds.core.fileset;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;

public abstract class FileSetManager {
	protected final Map<String, Set<String>> projectName2AbsolutePathes = new ConcurrentHashMap<String, Set<String>>();
	protected final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	    
	protected void add(String projectName, Collection<String> absolutePathes) {
		Set<String> fileSet = projectName2AbsolutePathes.get(projectName);
		if (fileSet == null) {
			fileSet = CollectionsUtils.newConcurentHashSet();
			projectName2AbsolutePathes.put(projectName, fileSet);
		}
		for (String path : absolutePathes) {
			fileSet.add(ResourceUtils
					.getAbsolutePathAsInFS(path));
		}
	}
	
	protected Set<String> get(String projectName) {
		Set<String> fileSet = projectName2AbsolutePathes.get(projectName);
		if (fileSet ==  null) {
			fileSet = Collections.emptySet();
		}
		// create full copy in order to allow parallel modifications
		return Collections.unmodifiableSet(fileSet); 
	}
	
	protected void remove(String projectName) {
		Set<String> absolutePathes = projectName2AbsolutePathes
				.get(projectName);
		if (absolutePathes != null) {
			absolutePathes.clear();
		}
	}

	protected void remove(String projectName, String absolutePath) {
		Set<String> absolutePathes = projectName2AbsolutePathes
				.get(projectName);
		if (absolutePathes != null && absolutePath != null) {
			absolutePathes.remove(absolutePath);
		}
	}

	protected boolean contained(String projectName, String absolutePath) {
		Set<String> absolutePathes = projectName2AbsolutePathes
				.get(projectName);
		if (absolutePathes != null && absolutePath != null) {
			return absolutePathes.contains(absolutePath);
		}
		return false;
	}
}
