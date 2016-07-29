package com.excelsior.xds.core.sdk;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.excelsior.xds.core.process.InputStreamListener;
import com.excelsior.xds.core.process.ProcessLauncher;

public class XShellFormatTracker {
	
	public static Sdk.XShellFormat test (Sdk sdk) {
		Sdk.XShellFormat res = Sdk.XShellFormat.UNDEFINED;
		File modFile = null;
		try {
			// Create .mod file
		    File dataDir = new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
			modFile = File.createTempFile("XdsTestTmp", ".mod", dataDir); //$NON-NLS-1$ //$NON-NLS-2$
			modFile.deleteOnExit();
			try(FileOutputStream fos = new FileOutputStream(modFile)){
				fos.write('q'); // non empty. some xv.exe crashes on empty files
			}

			List<String> args = new ArrayList<String>();
	        args.add(sdk.getCompilerExecutablePath());
	        args.add("=make"); //$NON-NLS-1$
	        args.add(modFile.getAbsolutePath());
	        
			final ProcessLauncher procLauncher = new ProcessLauncher();
			procLauncher.setCommandline(args);
			procLauncher.addEnvironment(sdk.getEnvironmentVariablesRaw());
			procLauncher.addEnvironment("__XDS_SHELL__", "[Eclipse]");    // turn on extended compiler output mode //$NON-NLS-1$ //$NON-NLS-2$
			procLauncher.setWorkingDirectory(dataDir);
			
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

			// RUN:
			boolean ok = false;
	        try {
	        	ok = procLauncher.launch();
	        } catch (Exception e) {}
			if (ok) {
				String log = sbXdsOutput.toString();
				// #Error 008: error in module header or import section at 1:1 :
				if (log.contains("\u0001E\u0008\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0001\u0000\u0000\u0000")) { //$NON-NLS-1$
					res = Sdk.XShellFormat.BINARY;
				} else if (log.contains("\u0001E8_1_1_")) { //$NON-NLS-1$
					res = Sdk.XShellFormat.TEXT;
				}
			}
			modFile.delete();
		} catch (Exception e) {}
		return res;
	}

}
