package com.excelsior.xds.core.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.excelsior.xds.core.text.TextEncoding;
import com.excelsior.xds.core.text.TextEncoding.CodepageId;

public final class EncodingUtils {
	public static final String DOS_ENCODING = "Cp866"; //$NON-NLS-1$
	
	/**
	 * In the case resource is IFile, its encoding is guessed from its first bytes (see {@link TextEncoding#readFileAndCodepage(java.io.InputStream, StringBuilder, CodepageId[])}) 
	 * @param r
	 * @param monitor
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws CoreException 
	 */
	public static void determineAndSetEncoding(IFile f, IProgressMonitor monitor) throws IOException, CoreException {
		if (!f.exists()) {
			return;
		}
		CodepageId cpId[] = {null};
		try(InputStream stream = f.getContents()){
			TextEncoding.readFileAndCodepage(stream, null, cpId);
			if (cpId[0] == null) {
				cpId[0] = null;
			}
			if(cpId[0] != null && !cpId[0].charsetName.equalsIgnoreCase(f.getCharset(true))) {
				f.setCharset(cpId[0].charsetName, monitor);
			}
		}
	}
	
	private EncodingUtils(){
	}
}
