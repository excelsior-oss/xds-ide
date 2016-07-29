package com.excelsior.xds.builder.buildsettings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.builder.internal.buildsettings.parser.AbstractBuildSettingsParser;
import com.excelsior.xds.builder.internal.buildsettings.parser.EquationParser;
import com.excelsior.xds.builder.internal.buildsettings.parser.OptionParser;
import com.excelsior.xds.builder.listener.BuildListenerManager;
import com.excelsior.xds.builder.listener.IBuilderListener;
import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.builders.BuildSettingsKey;
import com.excelsior.xds.core.builders.BuildSettingsKeyFactory;
import com.excelsior.xds.core.builders.DefaultBuildSettingsHolder;
import com.excelsior.xds.core.builders.XdsBuildResult;
import com.excelsior.xds.core.compiler.driver.CompileDriver;
import com.excelsior.xds.core.compiler.driver.CompileDriver.CompilationMode;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.process.InputStreamListener;
import com.excelsior.xds.core.process.ProcessLauncher;
import com.excelsior.xds.core.progress.DelegatingProgressMonitor;
import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.resource.XdsResourceChangeListener;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkManager;
import com.excelsior.xds.core.utils.XdsFileUtils;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;
import com.excelsior.xds.core.utils.time.ModificationStamp;

public final class BuildSettingsCache extends XdsResourceChangeListener{
    /**
     *  options from xc larger than this will be truncated to this
     */
    private static final int OPTION_TRUNCATION_LENGTH = 14;
    /**
     *  equations from xc larger than this will be truncated to this
     */
    private static final int EQUATION_TRUNCATION_LENGTH = 13;
    
	private static final String ERROR_OCCURED_WHILE_GETTING_BUILD_SETTINGS_MESSAGE = "Error occured while getting build settings";
    private static final String XDS_COMPILER_PROCESS_FINISHED_WITH_ERROR_MESSAGE   = "Xds compiler process finished with error";
    
    private final ReadWriteLock buildSettingsCacheLock = new ReentrantReadWriteLock();
    /**
     * Holds instances of {@link BuildSettings} (aggregated in the {@link BuildSettingsData}).<br>
     * Guarded by {@link #buildSettingsCacheLock}.
     */
    private final Map<BuildSettingsKey, BuildSettingsData> buildSettingsCache = new HashMap<BuildSettingsKey, BuildSettingsData>();
    
    private Set<IBuildSettingsCacheListener> buildSettingsCacheListeners = CollectionsUtils.newConcurentHashSet(3);
    
    private IBuilderListener builderListener = new BuilderListener();
    
    private BuildSettingsCache() {
    	super();
        try {
            BuildListenerManager.getInstance().addListener(builderListener);
        }
        catch(IllegalStateException e) {
            // can happen when launched from the unit test in the standalone mode 
        }
    }
    
    public static void addListener(IBuildSettingsCacheListener l) {
    	BuildSettingsFactoryHolder.INSTANCE.internalAddListener(l);
    }
    
    public static void removeListener(IBuildSettingsCacheListener l) {
    	BuildSettingsFactoryHolder.INSTANCE.internalRemoveListener(l);
    }
    
    public static BuildSettings createBuildSettings(IFile sourceIFile ) {
        return BuildSettingsFactoryHolder.INSTANCE.internalCreateBuildSettings(sourceIFile);
    }
    
    public static BuildSettings createBuildSettings(IProject iProject ) {
        return BuildSettingsFactoryHolder.INSTANCE.internalCreateBuildSettings(iProject);
    }
    
    public static BuildSettings createBuildSettings(IProject iproject, File sourceFile) {
        return BuildSettingsCache.internalCreateBuildSettings(iproject, sourceFile);
    }
    
    public static BuildSettings createBuildSettings(Sdk sdk, File workingDir, File prjFile) {
       return BuildSettingsFactoryHolder.INSTANCE.internalCreateBuildSettings(new BuildSettingsKey(sdk, workingDir, prjFile));
    }
    
    /**
     * Invalidates build settings cache for the given xds project
     * @param p
     */
    public static void invalidateBuildSettingsCache(IProject p) {
        BuildSettingsFactoryHolder.INSTANCE.internalInvalidateBuildSettingsCache(p);
    }
    
