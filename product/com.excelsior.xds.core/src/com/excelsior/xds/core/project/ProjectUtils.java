package com.excelsior.xds.core.project;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.natures.NatureIdRegistry;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.utils.XdsFileUtils;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

public final class ProjectUtils {
	private ProjectUtils() {
	}
    /**
     * Checks whether given project have the xds project nature 
     * @return
     */
    public static boolean isXdsProject(IProject p) {
        return isBelongsToXdsProject(p);
    }
	public static boolean isBelongsToXdsProject(IResource r) {
		if (r == null) {
			return false;
		}
		IProject p = r.getProject();
		return NatureUtils.hasNature(p, NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID);
	}
	
	public static boolean isWorkspaceContainsXdsProjects() {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject p : allProjects) {
            if (NatureUtils.hasNature(p, NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID)) {
            	return true;
            }
        }
		return false;
	}
	
	public static String getProjectLocation(IProject p) {
   	 return ResourceUtils.getAbsolutePathAsInFS(p.getLocation().toOSString());
   }
	
	public static IProject getXdsProjectWithProjectRoot(String projectRoot) {
	    IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject p : allProjects) {
            if (NatureUtils.hasNature(p, NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID)) {
                if (getProjectLocation(p).equals(projectRoot)) {
                    return p;
                }
            }
        }
        return null;
	}
	
	public static List<IProject> getXdsProjects() {
	    List<IProject> xdsProjects = new ArrayList<IProject>();
	    IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject p : allProjects) {
            if (NatureUtils.hasNature(p, NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID)) {
                xdsProjects.add(p);
            }
        }
        return xdsProjects;
	}
	
	/**
	 * Checks whether exist project with the name differs only by casing of name`s letters
	 * 
	 * @param projectName - name of the Eclipse project
	 * @return
	 */
	public static boolean isProjectExistsWithAnotherCase(String projectName) {
		projectName = projectName.toLowerCase();
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
       for (IProject p : allProjects) {
       	if (p.getName().toLowerCase().equals(projectName)) {
       		return true;
       	}
       }
       return false;
	}
	
	public static List<IProject> filterXdsProjects(Object[] elements) {
	    List<IProject> projects = new ArrayList<IProject>();
        for (Object o : elements) {
            if (o instanceof IProject) {
                IProject p = (IProject)o;
                if (NatureUtils.hasNature(p, NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID)) {
                    projects.add(p);
                }
            }
        }
        return projects;
	}
	
    public static IFile getResource(IProject project, String relativePath) {
        if (relativePath == null) return null;
        IFile file = project.getFile(relativePath);
        if (!file.exists()) return null;
        return file;
    }
    
    public static IProject getProject(String name) {
        IProject[] projects = ResourceUtils.getWorkspaceRoot().getProjects();
        for (IProject p : projects) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }
    
    public static void refreshLocalSync(IProject project) {
    	ResourceUtils.refreshLocalSync(project);
    }
    
    /**
     * Returns workspace resource corresponding to the prj file, or null if project is MAIN_MODULE type instead of PROJECT_FILE
     * 
     * @return
     */
    public static IFile getPrjFile(XdsProjectSettings xdsProjectSettings) {
        if (XdsProjectType.PROJECT_FILE.equals(xdsProjectSettings.getProjectType())) {
            return ProjectUtils.getResource( xdsProjectSettings.getProject(), xdsProjectSettings.getXdsProjectFile());
        }
        return null;
    }
     
    public static IFile getMainModuleFile(XdsProjectSettings xdsProjectSettings) {
    	return getResource( xdsProjectSettings.getProject(), xdsProjectSettings.getMainModule());
    }
    
    public static String getParseRootModuleName(XdsProjectSettings xdsProjectSettings) {
    	String compilationRootName = xdsProjectSettings.getCompilationRootName();
    	if (XdsFileUtils.isXdsProjectFile(compilationRootName)) {
    		try {
				return Files.readLines(ResourceUtils.getAbsoluteFile(getPrjFile(xdsProjectSettings)), Charset.defaultCharset(), new LineProcessor<String>() {
					final Pattern linePattern = Pattern.compile("[!]module (\\w+(?:[.]\\w{3})?)");
					String result;

					@Override
					public boolean processLine(String line) throws IOException {
						Matcher matcher = linePattern.matcher(line);
						if (matcher.find()) {
							String moduleName = matcher.group(1);
							if (XdsFileUtils.isCompilationUnitFile(moduleName) ||
									FilenameUtils.getExtension(moduleName).isEmpty()) {
								result = moduleName;
								return false;
							}
						}
						
						return true;
					}

					@Override
					public String getResult() {
						return result;
					}
				});
			} catch (IOException e) {
				LogHelper.logError(e);
			}
    		return null;
    	}
    	else {
    		return compilationRootName;
    	}
    }

    public static IFile getApplicationExecutableFile(XdsProjectSettings xdsProjectSettings) {
        String location = xdsProjectSettings.getApplicationExecutable();
        
        if (location == null) {
            // Autodetect 
            IFile seed_file = null; 
            
            switch(xdsProjectSettings.getProjectType()) {
            case MAIN_MODULE:
                seed_file = getMainModuleFile(xdsProjectSettings);
                break;
            case PROJECT_FILE: 
                seed_file = getPrjFile(xdsProjectSettings);
                break;
            }
            
            if (seed_file == null)
                return null;
                
            Sdk sdk = xdsProjectSettings.getProjectSdk();
            String ext = (sdk == null) ? "exe" : sdk.getExecutableFileExtensions(); //$NON-NLS-1$
            
            location = FilenameUtils.removeExtension(ResourceUtils.getAbsolutePath(seed_file))
                     + "." + ext; //$NON-NLS-1$
            
            // TODO : the following action should be done only once after first build
//            if (!new File(location).isFile()) {
//                Collection<File> files = FileUtils.listFiles(new File(FilenameUtils.getFullPath(location)), new String[]{ext}, false);
//                if (!files.isEmpty()) {
//                    location = files.iterator().next().getAbsolutePath();
//                }
//            }
        }
        
        String relativePath = ResourceUtils.getRelativePath(xdsProjectSettings.getProject(), location);
        return ProjectUtils.getResource( xdsProjectSettings.getProject(), relativePath);
   }
       
}