package com.excelsior.xds.builder.compile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import com.excelsior.xds.builder.buildsettings.BuildSettingsCache;
import com.excelsior.xds.builder.compile.MarkerMaker.Location;
import com.excelsior.xds.builder.console.BuildConsoleManager;
import com.excelsior.xds.builder.internal.nls.Messages;
import com.excelsior.xds.builder.listener.BuildListenerManager;
import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.builders.XdsBuildResult;
import com.excelsior.xds.core.builders.XdsSourceBuilderConstants;
import com.excelsior.xds.core.compiler.compset.CompilationSetManager;
import com.excelsior.xds.core.compiler.compset.ExternalResourceManager;
import com.excelsior.xds.core.compiler.driver.CompilationTarget;
import com.excelsior.xds.core.compiler.driver.CompileDriver;
import com.excelsior.xds.core.compiler.driver.XShellListener;
import com.excelsior.xds.core.console.ColorStreamType;
import com.excelsior.xds.core.console.IXdsConsole;
import com.excelsior.xds.core.console.XdsConsoleLink;
import com.excelsior.xds.core.jobs.IJobListener;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.marker.MarkerUtils;
import com.excelsior.xds.core.marker.XdsMarkerConstants;
import com.excelsior.xds.core.progress.DelegatingProgressMonitor;
import com.excelsior.xds.core.progress.IListenableProgressMonitor;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.project.XdsProjectType;
import com.excelsior.xds.core.resource.EncodingUtils;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.resource.visitor.InterruptResourceVisitorException;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.text.TextEncoding;
import com.excelsior.xds.core.utils.BuildJob;
import com.excelsior.xds.core.utils.BuilderUtils;
import com.excelsior.xds.core.utils.EclipseCommandLineUtils;
import com.excelsior.xds.core.utils.XdsFileUtils;

public class XdsSourceBuilder extends IncrementalProjectBuilder {
	
	private static final String XDS_SOURCE_BUILDER_DEBUG_OPTION_NAME = "-XdsSourceBuilderDebug"; //$NON-NLS-1$
	private static final String IDE_INFO_MAGIC = "IDE_INFO_MAGIC_2128506--"; //$NON-NLS-1$
	
	private boolean isCleanRebuild;
	private MarkerMaker markerMaker = new MarkerMaker();
	private HashSet<String>problemSet = new HashSet<String>();
	
	public XdsSourceBuilder() {
	}

