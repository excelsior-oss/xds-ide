package com.excelsior.xds.core.compiler.driver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

import com.excelsior.xds.core.builders.XdsBuildResult;
import com.excelsior.xds.core.console.ColorStreamType;
import com.excelsior.xds.core.console.IXdsConsole;
import com.excelsior.xds.core.console.XdsConsoleLink;
import com.excelsior.xds.core.internal.nls.Messages;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.process.InputStreamListener;
import com.excelsior.xds.core.process.ProcessInputStream;
import com.excelsior.xds.core.process.ProcessLauncher;
import com.excelsior.xds.core.progress.DelegatingProgressMonitor;
import com.excelsior.xds.core.progress.IListenableProgressMonitor;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.Sdk.XShellFormat;
import com.excelsior.xds.core.sdk.SdkManager;

/**
 * TODO : Replace Thread-sleep busy waiting with CountdownLatch
 * TODO : remove IXdsConsole dependency
 */
public class CompileDriver 
{
	public static enum CompilationMode {
        /** Compile single module */ 
		COMPILE_MODULE ("=compile"), //$NON-NLS-1$
		
        /** Check dependencies and compile module */ 
		BUILD_MODULE   ("=make"),    //$NON-NLS-1$

        /** Check dependencies and recompile module */ 
		REBUILD_MODULE ("=make", "=all"), //$NON-NLS-1$ //$NON-NLS-2$
		
		
        /** Check dependencies and compile project */ 
		BUILD_PROJECT    ("=project"), //$NON-NLS-1$

		/** Check dependencies and recompile project */ 
		REBUILD_PROJECT  ("=project",  "=all"), //$NON-NLS-1$ //$NON-NLS-2$
		
		
        /** Show module list */ 
		MAKE_MODULE_LIST ("=make", "-__XDS_LIST__:+"), //$NON-NLS-1$ //$NON-NLS-2$
        
		/** Show project's module list  */ 
		MAKE_PROJECT_MODULE_LIST ("=project", "-__XDS_LIST__:+"), //$NON-NLS-1$ //$NON-NLS-2$

		
		/** Extract definition from symbol file */ 
        BROWSE_SYM     ("=browse"),    //$NON-NLS-1$

        
        /** Show compiler options */ 
        SHOW_OPTIONS   ("=options"),   //$NON-NLS-1$

        /** Show compiler equations */ 
        SHOW_EQUATIONS ("=equations"); //$NON-NLS-1$
		
		public final String[] options;  
		
		CompilationMode (final String... options) {
			this.options = options;
		}
	};
	
	private static final String ODF_TO_STDOUT_MAGIC_BEGIN = "*** BEGIN ODF ***";  //$NON-NLS-1$
	private static final String ODF_TO_STDOUT_MAGIC_END   = "*** END ODF ***";    //$NON-NLS-1$
    private static final String LOOKUP_TO_STDOUT_MAGIC           = "*** Lookup *** = ";         //$NON-NLS-1$
    private static final String LOOKUP_NOT_FOUND_TO_STDOUT_MAGIC = "*** Lookup not found ***";  //$NON-NLS-1$
    private static final String LOOKUPLIST_TO_STDOUT_MAGIC       = "*** Lookup list ***";       //$NON-NLS-1$
    private static final String LOOKUPLIST_TO_STDOUT_MAGIC_END   = "*** Lookup list end ***";   //$NON-NLS-1$


	private final Sdk sdk;
	private final IXdsConsole console;
	private final IListenableProgressMonitor monitor;
	
	private volatile boolean isStdoutEof;

	
	public CompileDriver (Sdk sdk, IXdsConsole console, IListenableProgressMonitor  monitor) {
		this.sdk = sdk;
		this.console = console;
		this.monitor = monitor;
	}
	
	public CompileDriver (Sdk sdk) {
		this(sdk, null, DelegatingProgressMonitor.nullProgressMonitor());
	}

    public List<String> getModuleList(String srcFile) {
        return getModuleList(srcFile, null);
    }
	
    public List<String> getModuleList(String srcFile, String prjFile) {
        final List<String> moduleList = new ArrayList<String>();
        if (prjFile == null) {
            doCompile( sdk, CompilationMode.MAKE_MODULE_LIST
                     , srcFile, null
                     , new ModuleListListener(moduleList, FilenameUtils.getFullPath(srcFile)) );
        } else {
            File workDirectory = (new File(prjFile)).getParentFile();
            String arguments = "-prj=" + prjFile; //$NON-NLS-1$
            doCompile( sdk, CompilationMode.MAKE_MODULE_LIST
                     , srcFile, workDirectory
                     , new ModuleListListener(moduleList, FilenameUtils.getFullPath(srcFile)) 
                     , arguments );
        }
        return moduleList;
    }
    
