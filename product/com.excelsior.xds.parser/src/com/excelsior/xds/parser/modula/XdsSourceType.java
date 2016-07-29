package com.excelsior.xds.parser.modula;

public enum XdsSourceType 
{
    /**
     * Modula-2 source code received from *.def, *.mod files
     */
    Modula,   
    
    /**
     * Oberon-2 source code received from *.ob2 files
     */
    Oberon,

    /**
     * Oberon-2/Modula-2 definition received from *.odf files
     */
    OdfFile;

}