	@Override
	protected IProject[] build( final int kind, final Map<String, String> args
	                          ,	IProgressMonitor monitor ) throws CoreException 
   {
		IProject project = getProject();
		ResourceUtils.refreshLocalSync(project);
		final XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(project);
        final IResourceDelta delta = getDelta(project);
        String  buildItemId = args.get(BuilderUtils.MULTIPLE_BUILD_ITEM_ID);
        boolean isRealMultipleBuild = BuilderUtils.isInRealMultipleBuild(buildItemId);
        final IXdsConsole console = isRealMultipleBuild ? BuildConsoleManager.getConsole(null) : BuildConsoleManager.getConsole(xdsProjectSettings.getProject());

		if (shouldBuild(kind, delta, xdsProjectSettings)) {
			final Sdk currentSdk = xdsProjectSettings.getProjectSdk();
			if (currentSdk != null) {
				ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
		            public void run(IProgressMonitor monitor) throws CoreException {
		               performBuild(xdsProjectSettings, kind, args, delta, currentSdk, console, monitor);
		            }
		         }, monitor);
			}
			else{
			    deleteXdsMarkers(xdsProjectSettings.getProject(), false);
			    reportNoSdkDefinedProblem(Messages.XdsSourceBuilder_PleaseSpecifySdk);
                console.println(Messages.XdsSourceBuilder_PleaseSpecifySdk, ColorStreamType.ERROR);
		        BuildListenerManager.getInstance().notifyBuildStarted(getProject());
	            BuildListenerManager.getInstance().notifyBuildFinished(getProject(), XdsBuildResult.ERROR);
	            monitor.done();
			}
		}
		return null;
	}
	
	@Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
	    // TODO : should remove compilation artifacts (obj, exe, ... etc) here, if it is possible
	    isCleanRebuild = true;
    }

    private void performBuild ( final XdsProjectSettings xdsProjectSettings
                              , int kind, Map<String, String> args
                              , IResourceDelta delta, Sdk currentSdk
                              , IXdsConsole console 
                              , final IProgressMonitor monitor ) 
    {
    	
    	IListenableProgressMonitor delegatingMonitor;
    	if (monitor instanceof IListenableProgressMonitor) {
    		delegatingMonitor = (IListenableProgressMonitor) monitor;
		}
    	else {
    		delegatingMonitor = DelegatingProgressMonitor.wrap(monitor);
    	}
        problemSet.clear();
        BuildListenerManager.getInstance().notifyBuildStarted(getProject());
        

        XdsBuildResult buildRes = XdsBuildResult.ERROR;
        String  buildItemId = args.get(BuilderUtils.MULTIPLE_BUILD_ITEM_ID);
        
		try{
			if (checkCancel(delegatingMonitor)) return;
			
			if (BuilderUtils.isNotFirstInMultipleBuild(buildItemId)) {
                console.println(""); //$NON-NLS-1$
                console.println(""); //$NON-NLS-1$
                console.println(""); //$NON-NLS-1$
			} else {
			    console = BuildConsoleManager.reinitializeConsole(console);
			}
			if (!isSkipMakeAndBuildInvocation(args)) {
			    console.printlnFiltered(String.format(Messages.XdsSourceBuilder_PrjBuildStarted, xdsProjectSettings.getProject().getName()), ColorStreamType.SYSTEM);
			} else {
				if (EclipseCommandLineUtils.isApplicationArgSet(XDS_SOURCE_BUILDER_DEBUG_OPTION_NAME)) {
				    console.printlnFiltered(String.format(Messages.XdsSourceBuilder_PrjGettingModuleList, xdsProjectSettings.getProject().getName()), ColorStreamType.SYSTEM);
				}
			}
			
			if (xdsProjectSettings.getProjectType() == null) {
				return;
			}
			CompilationTarget compilationTarget = new CompilationTarget(xdsProjectSettings);
			if (!compilationTarget.isValid()) {
			    if (XdsProjectType.MAIN_MODULE.equals(compilationTarget.getXdsProjectType())) {
			        console.println(xdsProjectSettings.getProject().getName() + Messages.XdsSourceBuilder_NoMainModule, ColorStreamType.ERROR);
			        return;
			    }
			    else if (XdsProjectType.PROJECT_FILE.equals(compilationTarget.getXdsProjectType())) {
			        console.println(Messages.XdsSourceBuilder_NoPrjFile, ColorStreamType.ERROR);
                    LogHelper.logError("XDS prj file is not set at project properties, though it should be", new IllegalAccessException()); //$NON-NLS-1$
                    return;
                }
			}
			
			File compileDir = null;
			try{
				compileDir = xdsProjectSettings.getXdsWorkingDir();
			}
			catch(CoreException e) {
				console.println(e.getMessage(), ColorStreamType.ERROR);
				return;
			}
			if (!compileDir.isDirectory()) {
                String s = String.format(Messages.XdsSourceBuilder_InvalidWorkDir, compileDir.getAbsolutePath()); 
                console.println(s, ColorStreamType.ERROR);
                return;
			}
			
			{
			    File xc = new File(currentSdk.getCompilerExecutablePath());
			    if (!xc.isFile()) {
	                String s = String.format(Messages.XdsSourceBuilder_BadXcExePath, currentSdk.getCompilerExecutablePath()); 
	                console.println(s, ColorStreamType.ERROR);
	                return;
			    }
			}
			cancelMonitorOnJobCancel(delegatingMonitor);
            
			CompileDriver compileDriver = new CompileDriver(currentSdk, console, delegatingMonitor);
			// TODO : get rid of compilation set retrieval from here
			List<String> compilationSetFiles = compilationTarget.getCompilationSetFiles(compileDriver);
			
			CompilationSetManager.getInstance().replaceCompilationSet(xdsProjectSettings.getProject(), compilationSetFiles);
            
            try {
            	BuildSettings buildSettings = BuildSettingsCache.createBuildSettings(xdsProjectSettings.getProject());
				ExternalResourceManager.linkExternals(
						xdsProjectSettings.getProject(),
						buildSettings.getLookupDirs(), true,
						true, delegatingMonitor);
				// TODO : 2016 : isGetLibraryFileSetRequired(args) - seems like we can remove these params
				// TODO : 2016 : isGetCompilationSetRequired(args) - seems like we can remove these params
            } catch (CoreException e) {
                LogHelper.logError(e);
            }

            // Init progressbar in the monitor:
            final int filesProgress[] = new int[1];
            guessJobSize(compilationSetFiles, filesProgress);
            
            String jobName = String.format(Messages.XdsSourceBuilder_BuildPrj, xdsProjectSettings.getProject().getName());
            delegatingMonitor.beginTask(jobName, filesProgress[0]);
            
            String absolutePath = ResourceUtils.getAbsolutePath(compilationTarget.getCompilationTargetFile());
            final String workingDirectory = FilenameUtils.getFullPath(absolutePath);
            
            XShellListener xShellListener = createXShellListener(xdsProjectSettings, delegatingMonitor, xdsProjectSettings.getProject(), workingDirectory, 
                                                                 compilationSetFiles, console, filesProgress);
            
            BuildParamaters buildParameters = new BuildParamaters(args);
            if (isSkipMakeAndBuildInvocation(args)) {
                buildRes = XdsBuildResult.SUCCESS;
            } else {
                console.printlnFiltered(String.format(Messages.XdsSourceBuilder_PrjMakeInvoked, 
                        xdsProjectSettings.getProject().getName(), absolutePath, kind, args), ColorStreamType.SYSTEM);
                console.printlnFiltered(String.format(Messages.XdsSourceBuilder_CompilerWorkDir, compileDir.getAbsolutePath()), ColorStreamType.SYSTEM);

                if (!buildParameters.sourcesForCompilation.isEmpty()) { // Builder is invoked programatically - no delta
                    // Compile one file (may be more than one?):
                    boolean isMake    = ! args.containsKey(XdsSourceBuilderConstants.COMPILE_FILE_KEY);
                    boolean isRebuild = ! isMake || isCleanRebuild || FULL_BUILD == kind;
                    deleteXdsMarkers(xdsProjectSettings.getProject(), true);
                    for (String sourcePath : buildParameters.sourcesForCompilation) {
                        IResource resource = ResourceUtils.getResource(xdsProjectSettings.getProject(), new File(sourcePath).toURI());
                        if (resource != null) {
                            try {
                                resource.deleteMarkers(XdsMarkerConstants.BUILD_PROBLEM_MARKER_TYPE, true, IResource.DEPTH_ZERO);
                            } catch (CoreException e) {
                                LogHelper.logError(e);
                            } 
                        }
                        String prjFile = null;
                        if (XdsProjectType.PROJECT_FILE.equals(compilationTarget.getXdsProjectType())) {
                        	prjFile = ResourceUtils.getAbsolutePath(compilationTarget.getCompilationTargetFile());
                        }
                        buildRes = compileDriver.compileModule(sourcePath, prjFile, compileDir, isRebuild, isMake, xShellListener);
                    }
                }
                else {
                    // Make or rebuild the project:
                    boolean isRebuild = isCleanRebuild || args.containsKey(XdsSourceBuilderConstants.REBUILD_PROJECT_KEY);
                    deleteXdsMarkers(xdsProjectSettings.getProject(), !isRebuild);
                    
                    if (XdsProjectType.MAIN_MODULE.equals(compilationTarget.getXdsProjectType())) {
                        removeMarkersFromDeltaFiles(xdsProjectSettings);
                        buildRes = compileDriver.compileModule(absolutePath, compileDir, isRebuild, true, xShellListener);
                    }
                    else if (XdsProjectType.PROJECT_FILE.equals(compilationTarget.getXdsProjectType())) {
                        removeMarkersFromDeltaFiles(xdsProjectSettings);
                        buildRes = compileDriver.compileProject(absolutePath, compileDir, isRebuild, xShellListener);
                    }
                }
            }
            
            ResourceUtils.refreshLocalSync(getProject());

            if (filesProgress[0] > 0) {
            	delegatingMonitor.worked(filesProgress[0]);
            }

		}
		finally{
		    String summary;
		    ColorStreamType cs;
		    XdsConsoleLink link;
		    boolean filtered;
		    if (buildRes == XdsBuildResult.SUCCESS) {
	            if (!isSkipMakeAndBuildInvocation(args)) {
	                summary = Messages.XdsSourceBuilder_BuildComplete;
	            } else {
	                summary = Messages.XdsSourceBuilder_ModListComplete;
	            }
		        cs = ColorStreamType.SYSTEM;
                link = null;
                filtered = true;
		    } else {
		        if (buildRes == XdsBuildResult.TERMINATED) {
		            summary = Messages.XdsSourceBuilder_BuildTerminated;
		        } else {
	                summary = Messages.XdsSourceBuilder_BuildFailed;
		        }
                cs = ColorStreamType.ERROR;
                link = XdsConsoleLink.mkLinkToProblemsView();
                filtered = false;
		    }
	        String fullSummary = String.format(Messages.XdsSourceBuilder_ProjectSummary, xdsProjectSettings.getProject().getName(), summary);
	        if (filtered) {
	            console.printlnFiltered(fullSummary, cs);
	        } else {
                console.println(fullSummary, cs, link);
	        }
            console.println("");
	        
		    if (buildItemId != null) {
		        BuilderUtils.multipleBuildItemFinished(buildItemId, buildRes, summary);
		    }
            isCleanRebuild = false;
            BuildListenerManager.getInstance().notifyBuildFinished(getProject(), buildRes);
            delegatingMonitor.done();
		}
	}

	/**
	 * Observes the state of the job and cancels the monitor on job cancelation
	 * @param monitor
	 */
	private void cancelMonitorOnJobCancel(final IProgressMonitor monitor) {
		IJobManager jobMan = Job.getJobManager();
		Job[] build = jobMan.find(BuilderUtils.BUILD_JOB_FAMILY);
		BuildJob buildJob = null;
		if (build.length == 1) {
			if (build[0] instanceof BuildJob) {
				buildJob = (BuildJob) build[0];
				buildJob.addListener(new IJobListener() {
					@Override
					public void canceled() {
						monitor.setCanceled(true);
					}
				});
			}
		}
	}

    protected void guessJobSize(List<String> compilationSetFiles,
            final int[] filesProgress) {
        filesProgress[0] = 1; // ~= job size to show projressbar 
        for (String s: compilationSetFiles) {
            s = s.toLowerCase();
            if (s.endsWith("mod") || s.endsWith("ob2")) { //$NON-NLS-1$ //$NON-NLS-2$
                ++ filesProgress[0];
            }
        }
    }

    protected XShellListener createXShellListener(
            final XdsProjectSettings xdsProjectSettings,
            final IProgressMonitor monitor, final IProject project, final String workingDirectory,
            final List<String> compilationSetFiles, 
            final IXdsConsole console,
            final int[] filesProgress) 
    {
        return new XShellListener(){
            Set<IResource> cleanedResources = new HashSet<IResource>();
            boolean errorsFound = false;
            
            @Override
            public void onJobCaption(String caption){
                monitor.subTask(caption);
                // seems that onJobProgress called 1 time for each module:
                monitor.worked(1); 
                --filesProgress[0];
                
                // 'caption' ~= 'Compiling zzz.def' or 'Compiling "c:\dir\zzz.def"'
                // Search zzz.def resource in the compilation set and clear markers in it:
                String fname = caption.trim();
                if  (fname.contains(" "))  { //$NON-NLS-1$
                    fname = fname.substring(fname.lastIndexOf(' ')+1);
                }
                if (fname.length()>2 && fname.startsWith("\"") && fname.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
                    fname = fname.substring(1, fname.length()-1);
                }
                File fabs = new File(fname); 
                if (!fabs.isAbsolute()) {
                    fabs = null;
                    if (fname.contains("\\")) { //$NON-NLS-1$
                        fname = fname.substring(fname.lastIndexOf('\\')+1);
                    }
                    if (fname.contains("/")) { //$NON-NLS-1$
                        fname = fname.substring(fname.lastIndexOf('/')+1);
                    }
                }
                for (String s : compilationSetFiles) {
                    File f = new File(s);
                    if ((fabs == null && f.getName().equals(fname)) ||
                        (fabs != null && FilenameUtils.equalsNormalizedOnSystem(fabs.getAbsolutePath(), f.getAbsolutePath()))) {
                        // This file is going to be compiled. Clear all markers in it:
                        String relativeFilePath = ResourceUtils.getRelativePath(xdsProjectSettings.getProject(), f.getAbsolutePath());
                        if (relativeFilePath != null) { // this file is inside project
                            IResource res = xdsProjectSettings.getProject().getFile(relativeFilePath);
                            if (!cleanedResources.contains(res)) {
                                cleanedResources.add(res);
                                try {
                                    res.deleteMarkers(XdsMarkerConstants.BUILD_PROBLEM_MARKER_TYPE, false, IResource.DEPTH_ZERO);
                                } catch (CoreException e) {
                                    LogHelper.logError(e);
                                }
                            }
                        }
                        break;
                    }
                }
            }

            
            @Override
            public void onJobStart(int progressLimit, String comment) {
                //monitor.beginTask("Building sources...", progressLimit);
            }

            @Override
            public void onJobProgress(int commentProgress, int progress) {
                //monitor.worked(progress);
            }
            
            @Override
            public void onJobComment(String comment) {
                
                // New compilers may send some info to IDE via env.errors.SendIdeInfo(name, value)
                // using JobComments with the magic string:
                if (comment.startsWith(IDE_INFO_MAGIC)) {
                    comment = comment.substring(IDE_INFO_MAGIC.length());
                    int eqpos = comment.indexOf('=');
                    if (eqpos > 0) {
                        String name = comment.substring(0, eqpos).trim(); 
                        String value = comment.substring(eqpos+1).trim();
                        
                        // OutputEncoding=OEM or OutputEncoding=Windows:
                        if ("OutputEncoding".equals(name)) {
                            if ("OEM".equals(value)) {
                                // Linkers uses OEM encoding, old compilers with __XDS_SHELL_ uses 1251.
                                // But new compiler understands __XDS_SHELL__=[Eclipse],[UseOEM] and
                                // sends us its encoding. Turn to OEM if this functionality is supported and turned ON:
                                this.setStreamCharset(TextEncoding.whatCharsetToUse(EncodingUtils.DOS_ENCODING));
                            }
                        }
                        
                    }
                }
            }

            
            @Override
            public void onMessage( MessageType messageType, int messageCode, String message
                                 , String fileName, int line, int pos ) 
            {
                if (!new File(fileName).isAbsolute()) {
                    fileName = FilenameUtils.concat(workingDirectory, fileName); 
                }
                
                int severity;
                char chMode;
                ColorStreamType clrStream;
                switch(messageType){
                case COMPILE_ERROR:
                case COMPILE_FATAL_ERROR:
                    severity = IMarker.SEVERITY_ERROR;
                    chMode = 'E';
                    clrStream = ColorStreamType.XDS_LOG_ERROR;
                    break;
                case COMPILE_WARNING:   
                    severity = IMarker.SEVERITY_WARNING;
                    chMode = 'W';
                    clrStream = ColorStreamType.XDS_LOG_WARNING;
                    break;
                default:
                    severity = IMarker.SEVERITY_INFO;
                    chMode = ' ';
                    clrStream = ColorStreamType.NORMAL;
                }

                String consoleErr = String.format("* [%s %d.%02d %c%03d]\n* %s",  //$NON-NLS-1$
                        fileName,
                        line,
                        pos,
                        chMode,
                        messageCode,
                        message);
                
                IMarker marker = null;
                
                String relativeFilePath = ResourceUtils.getRelativePath(xdsProjectSettings.getProject(), fileName);
                Location loc = new Location();
                if (relativeFilePath != null) { // this file is inside project
                    loc.resource = xdsProjectSettings.getProject().getFile(relativeFilePath);
                    loc.lineNumber = line;
                    loc.posInLine = pos;
                }
                else {
                    loc.resource = xdsProjectSettings.getProject();
                }
                
                if (loc.resource.exists()) {
                    if (!(loc.resource instanceof IProject) && !cleanedResources.contains(loc.resource)) {
                        // 1st marker in the file: remove previous markers if any:
                        cleanedResources.add(loc.resource);
                        try {
                            loc.resource.deleteMarkers(XdsMarkerConstants.BUILD_PROBLEM_MARKER_TYPE, false, IResource.DEPTH_ZERO);
                        } catch (CoreException e) {
                            LogHelper.logError(e);
                        }
                    }
                    
                    loc.severity = severity;
                    loc.message  = message;
                    marker = reportProblem(loc); // null when message duplicates previously reported

                    errorsFound |= (severity == IMarker.SEVERITY_ERROR);
                }
                
                if (marker != null) {
                    if (marker.getResource() instanceof IFile) {
                        console.println(consoleErr, clrStream, new XdsConsoleLink(marker));    
                    } else {
                        // marker w/o file position, links not works (marker.getResource() may be project here)
                        console.println(consoleErr, clrStream);
                    }
                }

            }
            
            @Override
            public void onConsoleString(String str) {
                if (str.startsWith("VLINK (D)") && str.contains("Linker")) { //$NON-NLS-1$ //$NON-NLS-2$
                    // Seems that linker may output in cp1251 with '-w' key and we can 
                    // change 'xv.tem' file and turn off this dirty patch, but .....
                    this.setStreamCharset(TextEncoding.whatCharsetToUse(EncodingUtils.DOS_ENCODING)); // The VAX linker will print in cp866 after XDS compiler's cp1251...  //$NON-NLS-1$
                }
            	console.println(str);
            }
            
            @Override
            public void onParsingError(String message) {
                CompilationSetManager.getInstance().removeFromCompilationSet(project);
                console.println(message, ColorStreamType.ERROR);
            }
            
            @Override
            public void onCompilerExit(int exitCode) {
                if (exitCode != 0 && !errorsFound) { 
                    Location loc = new Location();
                    loc.resource = xdsProjectSettings.getProject();
                    loc.message  = String.format(Messages.XdsSourceBuilder_CompilerError, exitCode);
                    loc.severity = IMarker.SEVERITY_ERROR;
                    loc.violation = XdsMarkerConstants.NO_SDK_ERROR;
                    reportProblem(loc);
                }
            }
            
        };
    }

    private static boolean isSkipMakeAndBuildInvocation(Map<String, String> args) {
        return args != null && (
        		args.containsKey(XdsSourceBuilderConstants.GET_COMPILATION_SET_ONLY_KEY) ||
        		args.containsKey(XdsSourceBuilderConstants.GET_LIBRARY_FILE_SET_ONLY_KEY));
    }
    
    private class BuildParamaters{
        List<String> sourcesForCompilation = new ArrayList<String>();
        
        BuildParamaters(Map<String, String> buildParamaters) {
            if (buildParamaters != null) {
                for (Map.Entry<String, String> keyValuePair : buildParamaters.entrySet()) {
                    if (keyValuePair.getKey() != null && keyValuePair.getKey().startsWith(XdsSourceBuilderConstants.SOURCE_FILE_PATH_KEY_PREFIX)) {
                        sourcesForCompilation.add(keyValuePair.getValue());
                    }
                }
            }
        }
    }
    
	private void removeMarkersFromDeltaFiles(final XdsProjectSettings xdsProjectSettings) {
	    IResourceDelta delta = getDelta(xdsProjectSettings.getProject());
	    if (delta == null) { // full build case - nothing to do
	        return; 
	    }
	    try {
            delta.accept(new IResourceDeltaVisitor() {
                @Override
                public boolean visit(IResourceDelta delta) throws CoreException {
                    if (isDeltaResourceTargetForXdsProject(xdsProjectSettings, delta)) {
                    	MarkerUtils.deleteBuildProblemMarkers(delta.getResource(), true, IResource.DEPTH_ZERO);
                    }
                    
                    IResourceDelta[] affectedChildren = delta.getAffectedChildren();
                    for (int i = 0; i < affectedChildren.length; i++) {
                        affectedChildren[i].accept(this);
                    }
                    
                    return true;
                }
            });
        } catch (CoreException e) {
            LogHelper.logError(e);
        }
    }

    private void reportNoSdkDefinedProblem(String message) {
    	Location loc = new Location();
    	loc.resource = getProject();
    	loc.message = message;
    	loc.severity = IMarker.SEVERITY_ERROR;
    	loc.violation = XdsMarkerConstants.NO_SDK_ERROR;
    	reportProblem(loc);
    }

	/**
	 * Report the specified problem to the user.
	 * Returns null when this message si duplicated so no need to create new marker and it may be
	 * reason to don't print it again to the console
	 */
	private IMarker reportProblem(Location loc) {
	    loc.message = filterChars(loc.message); // KIDE-294
	    IMarker marker = null;
        String toString = String.format("%s [%d:%d]: %s", loc.resource.toString(), loc.lineNumber, loc.posInLine, loc.message); //$NON-NLS-1$
        if (problemSet.add(toString)) { // KIDE-155: inline may cause duplicated messages, reject them
    		try {
    		    marker = markerMaker.makeMarker(loc);
    		} catch (CoreException e) {
    			LogHelper.logError(e);
    		}
        }
        return marker;
	}
	
    private String filterChars(String s) {
        StringBuilder sb = null;
        for (int i=0; i<s.length(); ++i) {
            char ch = s.charAt(i);
            if (ch < 0x20) {
                // unprintable character found - change with ' '
                if (sb == null) {
                    sb = new StringBuilder(s.length());
                    sb.append(s.substring(0, i));
                }
                sb.append(' ');
            } else if (sb != null) {
                sb.append(ch);
            }
        }
        return sb == null ? s : sb.toString();
    }


	private static boolean deleteXdsMarkers(IProject project, boolean isCleanProjectResourceOnly) {
		try {
			MarkerUtils.deleteBuildProblemMarkers(project, true, isCleanProjectResourceOnly ? IResource.DEPTH_ZERO : IResource.DEPTH_INFINITE);
			return true;
		} catch (CoreException e) {
			LogHelper.logError(e);
			return false;
		}
	}

	private boolean shouldBuild(int kind, IResourceDelta delta, final XdsProjectSettings xdsProjectSettings) {
		if (kind != AUTO_BUILD)
			return true;
		if (delta == null)
			return false;
		
		final boolean[] isShouldBuild = {false};
		try {
            try {
                delta.accept(new IResourceDeltaVisitor() {
                    @Override
                    public boolean visit(IResourceDelta delta)
                            throws CoreException {
                        processDelta(xdsProjectSettings, delta);
                        
                        IResourceDelta[] children = delta.getAffectedChildren();
                        for (int i = 0; i < children.length; i++) {
                            children[i].accept(this);
                        }
                        return true;
                    }

                    private void processDelta(final XdsProjectSettings xdsProjectSettings, IResourceDelta child) {
                        if (isDeltaResourceTargetForXdsProject(xdsProjectSettings, child)) {
                            isShouldBuild[0] = true;
                            throw new InterruptResourceVisitorException();
                        }
                    }
                });
            } catch (InterruptResourceVisitorException ee) {
            }
			
			return isShouldBuild[0];
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
		return isShouldBuild[0];
	}
	
	private boolean isDeltaResourceTargetForXdsProject(
            final XdsProjectSettings xdsProjectSettings, IResourceDelta delta2Check) {
	    if (!delta2Check.getResource().exists() || delta2Check.getResource()
                .getLocation() == null) return false;
	    String changedResourcePath = delta2Check.getResource()
                .getLocation().toOSString();
        String changedResourceName = delta2Check
                .getProjectRelativePath().lastSegment();

        return isFileTargetForXdsProject(xdsProjectSettings, changedResourcePath, changedResourceName);
	}
	
	private boolean isFileTargetForXdsProject(
             final XdsProjectSettings xdsProjectSettings,
             String changedResourcePath, String changedResourceName) {
         return isFileTargetMainModule(xdsProjectSettings, changedResourceName) ||
                 isFileTargetProjectFile(
                         xdsProjectSettings, changedResourcePath,
                         changedResourceName);
     }
	
	private boolean isFileTargetProjectFile(
            final XdsProjectSettings xdsProjectSettings,
            String changedResourcePath, String changedResourceName) {
	    if ( !XdsProjectType.PROJECT_FILE.equals(xdsProjectSettings.getProjectType()) ) return false;
	    String projectFilePath = ResourceUtils.getAbsolutePath(xdsProjectSettings.getProject().getFile(xdsProjectSettings.getXdsProjectFile()));
	    Assert.isNotNull(projectFilePath);
        return (XdsFileUtils.isXdsProjectFile(changedResourceName) && 
                StringUtils.equals(changedResourcePath, projectFilePath) || XdsFileUtils.isCompilationUnitFile(changedResourceName));
    }

    private boolean isFileTargetMainModule(
            final XdsProjectSettings xdsProjectSettings, String fileName) {
        return XdsProjectType.MAIN_MODULE.equals(xdsProjectSettings.getProjectType()) && XdsFileUtils.isCompilationUnitFile(fileName);
    }
	
	private boolean checkCancel(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			// Discard build state if necessary.
			throw new OperationCanceledException();
		}

		if (isInterrupted()) {
			// Discard build state if necessary.
			return true;
		}
		return false;
	}
}