    public List<String> getProjectModuleList(String prjFile) {
        final List<String> moduleList = new ArrayList<String>();
        doCompile( sdk, CompilationMode.MAKE_PROJECT_MODULE_LIST
                 , prjFile, null
                 , new ModuleListListener(moduleList, FilenameUtils.getFullPath(prjFile)) );
        return moduleList;
    }


	public XdsBuildResult compileModule ( String srcFile, File compileDir
                              , boolean isRebuild, boolean isMake
                              , IXShellListener listener )
	{
		return compileModule(srcFile, null, compileDir, isRebuild, isMake, listener);
	}

	public XdsBuildResult compileModule ( String srcFile, String prjFile, File compileDir
			                  , boolean isRebuild, boolean isMake
			                  , IXShellListener listener ) 
	{
		CompilationMode mode = CompilationMode.COMPILE_MODULE;
		if (isMake) {
			mode = isRebuild ? CompilationMode.REBUILD_MODULE
					         : CompilationMode.BUILD_MODULE;
		}

		if (prjFile == null) {
			return doCompile(sdk, mode, srcFile, compileDir, listener);	
		} else {
		    if (compileDir == null) {
		        compileDir = (new File(prjFile)).getParentFile();
		    }
	        String arguments = "-prj=" + prjFile; //$NON-NLS-1$
	        return doCompile(sdk, mode, srcFile, compileDir, listener, arguments); 
		}
	}
	
	
    public XdsBuildResult compileProject ( String prjFile, File compileDir, boolean isRebuild
                                         , IXShellListener listener ) 
    {
		CompilationMode mode = isRebuild ? CompilationMode.REBUILD_PROJECT
		                                 : CompilationMode.BUILD_PROJECT;
		return doCompile(sdk, mode, prjFile, compileDir, listener);	
    }
	
	
    private XdsBuildResult doCompile ( Sdk sdk, CompilationMode mode
                                     , String fileName
                                     , File workDirectory
                                     , IXShellListener listener
                                     , String... arguments ) 
	{
	    XdsBuildResult rc = XdsBuildResult.ERROR;
	    if (fileName == null) {
	    	return rc;
	    }
        File file = new File(fileName);
        if (workDirectory == null) {
            workDirectory = file.getParentFile();
        }

        List<String> args = new ArrayList<String>();
        args.add(sdk.getCompilerExecutablePath());
        for (String option : mode.options)
            args.add(option);
        args.add(file.getAbsolutePath());
        for (String arg : arguments) {
        	if (arg != null)
        	    args.add(arg);
        }
        
        
		ProcessLauncher procLauncher = new ProcessLauncher();
		procLauncher.setCommandline(args);
		procLauncher.addEnvironment(sdk.getEnvironmentVariables());
		// turn on extended compiler output mode:
		procLauncher.addEnvironment("__XDS_SHELL__", "[Eclipse],[UseOEM]");  //$NON-NLS-1$ //$NON-NLS-2$
		procLauncher.setWorkingDirectory(workDirectory);
		procLauncher.setMonitor(monitor);
		if (console != null) {
		    procLauncher.setConsole(console, true);
		}

		
		{
		    StringBuilder sb = new StringBuilder();
		    for (String s : args) {
		        if (s.indexOf(' ') >=0) {
                    sb.append('"').append(s).append("\" "); //$NON-NLS-1$
		        } else {
		            sb.append(s).append(' ');
		        }
		    }
		    if (console != null) {
		        console.printlnFiltered(sb.toString(), ColorStreamType.SYSTEM);
		    }
		}
		
		XShellFormat xShellFormat = sdk.getXShellFormat();

		@SuppressWarnings("resource")
		final ProcessInputStream queueStdoutStream = new ProcessInputStream();
        final XShell xShellOut = new XShell(listener, xShellFormat);
		procLauncher.addProcessStdoutListener(new InputStreamListener() {
			@Override
			public void onHasData(byte[] buffer, int length) {
				queueStdoutStream.pushData(buffer, 0, length);
				xShellOut.onHasData(buffer, length);
			}

			@Override
			public void onEndOfStreamReached() {
				queueStdoutStream.setEOF();
                xShellOut.onEndOfStreamReached();
			}
		});
		
		@SuppressWarnings("resource")
		final ProcessInputStream queueStderrStream = new ProcessInputStream();
        final XShell xShellErr = new XShell(listener, xShellFormat);
		procLauncher.addProcessStderrListener(new InputStreamListener() {
			@Override
			public void onHasData(byte[] buffer, int length) {
				queueStderrStream.pushData(buffer, 0, length);
                xShellErr.onHasData(buffer, length);
			}

			@Override
			public void onEndOfStreamReached() {
				queueStderrStream.setEOF();
                xShellErr.onEndOfStreamReached();
			}
		});
		
		
		// RUN:
		boolean ok = false;
        try {
        	ok = procLauncher.launch();
        } catch (Exception e) {
            listener.onParsingError("Compiler execution error : " + e.getMessage()); //$NON-NLS-1$
			LogHelper.logError(e);
        }
        
        if (console != null) {
            console.setEncoding(null); // reset to default (to print Strings from java)
            console.println("");
        }
        
        if (ok) {
            int rescode = procLauncher.exitValue();
            if (console != null) {
                if (rescode == 0) {
                    console.printlnFiltered(Messages.CompileDriver_CompilerExitCode + rescode, ColorStreamType.SYSTEM);
                } else {
                    console.println(Messages.CompileDriver_CompilerExitCode + rescode, 
                                    ColorStreamType.ERROR, XdsConsoleLink.mkLinkToProblemsView());
                }
            }
            if (rescode == 0) {
                rc = XdsBuildResult.SUCCESS;
            }
            listener.onCompilerExit(rescode);
        } else {
            if (console != null) {
                console.println(Messages.CompileDriver_CompilerWasTerminated,
                                ColorStreamType.ERROR, XdsConsoleLink.mkLinkToProblemsView());
            }
            rc = XdsBuildResult.TERMINATED;
        }
        return rc;
	}
	
    
    static class ModuleListListener extends XShellListener {
        private final List<String> moduleList;
        private final String workingDirectory;
        
