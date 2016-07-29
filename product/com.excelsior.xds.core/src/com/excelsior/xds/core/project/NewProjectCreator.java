package com.excelsior.xds.core.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang.text.StrTokenizer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.osgi.framework.Bundle;

import com.excelsior.xds.core.compiler.compset.ExternalResourceManager;
import com.excelsior.xds.core.internal.nls.Messages;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.natures.NatureIdRegistry;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkManager;
import com.excelsior.xds.core.templates.SourceFileCreator;
import com.excelsior.xds.core.templates.SourceFileTemplate;

/**
 * Special class for creating Modula-2 projects.
 */
public final class NewProjectCreator {
    
    private String mainModule;

	public IProject createFromScratch(NewProjectSettings settings, Bundle resourceBundle, IUserInteraction userInteraction) {
    	
    	FromScratchMaker fsm = new FromScratchMaker(settings, resourceBundle, userInteraction); 
    	
    	String err = fsm.createProjectFiles();
    	if (err != null) {
    		if (!err.isEmpty()) {
    			userInteraction.showOkNotification(Messages.NewProjectCreator_CreatePrjFromScratch, err);
    		}
    		return null;
    	}
    	
        final IProject project = createProject(settings.getProjectName(), settings.getProjectRoot());
        openProject(project);
        
        ExternalResourceManager.recreateExternalsFolder(project, new NullProgressMonitor());

        final XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(project);

        if (XdsProjectType.PROJECT_FILE.equals(fsm.projectType)) {
        	xdsProjectSettings.setXdsProjectFile(ResourceUtils.getRelativePath(project, fsm.prjFile));
            xdsProjectSettings.setProjectType(XdsProjectType.PROJECT_FILE);
        } else {
        	xdsProjectSettings.setMainModule(ResourceUtils.getRelativePath(project, fsm.mainModule));
            xdsProjectSettings.setProjectType(XdsProjectType.MAIN_MODULE);
        }
        mainModule = fsm.mainModule;
        
	    xdsProjectSettings.setProjectSdk(settings.getProjectSdk());

        NatureUtils.addNature(project, NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID);
        ResourceUtils.scheduleWorkspaceRunnable(new IWorkspaceRunnable() {
		    @Override
		    public void run(IProgressMonitor monitor) throws CoreException {
		        xdsProjectSettings.flush();
		    }
		}, project, Messages.NewProjectCreator_BuildingProject, false);
        
        return project;
    }
	
	public String getMainModule() {
	    return mainModule;
	}

    public static IProject createFromSources (NewProjectSettings settings) {
        final IProject project = createProject(settings.getProjectName(), settings.getProjectRoot());
        openProject(project);
        ExternalResourceManager.recreateExternalsFolder(project, new NullProgressMonitor());
        
        final XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(project);

        if (settings.getXdsProjectFile() != null) {
        	xdsProjectSettings.setProjectType(XdsProjectType.PROJECT_FILE);
        	xdsProjectSettings.setXdsProjectFile(ResourceUtils.getRelativePath(project, settings.getXdsProjectFile()));
        } else {
        	xdsProjectSettings.setProjectType(XdsProjectType.MAIN_MODULE);
        	xdsProjectSettings.setMainModule(ResourceUtils.getRelativePath(project, settings.getMainModule()));
        }

	    xdsProjectSettings.setProjectSdk(settings.getProjectSdk());

        NatureUtils.addNature(project, NatureIdRegistry.MODULA2_SOURCE_PROJECT_NATURE_ID);
        
        ResourceUtils.scheduleWorkspaceRunnable(new IWorkspaceRunnable() {
		    @Override
		    public void run(IProgressMonitor monitor) throws CoreException {
		        xdsProjectSettings.flush();
		    }
		}, project, Messages.NewProjectCreator_BuildingProject, false);
        
        return project;
    }
    
    private static IProject createProject(String projectName, String location) {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        String workspacePath = ResourceUtils.getAbsolutePath(workspaceRoot);
        String differentCasingProjectName = ResourceUtils.checkResourceExistWithDifferentCasing(workspaceRoot, projectName);
        if (differentCasingProjectName != null) {
            projectName = differentCasingProjectName;
        }
        
        cleanProjectArea(projectName, location);
        
        IProject newProject = workspaceRoot
                .getProject(projectName);
        
        URI projectLocation = new File(location).toURI();
        IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());
        String locationParent = new File(location).getParent();
        if (location != null && (workspacePath.equals(location) || workspacePath.equals(locationParent))) {
            projectLocation = null;
        } else if (location == null) {
            projectLocation = null;
        }
        desc.setLocationURI(projectLocation);
        try {
            newProject.create(desc, null);
        } catch (CoreException e) {
            LogHelper.logError(e);
        }

