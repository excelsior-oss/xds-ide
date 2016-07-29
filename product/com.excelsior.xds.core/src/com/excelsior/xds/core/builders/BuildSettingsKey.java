package com.excelsior.xds.core.builders;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import com.excelsior.xds.core.sdk.Sdk;

/**
 * Helper class to obtain the {@link BuildSettings}.
 * Also used as the utility class to store BuildSettings in hash tables.<br>
 * 
 * @author lsa80
 */
/**
 * @author lsa
 *
 */
public class BuildSettingsKey{
	public final Sdk sdk; 
	public final File workingDir;
	public final File prjFile;
    
    public BuildSettingsKey(Sdk sdk, File workingDir, File prjFile) {
        this.sdk = sdk;
        this.workingDir = workingDir;
        this.prjFile = prjFile;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((prjFile == null) ? 0 : prjFile.hashCode());
        result = prime * result + ((sdk == null) ? 0 : sdk.hashCode());
        result = prime * result
                + ((workingDir == null) ? 0 : workingDir.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BuildSettingsKey other = (BuildSettingsKey) obj;
        if (prjFile == null) {
            if (other.prjFile != null)
                return false;
        } else if (!prjFile.equals(other.prjFile))
            return false;
        if (sdk == null) {
            if (other.sdk != null)
                return false;
        } else if (!equals(sdk, other.sdk))
            return false;
        if (workingDir == null) {
            if (other.workingDir != null)
                return false;
        } else if (!workingDir.equals(other.workingDir))
            return false;
        return true;
    }
    
    private static boolean equals(Sdk sdk1, Sdk sdk2) {
    	if (!StringUtils.equals(sdk1.getCompilerExecutablePath(), sdk2.getCompilerExecutablePath())) {
    		return false;
    	}
    	
    	if (!StringUtils.equals(sdk1.getSdkHomePath(), sdk2.getSdkHomePath())) {
    		return false;
    	}
    	return true;
    }

	@Override
	public String toString() {
		return "BuildSettingsKey [sdk=" + sdk + ", workingDir=" + workingDir
				+ ", prjFile=" + prjFile + "]";
	}
}