        public ModuleListListener(List<String> moduleList, String workingDirectory) {
            this.moduleList = moduleList;
            this.workingDirectory = workingDirectory;
        }
        
        @Override
        public void onModuleListStart() {
            synchronized (moduleList) {
                moduleList.clear();
            }
        }

        @Override
        public void onModuleListAppend(String fileName) {
            File file = new File(fileName);
            
            if (!file.isAbsolute()) {
                fileName = FilenameUtils.concat(workingDirectory, fileName); 
            }
            
            synchronized (moduleList) {
                moduleList.add(fileName);
            }
        }

        /* Notify that file list is complete. If compiling is terminated
         * before this point, and therefore this function is never called, 
         * then received file list is incomplete.
         */
        @Override
        public void onModuleListCommit() {
        }

        @Override
        public void onParsingError(String message) {
            synchronized (moduleList) {
                moduleList.clear();
            }
        }
        
    }
 

    /**
     * Runs compiler simulator (xn.exe)
     *  
     * @param args - compiler arguments 
     * @param workDirectory - compiler working directory 
     * 
     * @return output of compiler simulator
     * @throws CoreException
     */
    private String runCompNull (List<String> args, File workDirectory) throws CoreException
    {
        String xnExe = null;
        Sdk xn_sdk = null;
        try {
			xn_sdk = SdkManager.getInstance().getSdkSimulator();
		} catch (IOException e1) {
			IStatus status = LogHelper.createStatus(IStatus.ERROR, IStatus.ERROR, "Unexpected Exception",e1);
			throw new CoreException(status);
		}
        xnExe = xn_sdk.getCompilerExecutablePath();
        
        List<String> allArgs = new ArrayList<String>();
        allArgs.add(xnExe);
        allArgs.addAll(args);
        
        ProcessLauncher procLauncher = new ProcessLauncher();
        procLauncher.setCommandline(allArgs);
        if (sdk != null) {
            procLauncher.addEnvironment(sdk.getEnvironmentVariables());
        }
        procLauncher.setWorkingDirectory(workDirectory);
        procLauncher.setMonitor(monitor);

        if (console != null) {
            procLauncher.setConsole(console, true); 
            { // Print xn.exe cmdline to console:
                StringBuilder sb = new StringBuilder();
                for (String s : allArgs) {
                    if (s.indexOf(' ') >=0) {
                        sb.append('"').append(s).append("\" "); //$NON-NLS-1$
                    } else {
                        sb.append(s).append(' ');
                    }
                }
                console.printlnFiltered(sb.toString(), ColorStreamType.SYSTEM);
            }
        }
        
        final StringBuilder sbStdout = new StringBuilder();
        isStdoutEof = false;

        procLauncher.addProcessStdoutListener(new InputStreamListener() {
            @Override
            public void onHasData(byte[] buffer, int length) {
                for (int i = 0; i < length; ++i) {
                    sbStdout.append((char)buffer[i]);
                }
            }

            @Override
            public void onEndOfStreamReached() {
                isStdoutEof = true;
            }
        });
        
        // RUN:
        boolean ok = procLauncher.launch(); // may throw error
        
        if (console != null) {
            console.setEncoding(null); // reset to default (to print Strings from java)
            int rescode = 1;
            if (ok) {
                rescode = procLauncher.exitValue();
                console.println(Messages.CompileDriver_CompilerExitCode + rescode, 
                        rescode == 0 ? ColorStreamType.SYSTEM : ColorStreamType.ERROR);
            } else {
                console.println(Messages.CompileDriver_CompilerWasTerminated, ColorStreamType.ERROR);
            }
        }
        
        // wait for EOF of xn.exe stdout:
        while (!isStdoutEof) {
            try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				LogHelper.logError(e);
				break;
			}
        }
        return sbStdout.toString();
    }

    
    /**
     * Runs compiler simulator (xn.exe) and returns its output.
     *   
     * @param mode - compilation mode 
     * @param prjFile - XDS project file name (*.prj) or </tt>null<tt> 
     * @param compilerArgs - compiler arguments 
     * @param workDirectory - compiler working directory 
     *
     * @return compiler simulator output or </tt>null<tt>
     */
    public String getCompilerSimulatorOutput( CompilationMode mode, File prjFile
                                            , List<String> compilerArgs, File workDirectory) 
    {
        List<String> args = new ArrayList<String>();
        args.addAll(compilerArgs);
        if (prjFile != null) {
            args.add("-prj=" + prjFile.getAbsolutePath()); //$NON-NLS-1$
        }
        args.addAll(Arrays.asList(mode.options));
        if (sdk != null) {
            args.add("-run_as_alias=" + sdk.getCompilerExecutablePath()); //$NON-NLS-1$
        }
        
        try {
            return runCompNull(args, workDirectory);
        } catch (CoreException e) {
            LogHelper.logError(e);
        }
        
        return null;
    }
    
    
    
    /**
     * Converts the given XDS symbol file into source text in the odf-format.
     * odf - oberon pseudo-definition file. 
     *  
     * @param symFile - absolute path to the XDS symbol file name (*.sym) to be processed
     * 
     * @return source text in the odf-format, or 
     *         <tt>null</tt> when cannot be processed
     *         
     * TODO : check every call of this method, whether this can be worked around by creating a temporary *.sym file for the fileStore.
     */
    public static String decodeSymFile(String symFile)
    {
    	if (symFile == null){
    		return null;
    	}
        CompileDriver compileDriver = new CompileDriver(
            null, null, DelegatingProgressMonitor.nullProgressMonitor()
        );
        
        return compileDriver.decodeSymFile(symFile, null, null);
    }

    /**
     * Converts the given XDS symbol file into source text in the odf-format.
     * odf - oberon pseudo-definition file. 
     *  
     * @param symFile - XDS symbol file name (*.sym) (with or without path) to be processed
     * @param buildSettings - XDS compiler settings 
     * 
     * @return source text in the odf-format, or 
     *         <tt>null</tt> when cannot be processed
     */
    // Not used: it seems that =browse works ok w/o any .prj and lookups from it.
    // But when .prj is specified and project source modules are found by xn.exe 
    // it may begin to try to format output .odf file and change its contents. So to have
    // exactly same text for one .sym file - use static String decodeSymFile(String symFile) only.
    //