        return newProject;
    }

	private static void cleanProjectArea(String projectName, String location) {
		String workspacePath = ResourceUtils.getAbsolutePath(ResourcesPlugin.getWorkspace().getRoot());
		String locationParent = new File(location).getParent();
		if (workspacePath.equals(location) || workspacePath.equals(locationParent)) {
			location = FilenameUtils.concat(workspacePath, projectName);
		}
		
    	File projectDescriptor = new File(FilenameUtils.concat(location, ".project")); //$NON-NLS-1$
		if (projectDescriptor.exists()) {
			FileUtils.deleteQuietly(projectDescriptor);
		}
	}
    
    private static void openProject(IProject project) {
        if (!project.isOpen()) {
            try {
                project.open(null);
            } catch (CoreException e) {
                LogHelper.logError(e);
            }
        }
    }


    
    private static class FromScratchMaker {
        private NewProjectSettings desc;
        public String mainModule;
        public String prjFile;
        public XdsProjectType projectType;
        private IUserInteraction userInteraction;
        
        public FromScratchMaker(NewProjectSettings desc, Bundle resourceBundle, IUserInteraction userInteraction) {
        	this.desc = desc;
        	this.userInteraction = userInteraction;
        }
	    
	    /**
	     * Create project from scratch
	     * 
	     * @param newProjectDesc
	     * @return true when ok
	     * 
	     * Set this.mainModule  = main module name if it was created
	     *     this.prjFile     = .prj file name if it was created
	     *     this.projectType = XdsProjectType.MAIN_MODULE or XdsProjectType.PROJECT_FILE
	     */
	    private String createProjectFiles() {
	    	
	    	this.projectType = XdsProjectType.MAIN_MODULE;
	   	
	    	// Get SDK:
	    	Sdk sdk = desc.getProjectSdk();
	    	if (sdk == null) {
	    		sdk = SdkManager.getInstance().loadSdkRegistry().getDefaultSdk();
	    	}
	    	if (sdk == null) {
	    		return Messages.NewProjectCreator_CantNoSdk;
	    	}
	    	
	    	// .prj file:
	    	File prjFileToCreate;
	    	{
    	    	String name = desc.getXdsProjectFile();
    	    	if (name == null || name.isEmpty()) {
    	    	    name = desc.getProjectName();
    	    	}
    	    	if (!name.toLowerCase().endsWith(".prj")) { //$NON-NLS-1$
    	    	    name += ".prj"; //$NON-NLS-1$
    	    	}
    	    	prjFileToCreate = new File(desc.getProjectRoot(), name);
	    	}
	
	    	// Main module or ""
	    	String mainModule = ""; //$NON-NLS-1$
	    	String mainModuleName = ""; //$NON-NLS-1$
	    	String pragmaMain = ""; //$NON-NLS-1$
	    	if (desc.getMainModule() != null && !StringUtils.isBlank(desc.getMainModule())) {
	    		// Ensure extension = "mod" or "ob2" in lower case 
	    		mainModule = desc.getMainModule().trim();
	    		if (mainModule.toLowerCase().endsWith(".mod")) { //$NON-NLS-1$
	    			mainModule = mainModule.substring(0, mainModule.length()-3) + "mod";  //$NON-NLS-1$
	    		} else if (mainModule.toLowerCase().endsWith(".ob2")) { //$NON-NLS-1$
	    			mainModule = mainModule.substring(0, mainModule.length()-3) + "ob2";  //$NON-NLS-1$
	    	    	pragmaMain = "<* +MAIN *>\n"; //$NON-NLS-1$
	    		} else {
	    			mainModule += ".mod"; //$NON-NLS-1$
	    		}
	    		mainModuleName = FilenameUtils.getBaseName(mainModule);
	    	}
	    	
	    	// xc:
	    	File xcExe = new File(sdk.getCompilerExecutablePath());
	    	String xcName = FilenameUtils.getBaseName(xcExe.getName());
	
	    	// Some variables from old xShell\src\var.c:
	    	//
	    	HashMap<String, String> vars = new HashMap<String, String>();
			vars.put("mainmodule", mainModule); // - main module name or "" //$NON-NLS-1$
	    	vars.put("xdsmain", xcExe.getAbsolutePath()); //  - main Compiler Executable //$NON-NLS-1$
	    	vars.put("xdsname", xcName); // - its name without .exe //$NON-NLS-1$
	    	vars.put("xdsdir", xcExe.getParent()); //   - its directory //$NON-NLS-1$
	    	vars.put("project", prjFileToCreate.getAbsolutePath()); // - full project file name //$NON-NLS-1$
	    	vars.put("projdir", prjFileToCreate.getParent()); // - project directory //$NON-NLS-1$
	       	vars.put("projname", desc.getProjectName()); // - project name without path and extension (== Eclipse project name) //$NON-NLS-1$
	       	vars.put("projext", "prj"); //  - project file extension //$NON-NLS-1$ //$NON-NLS-2$
	       	vars.put("homedir", xcExe.getParent()); // - directory where (old) xds shell resides == xdsdir //$NON-NLS-1$
	       	vars.put("eclipse.ide.pragmamain", pragmaMain);     // - used for main module generation  //$NON-NLS-1$
	       	vars.put("eclipse.ide.modulename", mainModuleName); // - used for main module generation  //$NON-NLS-1$
	    	
	
	       	// Create files contents:
	    	//
	    	ArrayList<String> alProblems = new ArrayList<String>(); 
	    	
	    	// --- .prj
	    	String prjContent = null;
	    	if (desc.getXdsProjectFile() != null) {
	    	    if (desc.getTemplateFile() != null) {
	                prjContent = openTemplate(desc.getTemplateFile(), vars, alProblems, Messages.NewProjectCreator_ProjectWord); 
	    	    } else {
	    	        prjContent = ""; //$NON-NLS-1$

	    	        String path = "$!/"; //$NON-NLS-1$
	    	        {
	    	            File fprj = new File(desc.getXdsProjectFile());
	    	            while ((fprj = fprj.getParentFile()) != null) {
	    	                path += "../"; //$NON-NLS-1$
	    	            }
	    	        }
                    path += FilenameUtils.separatorsToUnix(FilenameUtils.getFullPathNoEndSeparator(mainModule));
                    
	    	        if (!path.equals("$!/")) { //$NON-NLS-1$
	                    prjContent += "-lookup = *.mod = " + path + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
                        prjContent += "-lookup = *.ob2 = " + path + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
                        prjContent += "-lookup = *.def = " + path + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
                        prjContent += "\n"; //$NON-NLS-1$
                        prjContent += "!module " + path + '/' + FilenameUtils.getName(mainModule) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
	    	        } else {
	                    prjContent += "!module " + mainModule + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
	    	        }
	    	    }
	    	}
	    	
	    	// --- .red
	    	String redContent = desc.isCreateRedFile() ? 
	    	        openTemplate(sdk.getTrdFile(), vars, alProblems, Messages.NewProjectCreator_RedirectionWord) : null;
	    	
	    	// --- main
	    	String mainContent = null; 
	    	if (!mainModule.isEmpty()) {
	    		try {
					mainContent = SourceFileCreator.getTemplateContent(sdk, SourceFileTemplate.MAIN_MODULE, vars);
				} catch (FileNotFoundException e) {
					LogHelper.logError(e);
				} catch (IOException e) {
					LogHelper.logError(e);
				}
	    	}
	    	
	    	if (!alProblems.isEmpty()) {
	    		String msg = Messages.NewProjectCreator_TheFollowingProblems;
	    		for (String s:alProblems) {
	    			msg += s + "\n"; //$NON-NLS-1$
	    		}
	    		msg += Messages.NewProjectCreator_DoYouWantToContinue;
	    		if (!userInteraction.askYesNoQuestion(Messages.NewProjectCreator_CreatePrjFromScratch, msg)) {
    				return ""; //$NON-NLS-1$
    			}
	    	}
	    	
	    	//--- Create directories and write files:
	    	
	    	// Ensure project root dir:
	    	File projectDir = new File(desc.getProjectRoot());
	    	if (projectDir.isDirectory()) {
	    		File[] fl = projectDir.listFiles();
	    		if (fl != null && fl.length > 0) {
	    			if (!userInteraction.askYesNoQuestion(Messages.NewProjectCreator_CreatePrjFromScratch, Messages.NewProjectCreator_ProjDirEmpty)) {
	    				return ""; //$NON-NLS-1$
	    			}
	    		}
	    	} else if (projectDir.isFile()) {
                                return Messages.NewProjectCreator_TheDirIsFile + ": " + projectDir.getAbsolutePath();  //$NON-NLS-1$
	    	} else if (!projectDir.mkdirs()) {
                                return Messages.NewProjectCreator_CantCreateDir + ": " + projectDir.getAbsolutePath();  //$NON-NLS-1$
	    	}
	
	    	// Create default directories
	    	if (desc.isCreateDirs()) {
	    		String dirs = sdk.getDirsToCreate();
	    		if (dirs != null) {
	    			String arr[] = new StrTokenizer(dirs, ';', '"').getTokenArray();
	    			for (String d : arr) {
	    			    d = d.trim();
	    			    if (!d.isEmpty()) {
    	    				File subdir = new File(projectDir, d.trim());
    	    				if (!subdir.isDirectory() && !subdir.mkdirs()) {
                                                return Messages.NewProjectCreator_CantCreateDir+ ": " + subdir.getAbsolutePath(); //$NON-NLS-1$
    	    				}
	    			    }
	    			}
	    		}
	    	}
	    	
	    	// Create .prj file
	    	if (prjContent != null) {
	    		String err = createFile(prjFileToCreate, prjContent);
	    		if (err != null) {
	    			return err;
	    		}
	    		this.prjFile = prjFileToCreate.getAbsolutePath();
	    		this.projectType = XdsProjectType.PROJECT_FILE;
	    	}
	
	    	// Create main module:
	    	if (mainContent != null) {
	    		File mainFile = new File(desc.getProjectRoot(), mainModule); 
	    		String err = createFile(mainFile, mainContent);
	    		if (err != null) {
	    			return err;
	    		}
	    		this.mainModule = mainFile.getAbsolutePath(); 
	    	}
	    	
	    	// Create .red file
	    	if (redContent != null) {
	    		File redFile = new File(desc.getProjectRoot(), xcName+".red");  //$NON-NLS-1$
	    		String err = createFile(redFile, redContent);
	    		if (err != null) {
	    			return err;
	    		}
	    	}
	    	
	    	return null;
	    }

	    /**
	     * Create (project or redirection) file content from the template given in xds.ini
	     * Opens $() variables as it was made in old XDS IDE
	     *  
	     * @param desc
	     * @param fileNameTpl - smth. like "$(xdsdir)\$(xdsname).tpr", "xc.tpr" or null
	     * @param vars - Map with $() vars values
	     * @param alProblems - dest. place to add problems description 
	     * @param creatureName - "redirection", "project"..
	     * @return - string with the file content or null 
	     */
	    private String openTemplate( String fileNameTpl
	    		                   , HashMap<String, String> vars
	    		                   , ArrayList<String> alProblems 
	    		                   , String creatureName ) 
	    {
	    	if (fileNameTpl == null || fileNameTpl.isEmpty()) {
	    		return null;
	    	}
	    	
	    	String fileName = openVars(fileNameTpl, vars);
	        try {
                return openTemplate(new FileInputStream(new File(fileName)), vars, alProblems, creatureName);
            } catch (FileNotFoundException e) {
                return null;
            } 
	    }
	
	    private String openTemplate( InputStream srcStream
	    		                   , HashMap<String, String> vars
	    		                   , ArrayList<String> alProblems 
	    		                   , String creatureName ) 
	    {
	    	StringBuilder sb = new StringBuilder();
	    	BufferedReader br = null;
	    	try {
	    		br = new BufferedReader(new InputStreamReader(srcStream));
	    		String line;
	    		while ((line = br.readLine()) != null) {
	    			if (!line.trim().startsWith("%")) { //$NON-NLS-1$
	    				line = openVars(line, vars);
	    			}
	    			sb.append(line).append("\n"); //$NON-NLS-1$
	    		}
	    	} catch (FileNotFoundException fnf) {
	    	    // should never get there
	    		return null;
	    	} catch (IOException io) {
	    		alProblems.add(String.format(Messages.NewProjectCreator_ErrReadingTpl, creatureName, creatureName));
	    		return null;
	    	} finally {
	    		if (br != null) {
					IOUtils.closeQuietly(br);
	    		}
	    	}
	    	String template = sb.toString();

	    	/* process variables defined in Eclipse style */
	    	StrSubstitutor sub = new StrSubstitutor(vars);
	        template = sub.replace(template);
	        
	    	return template;
	    }
	    
	    private String openVars(String str, HashMap<String, String> vars) {
	    	if (!str.contains("$(")) //$NON-NLS-1$
	    		return str;
	    	
	    	StringBuilder sb = new StringBuilder();
	    	int pos = 0;
	    	int beg;
	    	while((beg = str.indexOf("$(", pos)) >= 0) { //$NON-NLS-1$
	    		int end = str.indexOf(')', beg);
	    		if (end < 0) {
	    			break;
	    		}
	    		String var = str.substring(beg+2, end).toLowerCase();
	    		var = vars.get(var);
	    		if (var != null) {
	        		sb.append(str, pos, beg);
	    			sb.append(var);
	    		} else { // undefined var - leave it as is
	        		sb.append(str, pos, end+1);
	    		}
	    		pos = end+1;
	    	}
			sb.append(str, pos, str.length());
			return sb.toString();
	    }
	    
	    private String createFile(File f, String content) {
	    	try {
	    	    f.getParentFile().mkdirs();
				FileWriter w = new FileWriter(f);
				w.write(content);
				w.close();
			} catch (IOException e) {
				return Messages.NewProjectCreator_ErrWritingFile + f.getAbsolutePath() + "'"; //$NON-NLS-1$
			}
	    	return null;
	    }
    } // class FromScratchMaker
    
    public interface IUserInteraction {
    	void showOkNotification(String title, String message);
    	boolean askYesNoQuestion(String title, String message);
    }
}