    /**
     * Invalidates build settings cache for the all xds projects
     */
    public static void invalidateBuildSettingsCache() {
        BuildSettingsFactoryHolder.INSTANCE.internalInvalidateBuildSettingsCache();
    }
    
    private void internalInvalidateBuildSettingsCache() {
        List<IProject> xdsProjects = ProjectUtils.getXdsProjects();
        for (IProject p : xdsProjects) {
            internalInvalidateBuildSettingsCache(p);
        }
    }
    
    private void internalInvalidateBuildSettingsCache(IProject p) {
        if (ProjectUtils.isXdsProject(p)) {
            XdsProjectSettings projectSettings = XdsProjectSettingsManager.getXdsProjectSettings(p);
            
            File projectFile;
            
            {
                IFile projectFileResource = ProjectUtils.getPrjFile(projectSettings);
                if (projectFileResource != null) {
                    projectFile = ResourceUtils.getAbsoluteFile(projectFileResource);
                }
                else {
                    projectFile = null;
                }
            }
            
            Lock writeLock = buildSettingsCacheLock.writeLock();
            try{
            	writeLock.lock();
            	for (Iterator<BuildSettingsKey> keyIterator = buildSettingsCache.keySet().iterator(); keyIterator.hasNext();) {
                    BuildSettingsKey key = (BuildSettingsKey) keyIterator.next();
                    boolean isDelete = false;
                    if (projectFile == null && key.prjFile == null) {
                        isDelete = true;
                    }
                    else{
                        if ((projectFile != null && key.prjFile != null) &&
                                ResourceUtils.equalsPathesAsInFS(projectFile, key.prjFile)) {
                            isDelete = true;
                        }
                    }
                    if (isDelete){
                        keyIterator.remove();
                    }
                }
            }
            finally {
            	writeLock.unlock();
            }
            internalNotifyReload(p);
        }
    }

    private BuildSettings internalCreateBuildSettings(IFile sourceIFile ) {
    	BuildSettingsKey buildSettingsKey;
    	if (sourceIFile == null) {
    		buildSettingsKey = DefaultBuildSettingsHolder.DefaultBuildSettingsKey;
    	}
    	else {
    		buildSettingsKey = BuildSettingsKeyFactory.createBuildSettingsKey(sourceIFile.getProject());
    	}
        return BuildSettingsFactoryHolder.INSTANCE.internalCreateBuildSettings(buildSettingsKey);
    }
    
    private BuildSettings internalCreateBuildSettings(IProject project) {
    	BuildSettingsKey buildSettingsKey = BuildSettingsKeyFactory.createBuildSettingsKey(project);
    	if (buildSettingsKey == null){
    		buildSettingsKey = DefaultBuildSettingsHolder.DefaultBuildSettingsKey;
    	}
    	return BuildSettingsFactoryHolder.INSTANCE.internalCreateBuildSettings(buildSettingsKey);
    }

	private static BuildSettings internalCreateBuildSettings(IProject project,
			File sourceFile) {
        BuildSettingsKey buildSettingsKey = BuildSettingsKeyFactory.createBuildSettingsKey(project, sourceFile);
        if (buildSettingsKey == null){
        	buildSettingsKey = DefaultBuildSettingsHolder.DefaultBuildSettingsKey;
        }
        return BuildSettingsFactoryHolder.INSTANCE.internalCreateBuildSettings(buildSettingsKey);
	}
    
