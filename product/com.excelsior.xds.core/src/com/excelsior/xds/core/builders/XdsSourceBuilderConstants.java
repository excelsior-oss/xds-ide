package com.excelsior.xds.core.builders;

public abstract class XdsSourceBuilderConstants {
    public static final String BUILDER_ID = "com.excelsior.xds.builder.compile.XdsSourceBuilder"; //$NON-NLS-1$
    
    /**
     * SKIP build/rebuild steps and perform update of the compilation file set 
     */
    public static final String GET_COMPILATION_SET_ONLY_KEY = "GET_COMPILATION_SET_ONLY_KEY"; //$NON-NLS-1$
    
    /**
     * SKIP build/rebuild steps and perform update of the library file set 
     */
    public static final String GET_LIBRARY_FILE_SET_ONLY_KEY = "GET_LIBRARY_FILE_SET_ONLY_KEY"; //$NON-NLS-1$
    
    /**
     * Include update of the library file set but dont skip another build steps 
     */
    public static final String GET_LIBRARY_FILE_SET_KEY = "GET_LIBRARY_FILE_SET"; //$NON-NLS-1$
    public static final String SOURCE_FILE_PATH_KEY_PREFIX = "SRC"; //$NON-NLS-1$

    public static final String COMPILE_FILE_KEY    = "COMPILE";     //$NON-NLS-1$
    public static final String BUILD_PROJECT_KEY   = "BUILD_PROJECT"; //$NON-NLS-1$
    public static final String REBUILD_PROJECT_KEY = "REBUILD_PROJECT"; //$NON-NLS-1$

}

