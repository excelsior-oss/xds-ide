package com.excelsior.xds.core.compiler.libset;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import com.excelsior.xds.core.fileset.FileSetManager;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.utils.XdsFileUtils;

public class LibraryFileSetManager extends FileSetManager{
	private static class LibrarySetManagerManagerHolder{
        static LibraryFileSetManager INSTANCE = new LibraryFileSetManager();
    }
    
    public static LibraryFileSetManager getInstance(){
        return LibrarySetManagerManagerHolder.INSTANCE;
    }
    
    public Set<String> getLibraryFileSet(IProject project) {
    	Lock readLock = readWriteLock.readLock();
    	readLock.lock();
    	try{
    		return get(project.getName());
    	}
    	finally{
    		readLock.unlock();
    	}
    }
    
    public Set<String> getLibraryFileSet(String projectName) {
    	Lock readLock = readWriteLock.readLock();
    	readLock.lock();
    	try{
    		return get(projectName);
    	}
    	finally{
    		readLock.unlock();
    	}
    }
    
    public boolean isInLibraryFileSet(IFile sourceIFile) {
    	Lock readLock = readWriteLock.readLock();
    	readLock.lock();
    	try{
    		return contained(sourceIFile.getProject().getName(), ResourceUtils.getAbsolutePath(sourceIFile));
    	}
    	finally{
    		readLock.unlock();
    	}
    }
    
    public Set<String> updateFrom(IProject project) {
    	Lock writeLock = readWriteLock.writeLock();
    	writeLock.lock();
    	try{
    		remove(project.getName());
	    	XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(project);
	    	final Set<String> absolutePathes = new HashSet<String>();
	    	Sdk sdk = xdsProjectSettings.getProjectSdk();
	    	if (sdk == null) {
	    		return absolutePathes;
	    	}
			String sdkLibraryDir = sdk.getLibraryDefinitionsPath();
	    	if (StringUtils.isBlank(sdkLibraryDir) || !new File(sdkLibraryDir).exists()) {
	    		return absolutePathes;
	    	}
	    	Iterator<File> files = FileUtils.iterateFiles(new File(sdkLibraryDir), DefinitionFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
	    	for (; files.hasNext();) {
	    		File file = files.next();
	    		absolutePathes.add(ResourceUtils.getAbsolutePathAsInFS(file.getAbsolutePath()));
			}
	    	add(project.getName(), absolutePathes);
	    	return Collections.unmodifiableSet(absolutePathes);
    	}
    	finally{
    		writeLock.unlock();
    	}
    }
    
    private static class DefinitionFileFilter implements IOFileFilter {
    	public static DefinitionFileFilter INSTANCE = new DefinitionFileFilter();

		@Override
		public boolean accept(File file) {
			return XdsFileUtils.isDefinitionModuleFile(file.getName());
		}

		@Override
		public boolean accept(File dir, String name) {
			return XdsFileUtils.isDefinitionModuleFile(name);
		}
    }
}