    private BuildSettings internalCreateBuildSettings(BuildSettingsKey key) {
    	Sdk sdk = key.sdk;
    	File workingDir = key.workingDir;
    	File prjFile = key.prjFile;
    	
        if (sdk == null) {
            if (prjFile == null) {
                return DefaultBuildSettingsHolder.DefaultBuildSettings;
            }
            try {
                sdk = SdkManager.getInstance().getSdkSimulator();
            } catch (IOException e) {
                LogHelper.logError("Build settings creation error", e);   //$NON-NLS-1$
                return DefaultBuildSettingsHolder.DefaultBuildSettings;
            }
        }
        
        {
        	Lock readLock = buildSettingsCacheLock.readLock();
        	try{
        		readLock.lock();
        		BuildSettingsData buildSettingsData = buildSettingsCache.get(key);
        		if (buildSettingsData != null && buildSettingsData.buildSettings != null){
        			return buildSettingsData.buildSettings;
        		}
        	}
        	finally{
        		readLock.unlock();
        	}
        }
        
        BuildSettings buildSettings = new BuildSettings(sdk, workingDir, prjFile);
        
        String compilerExe = sdk.getCompilerExecutablePath();
        List<String> args = new ArrayList<String>(3);
        
        if (prjFile != null) {
            args = Arrays.asList("-prj=" + prjFile.getAbsolutePath()); //$NON-NLS-1$
        }
        
        try {
            String options = getCompilerOutput(sdk, "=options", args, workingDir, compilerExe); //$NON-NLS-1$
            if (options == null) {
            	return DefaultBuildSettingsHolder.DefaultBuildSettings;
            }
            parseOptions(buildSettings, options);
            
            String equations = getCompilerOutput(sdk, "=equations", args, workingDir, compilerExe); //$NON-NLS-1$
            if (equations != null) {
            	parseEquations(buildSettings, equations);
            }
            String lookupEquations = getLookupEquations(sdk, workingDir, prjFile);
            if (lookupEquations != null) {
            	buildSettings.setLookupEquations(lookupEquations);
            }
        } catch (CoreException e) {
            LogHelper.logError(e);
        } catch (IOException e) {
            LogHelper.logError(ERROR_OCCURED_WHILE_GETTING_BUILD_SETTINGS_MESSAGE, e);
        }
        
        Lock writeLock = buildSettingsCacheLock.writeLock();
        try{
        	writeLock.lock();
        	BuildSettingsData oldBuildSettingsData = buildSettingsCache.get(key);
        	ModificationStamp modificationStamp = new ModificationStamp();
        	if (oldBuildSettingsData == null || modificationStamp.isGreaterThan(oldBuildSettingsData.modificationStamp)) {
        		buildSettingsCache.put(key, new BuildSettingsData(buildSettings, modificationStamp));
        	}
        }
        finally{
        	writeLock.unlock();
        }
        
        return buildSettings;
    }
    
    /**
     * SUMMARY : Parses {@code options}, add them to buildSettings <br><br>
     * 
     * For the options (from xc) with the name longer then {@link #OPTION_TRUNCATION_LENGTH} the following algorithm is used: <br>
     * Note that name from xc name longer then {@link #OPTION_TRUNCATION_LENGTH} can only have the following form : {@code (14-symbol prefix)'...'}<br><br>
     * 
     * Common prefix of length {@link #OPTION_TRUNCATION_LENGTH} is substringed, number of names for each such common prefix is computed. <br>
     * 1) if this number equals 1 - corresponding name (with this prefix) from parsed from xn is used
     * 2) if this number greater than 1 -  corresponding (optionName, optionValue) from parsed from xn is used<br><br>
     * 
     * Please see KIDE-329 for more<br>
     * @param buildSettings
     * @param options options from xc - raw
     * @throws IOException
     */
    private static void parseOptions( final BuildSettings buildSettings
                                               , String options ) throws IOException {
		AbstractBuildSettingsParser<Boolean> optionParser = new AbstractBuildSettingsParser<Boolean>(
				buildSettings.getSdk(), buildSettings.getPrjFile(),
				buildSettings.getWorkDir(), OPTION_TRUNCATION_LENGTH,
				new OptionParser(), CompilationMode.SHOW_OPTIONS) {
			@Override
			protected void settingParsed(String name, Boolean value) {
				buildSettings.addOption(name, value);
			}
		};
		optionParser.parse(options);
    }
    
	private static void parseEquations(final BuildSettings buildSettings,
			String equations) throws IOException {
		AbstractBuildSettingsParser<String> equationParser = new AbstractBuildSettingsParser<String>(
				buildSettings.getSdk(), buildSettings.getPrjFile(),
				buildSettings.getWorkDir(), EQUATION_TRUNCATION_LENGTH,
				new EquationParser(), CompilationMode.SHOW_EQUATIONS) {
			@Override
			protected void settingParsed(String name, String value) {
				buildSettings.addEquation(name, value);
			}
		};
		equationParser.parse(equations);
	}

