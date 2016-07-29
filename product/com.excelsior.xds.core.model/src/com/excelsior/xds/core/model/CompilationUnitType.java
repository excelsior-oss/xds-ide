package com.excelsior.xds.core.model;

public enum CompilationUnitType 
{
    /** Modula-2/Oberon-2 program module: *.mod, *.ob2 */
    PROGRAM_MODULE,
    
    /** Modula-2/Oberon-2 definition module: *.def, *.odf */
    DEFINITION_MODULE,
    
    /** XDS symbol file: *.sym */
    SYMBOL_FILE
}
