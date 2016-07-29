package com.excelsior.xds.core.ldp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.exceptions.ExceptionHelper;
import com.excelsior.xds.core.log.LogHelper;

public final class LdpFileUtils {
	public static List<File> parseLdp(File fLdp) throws CoreException {
		List<File> pktList = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(fLdp))) {
            String str;
            while((str = reader.readLine()) != null) {
                str = str.trim();
                if (str.isEmpty() || str.startsWith("#") || str.startsWith(";")) { //$NON-NLS-1$ //$NON-NLS-2$
                    continue;
                }
                File f = new File(str);
                if (!f.isFile()) {
                    if (!f.isAbsolute()) {
                        f = new File(fLdp.getParent(), f.getPath()); // try .ldp's directory
                    }
                    if (!f.isFile()) {
//                    	abortLaunch(String.format(Messages.LaunchDelegatePkt_LdpRefersToUnexistentPkt,
//                                fLdp.getAbsolutePath(), str));
                    	throw new PktFileNotFound(f);
                    }
                }
                pktList.add(f);
            }
        } catch(IOException e) {
        	ExceptionHelper.rethrowAsCoreException(e);
        }
        return pktList;
    }
	
	/**
	 * Thrown when LDP file refers to the PKT file which doesnot exist.
	 * @author lsa80
	 */
	public static class PktFileNotFound extends CoreException {
		private static final long serialVersionUID = 1L;
		private final File pktFile;
		public PktFileNotFound(File pktFile) {
			super(LogHelper.createErrorStatus(String.format("Ldp file not found %s", pktFile.getAbsoluteFile())));
			this.pktFile = pktFile;
		}
		
		public File getPktFile() {
			return pktFile;
		}
	}
	
	private LdpFileUtils(){
	}
}
