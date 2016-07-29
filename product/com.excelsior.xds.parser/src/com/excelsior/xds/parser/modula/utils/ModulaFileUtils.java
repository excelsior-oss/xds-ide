package com.excelsior.xds.parser.modula.utils;

import com.excelsior.xds.core.utils.XdsFileUtils;
import com.excelsior.xds.parser.modula.XdsSourceType;

/**
 * Utility class for recognizing Modula-2 files.  
 */
public final class ModulaFileUtils
{
	/**
	 * Only static method are allowed - this class should have no state
	 */
	private ModulaFileUtils() {
	}
    /**
     * Returns type of a source file by file extension.
     * 
     * @return type of the source file
     */
    public static XdsSourceType getSourceType(String fileName) {
        XdsSourceType sourceType;
        String fileExtension = XdsFileUtils.getLowercasedExtension(fileName);
        
        if (XdsFileUtils.MODULA_FILE_EXTENSIONS.contains(fileExtension)) {
            sourceType = XdsSourceType.Modula;
        }
        else if (XdsFileUtils.XDS_SYMBOL_FILE_EXTENSION.equals(fileExtension)) {
            sourceType = XdsSourceType.OdfFile;
        }
        else if (XdsFileUtils.OBERON_MODULE_FILE_EXTENSION.equals(fileExtension)) {
            sourceType = XdsSourceType.Oberon;
        }
        else if (XdsFileUtils.OBERON_DEFINITION_MODULE_FILE_EXTENSION.equals(fileExtension)) {
            sourceType = XdsSourceType.OdfFile;
        }
        else {
            sourceType = XdsSourceType.Modula;
        }

        return sourceType;
    }
    
}