    private static String getCompilerOutput( Sdk sdk, String compilerMode
                                           , List<String> compilerArgs
                                           , File workingDir, String compilerExe ) throws CoreException
    {
        ProcessLauncher procLauncher = new ProcessLauncher();
        procLauncher.addEnvironment(sdk.getEnvironmentVariables());
        procLauncher.setWorkingDirectory(workingDir);
        
        final StringBuilder sbXdsOutput = new StringBuilder();
        procLauncher.addProcessStdoutListener(new InputStreamListener() {
            @Override
            public void onHasData(byte[] buffer, int length) {
                sbXdsOutput.append(new String(buffer, 0, length));
            }

            @Override
            public void onEndOfStreamReached() {
            }
        });

        List<String> args = new ArrayList<String>();
        args.add(compilerExe);
        args.addAll(compilerArgs);

        args.add(compilerMode);
        procLauncher.setCommandline(args);
        procLauncher.launch();
        if (procLauncher.exitValue() != 0) {
            LogHelper.logError(XDS_COMPILER_PROCESS_FINISHED_WITH_ERROR_MESSAGE);
            return null;
        }
        return sbXdsOutput.toString();
    }

    
    private static String getLookupEquations(Sdk sdk, File workingDir, File prjFile) {
        CompileDriver cd = new CompileDriver(sdk, null, DelegatingProgressMonitor.nullProgressMonitor());
        String prjFileLocation = (prjFile != null) ? prjFile.getAbsolutePath() : null;
        String lookups = cd.getLookupEquations(prjFileLocation, workingDir);
        return lookups;
    }
    
    
    final Set<IProject> affectedProjects = new HashSet<IProject>();
    
	@Override
	protected void endDeltaProcessing(IResourceDelta delta) {
		for (IProject p : affectedProjects) {
            invalidateBuildSettingsCache(p);
        }
		affectedProjects.clear();
	}

	@Override
	protected boolean handleResourceChanged(IResourceDelta rootDelta, IResourceDelta delta,
			IResource r) {
        rememberAffectedProject(r);
		return super.handleResourceChanged(rootDelta, delta, r);
	}

	@Override
	protected boolean handleResourceRemoved(IResourceDelta rootDelta, IResourceDelta delta,
			IResource r) {
		rememberAffectedProject(r);
		return super.handleResourceRemoved(rootDelta, delta, r);
	}
	
	private void rememberAffectedProject(IResource r) {
		if (r instanceof IFile) {
            IFile ifile = (IFile)r;
            // if this ifile is xds project file - we will invalidate
            // build settings cache for its project (since this project file is changed)
            String path = ResourceUtils.getAbsolutePath(ifile);
            if (XdsFileUtils.isXdsProjectFile(path)){
            	affectedProjects.add(ifile.getProject());
            }
        }
    }
	
	private void internalAddListener(IBuildSettingsCacheListener l) {
    	buildSettingsCacheListeners.add(l);
    }
    
    private void internalRemoveListener(IBuildSettingsCacheListener l) {
    	buildSettingsCacheListeners.remove(l);
    }
    
    private void internalNotifyReload(IProject p) {
    	for (IBuildSettingsCacheListener l : buildSettingsCacheListeners) {
			l.buildSettingsReload(p);
		}
    }
    
    private final class BuilderListener implements IBuilderListener {
		@Override
		public void onBuildStarted(IProject p) {
			internalInvalidateBuildSettingsCache(p);
		}

		@Override
		public void onBuildFinished(IProject p, XdsBuildResult buildRes) {
		}
	}

	private static class BuildSettingsFactoryHolder{
        static BuildSettingsCache INSTANCE  = new BuildSettingsCache();
    }
	
	/**
	 * Holds {@link BuildSettings} and {@link ModificationStamp} when instance of {@link BuildSettings} was put in to the {@link BuildSettingsCache#buildSettingsCache}
	 * @author lsa80
	 */
	private static class BuildSettingsData {
		final BuildSettings buildSettings;
		final ModificationStamp modificationStamp;
		
		public BuildSettingsData(BuildSettings buildSettings,
				ModificationStamp modificationStamp) {
			this.buildSettings = buildSettings;
			this.modificationStamp = modificationStamp;
		}
	}
}