//    public static String decodeSymFile(String symFile, BuildSettings buildSettings)
//    {
//        String prjFile = buildSettings.getPrjFile() != null
//                       ? buildSettings.getPrjFile().getAbsolutePath() 
//                       : null;
//        CompileDriver compileDriver = new CompileDriver(
//            buildSettings.getSdk(), null, new NullProgressMonitor()
//        );
//        
//        return compileDriver.decodeSymFile(symFile, prjFile, buildSettings.getWorkDir());
//    }
    
    /**
     * Converts the given XDS symbol file into source text in the odf-format.
     * odf - oberon pseudo-definition file. 
     *  
     * @param fileName - XDS symbol file name (*.sym) (with or without path) to be processed
     * @param prjFile - XDS project file name (*.prj) or </tt>null<tt> 
     * @param workDirectory - the working directory for xn.exe (or <tt>null</tt> to use directory of symFile)
     * 
     * @return source text in the odf-format, or 
     *         <tt>null</tt> when cannot be processed
     */
    private String decodeSymFile(String fileName, String prjFile, File workDirectory) 
    {
        try {
            File file = new File(fileName);
            if (workDirectory == null) {
                workDirectory = file.getParentFile();
            }
            
            List<String> args = new ArrayList<String>();
            args.add("=browse"); //$NON-NLS-1$
            if (sdk != null) {
                args.add("-run_as_alias=" + sdk.getCompilerExecutablePath()); //$NON-NLS-1$
            }
            if (prjFile != null) {
                args.add("-prj=" + prjFile); //$NON-NLS-1$
            }
            args.add("+odf_to_stdout"); //$NON-NLS-1$
            args.add(fileName);
    
            String res = runCompNull(args, workDirectory);
    
            int pos = res.indexOf(ODF_TO_STDOUT_MAGIC_BEGIN);
            if (pos > 0) {
                res = res.substring(pos + ODF_TO_STDOUT_MAGIC_BEGIN.length());
                pos = res.indexOf(ODF_TO_STDOUT_MAGIC_END);
                if (pos > 0) {
                    res = res.substring(0, pos);
                    return res.trim();
                }
            }
            throw new Exception("decodeSymFile internal error: no .odf body found in xn.exe stdout for " + fileName); //$NON-NLS-1$
        } catch (Exception e) {
            LogHelper.logError(e);
        }
        return null;
    }
   
    /**
     * @param fileName - (without extension => searches .def, .ob2 and .sym in this order)
     * @param prjFile - .prj file name or null 
     * @param workDirectory - compiler working directory (required!)
     * @return file or null
     */
    public File lookupFile(String fileName, String prjFile, File workDirectory) 
    {
        try {
            if (workDirectory == null) {
                throw new IllegalArgumentException("workDirectory is null"); //$NON-NLS-1$
            }

            List<String> args = new ArrayList<String>();
            args.add("=lookup"); //$NON-NLS-1$
            if (sdk != null) {
                args.add("-run_as_alias=" + sdk.getCompilerExecutablePath()); //$NON-NLS-1$
            }
            if (prjFile != null) {
                args.add("-prj=" + prjFile); //$NON-NLS-1$
            }
            args.add(fileName);
    
            String res = runCompNull(args, workDirectory);
    
            if (res.contains(LOOKUP_NOT_FOUND_TO_STDOUT_MAGIC)) {
                return null; // no errors, not found
            } else { 
                int pos = res.indexOf(LOOKUP_TO_STDOUT_MAGIC);
                if (pos > 0) {
                    res = res.substring(pos + LOOKUP_TO_STDOUT_MAGIC.length());
                    pos = res.indexOf('\r');
                    if (pos > 0) {
                        res = res.substring(0, pos);
                    }
                    pos = res.indexOf('\n');
                    if (pos > 0) {
                        res = res.substring(0, pos);
                    }
                    res = res.trim();
                    File f = new File(res);
                    if (!f.isAbsolute()) {
                        f = new File(workDirectory, res);    
                    }
                    if (!f.exists()) {
                        throw new Exception("lookupFile: file '" + f.getAbsolutePath() + "' not found"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    return f;
                }
            }
            throw new Exception("lookupFile internal error: no lookup magic found in xn.exe stdout"); //$NON-NLS-1$
        } catch (Exception e) {
            LogHelper.logError(e);
        }
        return null;
    }
   
    
    /**
     * @param prjFile - .prj file name or null 
     * @param workDirectory - compiler working directory (required!)
     * @return lines like "*.sym=./sym;c:/zz" delimited with newline or null when smth is wrong
     */
    public String getLookupEquations(String prjFile, File workDirectory) 
    {
        try {
            if (workDirectory == null) {
                throw new IllegalArgumentException("workDirectory is null"); //$NON-NLS-1$
            }

            List<String> args = new ArrayList<String>();
            args.add("=lookup"); //$NON-NLS-1$
            if (sdk != null) {
                args.add("-run_as_alias=" + sdk.getCompilerExecutablePath()); //$NON-NLS-1$
            }
            if (prjFile != null) {
                args.add("-prj=" + prjFile); //$NON-NLS-1$
            }

            String res = runCompNull(args, workDirectory);

            int pos = res.indexOf(LOOKUPLIST_TO_STDOUT_MAGIC);
            if (pos > 0) {
                res = res.substring(pos + LOOKUPLIST_TO_STDOUT_MAGIC.length());
                pos = res.indexOf(LOOKUPLIST_TO_STDOUT_MAGIC_END);
                if (pos > 0) {
                    res = res.substring(0, pos);
                    return res;
                }
            }
            throw new Exception("getAllLookups internal error: no lookup list magic(s) found in xn.exe stdout"); //$NON-NLS-1$
        } catch (Exception e) {
            LogHelper.logError(e);
        }
        return null;
    }

}